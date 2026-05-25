package com.example.bibliotecaunifor.crud

import com.google.firebase.firestore.DocumentId

data class Entrada(
    @DocumentId
    val id: String= "",
    val isbn: String = "",
    val titulo: String = "",
    val autor: String = "",
    val edicao: String = "",
    val publicacao: String = "",
    val cdu: String = "",
    val cutter: String = "",
    val assuntos: List<String> = emptyList(),
    val exemplares: List<Exemplar> = emptyList(),
    val reservaCount: Int = 0,
    val imageUrl: String = ""
) {
    val totalExemplares: Int get() = exemplares.size
    val exemplaresAlugados: Int get() = exemplares.count { it.situacao == "Alugado" }
    val exemplaresDisponiveis: Int get() = exemplares.count { it.situacao == "Disponivel" }
}

data class Exemplar(
    val registro: String = "",
    val edicao: String = "",
    val ano: Int = 0,
    val suporte: String = "",
    val localizacao: String = "",
    val situacao: String = ""
)