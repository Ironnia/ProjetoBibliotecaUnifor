package com.example.bibliotecaunifor

import com.google.firebase.firestore.DocumentId

data class Sala(
    @DocumentId val id: String = "",
    val nome: String = "",
    val tipo: String = "",
    val disponivel: Boolean = true,
    val capacidade: Int = 0
)