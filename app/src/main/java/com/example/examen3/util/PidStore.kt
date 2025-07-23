package com.example.examen3.util

import android.content.Context
import java.util.UUID

object PidStore {
    private const val KEY = "PID"

    private fun generateShortPid(): String =
        UUID.randomUUID().toString()

    fun getMyPid(ctx: Context): String {
        val prefs = ctx.getSharedPreferences("pid_prefs", Context.MODE_PRIVATE)
        return prefs.getString(KEY, null)
            ?: generateShortPid().also {
                prefs.edit().putString(KEY, it).apply()
            }
    }
}
