package com.example.bibliotecaunifor.crud

import com.google.firebase.firestore.DocumentId

data class AluguelJogo(
    @DocumentId
    val id: String = "",
    val idUsuario: String = "",
    val emailUsuario: String = "",
    val idItem: String = "", // ID do jogo de tabuleiro
    val tituloItem: String = "", // Nome do jogo
    val tipoItem: String = "jogo",
    val status: String = "pendente", // "pendente", "ativo", "devolvido"
    val dataEmprestimo: Long = 0L,
    val dataDevolucao: Long = 0L
)
