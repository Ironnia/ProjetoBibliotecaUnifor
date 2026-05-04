package com.example.bibliotecaunifor.admin.agendamentos

data class AdminMesa(
    val nome: String,
    val andar: String,
    val statusTexto: String,
    val isOcupada: Boolean,
    val matricula: String = ""
)
