package com.example.bibliotecaunifor

import com.google.firebase.firestore.DocumentId

data class Emprestimo(
    @DocumentId val id: String = "",
    val idUsuario: String = "",
    val matricula: String = "",
    val idItem: String = "", // essa é a id do Livro ou Jogo, algo que precisamos colocar para registrar no CRUD.
    val tituloItem: String = "",
    val dataEmprestimo: Long = 0,
    val dataDevolucao: Long = 0,
    val status: String = "pendente", // vai ser variação de pendente, ativo, devolvido, atrasado
    val tipoItem: String = "livro" // livro ou jogo
)