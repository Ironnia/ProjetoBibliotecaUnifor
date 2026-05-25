package com.example.bibliotecaunifor.admin.agendamentos

data class AdminHorario(
    val horario: String,
    val isOcupado: Boolean,
    val email: String = "", // Nome ou identificador do Aluno
    val idAgendamento: String? = null,
    val idSala: String = "",
    val nomeSala: String = "",
    val data: String = ""
)
