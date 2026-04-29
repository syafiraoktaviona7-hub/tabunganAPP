package com.example.tabunganapp

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore by preferencesDataStore(name = "tabungan_prefs")

class DataStoreManager(private val context: Context) {

    private fun getKey(userId: String) =
        stringPreferencesKey("celengan_$userId")

    suspend fun saveCelengan(userId: String, list: List<Celengan>) {
        val jsonArray = JSONArray()

        list.forEach {
            val obj = JSONObject()
            obj.put("nama", it.nama)
            obj.put("target", it.target)
            obj.put("terkumpul", it.terkumpul)
            obj.put("image", it.image) // 🔥 TAMBAHAN PENTING
            obj.put("nominal", it.nominal)
            obj.put("jenis", it.jenis)
            obj.put("notifAktif", it.notifAktif)
            obj.put("jamNotif", it.jamNotif)

            val hariArray = JSONArray()
            it.hariNotif.forEach { h -> hariArray.put(h) }
            obj.put("hariNotif", hariArray)

            jsonArray.put(obj)
        }

        context.dataStore.edit {
            it[getKey(userId)] = jsonArray.toString()
        }
    }

    suspend fun loadCelengan(userId: String): List<Celengan> {
        val prefs = context.dataStore.data.first()
        val jsonString = prefs[getKey(userId)] ?: return emptyList()

        val list = mutableListOf<Celengan>()
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val hariList = mutableListOf<String>()
            val hariArray = obj.optJSONArray("hariNotif")
            if (hariArray != null) {
                for (j in 0 until hariArray.length()) {
                    hariList.add(hariArray.getString(j))
                }
            }

            list.add(
                Celengan(
                    nama = obj.getString("nama"),
                    target = obj.getInt("target"),
                    terkumpul = obj.getInt("terkumpul"),
                    image = obj.optString("image", null), // 🔥 INI KUNCI
                    nominal = obj.optInt("nominal", 0),
                    jenis = obj.optString("jenis", "Harian"),
                    notifAktif = obj.optBoolean("notifAktif", false),
                    jamNotif = obj.optString("jamNotif", "08:00"),
                    hariNotif = hariList
                )
            )
        }

        return list
    }

    // ── FOTO PROFIL ──────────────────────────────────────────────
    private fun getProfileImageKey(userId: String) =
        stringPreferencesKey("profile_image_$userId")

    suspend fun saveProfileImage(userId: String, imageUri: String) {
        context.dataStore.edit { prefs ->
            prefs[getProfileImageKey(userId)] = imageUri
        }
    }

    suspend fun loadProfileImage(userId: String): String? {
        val prefs = context.dataStore.data.first()
        return prefs[getProfileImageKey(userId)]
    }

}