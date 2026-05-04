package com.example.bibliotecaunifor.admin.emprestimos

data class AdminEmprestimo(
    val titulo: String,
    val autor: String,
    val matricula: String,
    val dataHora: String,
    val isParaDevolucao: Boolean // true: Para devolução, false: A retirar
)
