package com.example.examen3.ui.admin   // usa tu paquete

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.examen3.ui.theme.Examen3Theme

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Examen3Theme { AdminScreen() } }
    }
}
