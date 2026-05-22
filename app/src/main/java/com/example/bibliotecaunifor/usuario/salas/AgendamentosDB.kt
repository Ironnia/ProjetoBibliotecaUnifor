package com.example.bibliotecaunifor.usuario.salas

import com.google.firebase.firestore.DocumentId

data class AgendamentoDb(
    @DocumentId val id: String = "",
    val idUsuario: String = "",
    val nomeUsuario: String = "",
    val idSala: String = "",
    val nomeSala: String = "",
    val data: String = "",
    val horario: String = "",
    val status: String = "reservado"
)
