package com.example.bibliotecaunifor

// Precisa ter os mesmo valores que estamos usando no Firebase.
data class Usuario(
    val nome: String = "",
    val email: String = "",
    val tipo: String = "usuario"
    // val matricula: String = ""
)
