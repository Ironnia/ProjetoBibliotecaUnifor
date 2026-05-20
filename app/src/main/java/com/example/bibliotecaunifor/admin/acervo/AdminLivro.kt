package com.example.bibliotecaunifor.admin.acervo

data class AdminLivro(
    val titulo: String,
    val autor: String,
    val totalExemplares: Int,
    val exemplaresAlugados: Int,
    val hasFullButtons: Boolean = false // true: Editar/Excluir, false: Opções
)
