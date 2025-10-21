package com.example.notasytareas.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatFecha(timestamp: Long, formato: String = "dd 'de' MMMM, yyyy"): String {
    val sdf = SimpleDateFormat(formato, Locale.getDefault())
    return sdf.format(Date(timestamp))
}