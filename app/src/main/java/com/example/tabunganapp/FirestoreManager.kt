package com.example.tabunganapp

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

object FirestoreManager {

    private val db = FirebaseFirestore.getInstance()

    // ── SAVE semua celengan ───────────────────────────────────────
    // Dipakai saat: buat baru, edit, delete
    // TIDAK dipakai saat nabung (pakai tambahSaldo)
    suspend fun saveCelengan(userId: String, list: List<Celengan>) {
        if (userId.isEmpty()) return

        try {
            println("🔥 SAVE FIRESTORE: ${list.size} item")

            val userRef = db.collection("users").document(userId)

            // Ambil semua dokumen celengan yang sudah ada di Firestore
            val existing = userRef.collection("celengan").get().await()
            val existingIds = existing.documents.map { it.id }.toSet()

            // ID celengan yang masih ada di list sekarang
            val currentIds = list.map { it.id }.toSet()

            // Hapus dokumen yang sudah tidak ada di list (celengan dihapus user)
            for (docId in existingIds) {
                if (docId !in currentIds) {
                    userRef.collection("celengan").document(docId).delete().await()
                }
            }

            // Save/update setiap celengan
            list.forEach { celengan ->
                // Pastikan id tidak kosong — safety check
                if (celengan.id.isEmpty()) return@forEach

                val celenganRef = userRef.collection("celengan").document(celengan.id)

                val celenganMap = hashMapOf(
                    "id"         to celengan.id,
                    "nama"       to celengan.nama,
                    "target"     to celengan.target,
                    "terkumpul"  to celengan.terkumpul,
                    "image"      to (celengan.image ?: ""),
                    "nominal"    to celengan.nominal,
                    "jenis"      to celengan.jenis,
                    "notifAktif" to celengan.notifAktif,
                    "jamNotif"   to celengan.jamNotif,
                    "hariNotif"  to celengan.hariNotif
                )

                celenganRef.set(celenganMap, SetOptions.merge()).await()

                // Simpan riwayat
                celengan.riwayat.forEachIndexed { trxIndex, trx ->
                    val trxMap = hashMapOf(
                        "tanggal"    to trx.tanggal,
                        "nominal"    to trx.nominal,
                        "tipe"       to trx.tipe,
                        "keterangan" to trx.keterangan
                    )
                    celenganRef.collection("riwayat")
                        .document("trx_$trxIndex")
                        .set(trxMap)
                        .await()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── TAMBAH SALDO saat nabung ──────────────────────────────────
    suspend fun tambahSaldo(
        userId: String,
        celenganId: String,
        jumlah: Int
    ) {
        // Guard: jangan lanjut kalau id kosong
        if (userId.isEmpty() || celenganId.isEmpty()) return

        try {
            val docRef = db
                .collection("users")
                .document(userId)
                .collection("celengan")
                .document(celenganId)

            // Cek dulu apakah dokumen ada
            val snapshot = docRef.get().await()

            if (snapshot.exists()) {
                // Dokumen ada → increment aman
                docRef.update(
                    "terkumpul",
                    FieldValue.increment(jumlah.toLong())
                ).await()
            } else {
                // Dokumen belum ada di Firestore (misalnya baru dibuat offline)
                // → tidak crash, data sudah aman di memory dan akan di-save
                // via saveCelengan saat list berubah
                println("⚠️ tambahSaldo: dokumen $celenganId tidak ditemukan, skip update Firestore")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Tidak crash app — data sudah update di memory
        }
    }

    // ── TAMBAH RIWAYAT TRANSAKSI ──────────────────────────────────
    suspend fun tambahRiwayat(
        userId: String,
        celenganId: String,
        trx: Transaksi
    ) {
        if (userId.isEmpty() || celenganId.isEmpty()) return

        try {
            val trxMap = hashMapOf(
                "tanggal"    to trx.tanggal,
                "nominal"    to trx.nominal,
                "tipe"       to trx.tipe,
                "keterangan" to trx.keterangan
            )

            db.collection("users")
                .document(userId)
                .collection("celengan")
                .document(celenganId)
                .collection("riwayat")
                .add(trxMap)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── LOAD semua celengan ───────────────────────────────────────
    suspend fun loadCelengan(userId: String): List<Celengan> {
        if (userId.isEmpty()) return emptyList()

        return try {
            val result = mutableListOf<Celengan>()

            val celenganSnapshot = db
                .collection("users")
                .document(userId)
                .collection("celengan")
                .get()
                .await()

            for (doc in celenganSnapshot.documents) {
                val data = doc.data ?: continue

                // Load riwayat
                val riwayatSnapshot = doc.reference
                    .collection("riwayat")
                    .get()
                    .await()

                val riwayatList = mutableStateListOf<Transaksi>()
                for (trxDoc in riwayatSnapshot.documents) {
                    val t = trxDoc.data ?: continue
                    riwayatList.add(
                        Transaksi(
                            tanggal    = t["tanggal"] as? String ?: "",
                            nominal    = (t["nominal"] as? Long)?.toInt() ?: 0,
                            tipe       = t["tipe"] as? String ?: "MASUK",
                            keterangan = t["keterangan"] as? String ?: ""
                        )
                    )
                }

                @Suppress("UNCHECKED_CAST")
                val hariNotif = data["hariNotif"] as? List<String> ?: emptyList()

                // ✅ Pakai doc.id sebagai id celengan
                // (doc.id adalah UUID yang kita set saat saveCelengan)
                result.add(
                    Celengan(
                        id         = doc.id,
                        nama       = data["nama"] as? String ?: "",
                        target     = (data["target"] as? Long)?.toInt() ?: 0,
                        terkumpul  = (data["terkumpul"] as? Long)?.toInt() ?: 0,
                        image      = (data["image"] as? String)?.ifEmpty { null },
                        nominal    = (data["nominal"] as? Long)?.toInt() ?: 0,
                        jenis      = data["jenis"] as? String ?: "Harian",
                        notifAktif = data["notifAktif"] as? Boolean ?: false,
                        jamNotif   = data["jamNotif"] as? String ?: "08:00",
                        hariNotif  = hariNotif,
                        riwayat    = riwayatList
                    )
                )
            }

            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ── PROFILE ───────────────────────────────────────────────────
    suspend fun saveProfileImage(userId: String, imageUrl: String) {
        if (userId.isEmpty()) return
        try {
            db.collection("users")
                .document(userId)
                .set(mapOf("photoUrl" to imageUrl), SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadProfileImage(userId: String): String? {
        if (userId.isEmpty()) return null
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.getString("photoUrl")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}