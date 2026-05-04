package com.example.bibliotecaunifor.admin.agendamentos

data class AdminHorario(
    val horario: String,
    val isOcupado: Boolean,
    val matricula: String = ""
)
