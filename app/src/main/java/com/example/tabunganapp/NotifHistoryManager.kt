package com.example.tabunganapp

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object NotifHistoryManager {

    private const val PREFS_NAME = "wishpay_notif_history"
    private const val KEY_HISTORY = "history"
    private const val SEPARATOR_ENTRY = "\n"
    private const val SEPARATOR_FIELD = "|||"
    private const val MAX_HISTORY = 50   // Batasi agar tidak terlalu besar

    /**
     * Dipanggil dari NotifReceiver saat notifikasi dikirim ke HP.
     * Menyimpan: waktu kirim + nama celengan (jika ada)
     */
    fun save(context: Context, namaTabungan: String = "") {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val timeStr = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id")).format(Date())

        val newEntry = "$timeStr$SEPARATOR_FIELD$namaTabungan"

        val existing = prefs.getString(KEY_HISTORY, "") ?: ""
        val entries = if (existing.isEmpty()) mutableListOf()
        else existing.split(SEPARATOR_ENTRY).toMutableList()

        // Tambah di depan (paling baru dulu), batasi jumlah
        entries.add(0, newEntry)
        if (entries.size > MAX_HISTORY) entries.subList(MAX_HISTORY, entries.size).clear()

        prefs.edit().putString(KEY_HISTORY, entries.joinToString(SEPARATOR_ENTRY)).apply()
    }

    /**
     * Dipanggil dari App() / NotificationScreen untuk load riwayat.
     * Return: List<NotificationItem> siap pakai
     */
    fun load(context: Context): List<NotificationItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_HISTORY, "") ?: ""
        if (raw.isEmpty()) return emptyList()

        return raw.split(SEPARATOR_ENTRY)
            .mapNotNull { entry ->
                val parts = entry.split(SEPARATOR_FIELD)
                if (parts.isEmpty()) return@mapNotNull null
                val timeStr = parts[0]
                val nama = parts.getOrElse(1) { "" }
                NotificationItem(
                    title = "Pengingat Menabung",
                    message = if (nama.isNotEmpty())
                        "Jangan lupa nabung untuk $nama ✨"
                    else
                        "Yuk isi tabungan kamu hari ini ✨",
                    time = timeStr
                )
            }
    }

    /** Hapus semua riwayat (opsional, untuk tombol "Hapus Semua") */
    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().remove(KEY_HISTORY).apply()
    }
}