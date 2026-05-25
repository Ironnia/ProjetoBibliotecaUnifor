package com.example.bibliotecaunifor.crud

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Emprestimo(
    @DocumentId
    val id: String = "",
    val idLivro: String = "",
    val idExemplar: String = "", // registro do exemplar físico
    val idUsuario: String = "",
    val nomeUsuario: String = "",
    val tituloLivro: String = "",
    val autorLivro: String = "",
    val status: String = "pendente", // "pendente", "ativo", "devolvido", "atrasado"
    val dataRetirada: Date? = null,
    val dataDevolucaoPrevista: Date? = null,
    val dataDevolucaoReal: Date? = null
)
