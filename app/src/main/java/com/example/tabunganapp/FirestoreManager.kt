package com.example.tabunganapp

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

// ═══════════════════════════════════════════════════════════════════
//  FIRESTORE MANAGER — Ganti DataStore, data tersimpan di cloud
// ═══════════════════════════════════════════════════════════════════

object FirestoreManager {

    private val db = FirebaseFirestore.getInstance()

    // ── SAVE semua celengan user ke Firestore ──────────────────────
    suspend fun saveCelengan(userId: String, list: List<Celengan>) {
        if (userId.isEmpty()) return

        val userRef = db.collection("users").document(userId)

        // Hapus riwayat sub-collection dulu, baru hapus celengan
        // (Firestore TIDAK otomatis hapus sub-collection)
        val existing = userRef.collection("celengan").get().await()
        for (doc in existing.documents) {
            val riwayatDocs = doc.reference.collection("riwayat").get().await()
            for (riwayatDoc in riwayatDocs.documents) {
                riwayatDoc.reference.delete().await()
            }
            doc.reference.delete().await()
        }

        // Simpan ulang semua celengan + riwayatnya
        list.forEachIndexed { index, celengan ->
            val celenganId  = "celengan_$index"
            val celenganRef = userRef.collection("celengan").document(celenganId)

            val celenganMap = hashMapOf(
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
            celenganRef.set(celenganMap).await()

            // Simpan riwayat transaksi sebagai sub-collection
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
    }

    // ── LOAD semua celengan user dari Firestore ────────────────────
    suspend fun loadCelengan(userId: String): List<Celengan> {
        if (userId.isEmpty()) return emptyList()

        val result = mutableListOf<Celengan>()

        val celenganSnapshot = db
            .collection("users")
            .document(userId)
            .collection("celengan")
            .get()
            .await()

        for (doc in celenganSnapshot.documents) {
            val data = doc.data ?: continue

            // Load riwayat dari sub-collection
            val riwayatSnapshot = doc.reference.collection("riwayat").get().await()
            val riwayatList     = mutableStateListOf<Transaksi>()

            for (trxDoc in riwayatSnapshot.documents) {
                val t = trxDoc.data ?: continue
                riwayatList.add(
                    Transaksi(
                        tanggal    = t["tanggal"]    as? String ?: "",
                        nominal    = (t["nominal"]   as? Long)?.toInt() ?: 0,
                        tipe       = t["tipe"]       as? String ?: "MASUK",
                        keterangan = t["keterangan"] as? String ?: ""
                    )
                )
            }

            // @Suppress di baris sendiri — tidak boleh inline dalam named argument
            @Suppress("UNCHECKED_CAST")
            val hariNotif = data["hariNotif"] as? List<String> ?: emptyList()

            result.add(
                Celengan(
                    nama       = data["nama"]       as? String ?: "",
                    target     = (data["target"]    as? Long)?.toInt() ?: 0,
                    terkumpul  = (data["terkumpul"] as? Long)?.toInt() ?: 0,
                    image      = (data["image"]     as? String)?.ifEmpty { null },
                    nominal    = (data["nominal"]   as? Long)?.toInt() ?: 0,
                    jenis      = data["jenis"]      as? String ?: "Harian",
                    notifAktif = data["notifAktif"] as? Boolean ?: false,
                    jamNotif   = data["jamNotif"]   as? String ?: "08:00",
                    hariNotif  = hariNotif,
                    riwayat    = riwayatList
                )
            )
        }

        return result
    }

    // ── SAVE foto profil URL ke Firestore ─────────────────────────
    suspend fun saveProfileImage(userId: String, imageUrl: String) {
        if (userId.isEmpty()) return
        db.collection("users")
            .document(userId)
            .set(mapOf("photoUrl" to imageUrl), SetOptions.merge())
            .await()
    }

    // ── LOAD foto profil URL dari Firestore ───────────────────────
    suspend fun loadProfileImage(userId: String): String? {
        if (userId.isEmpty()) return null
        val doc = db.collection("users").document(userId).get().await()
        return doc.getString("photoUrl")
    }
}