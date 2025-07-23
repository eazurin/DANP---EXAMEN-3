package com.example.examen3.util

import android.content.Context
import java.util.UUID

object PidStore {
    private const val KEY = "PID"
    fun getMyPid(ctx: Context): String {
        val prefs = ctx.getSharedPreferences("pid_prefs", Context.MODE_PRIVATE)
        return prefs.getString(KEY, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY, it).apply()
        }
    }
}
