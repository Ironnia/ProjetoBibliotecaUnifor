package com.example.bibliotecaunifor
import com.google.firebase.firestore.DocumentId

data class Jogo(
    @DocumentId val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val jogadores: String = "",
    val tempoMinutos: Int = 0,
    val disponível: Boolean = true,
// Isso preciso ver como fazer quando chegar na parte de mexer com os jogos
    val idUsuarioComJogo: String? = null
)