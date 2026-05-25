package com.example.bibliotecaunifor.crud

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

object JogosRepository {
    private val db = Firebase.firestore

    /**
     * Aprova a retirada de um jogo reservado, mudando seu status para "ativo".
     */
    suspend fun aprovarRetiradaJogo(idAluguel: String) {
        db.collection("alugueis")
            .document(idAluguel)
            .update("status", "ativo")
            .await()
    }

    /**
     * Registra a devolução do jogo e reativa a sua disponibilidade no acervo de forma atômica.
     */
    suspend fun registrarDevolucaoJogo(idAluguel: String, idJogo: String) {
        val batch = db.batch()

        // 1. Atualiza o status do aluguel para "devolvido"
        val aluguelRef = db.collection("alugueis").document(idAluguel)
        batch.update(aluguelRef, "status", "devolvido")

        // 2. Libera a disponibilidade do jogo de tabuleiro na coleção "jogos"
        val jogoRef = db.collection("jogos").document(idJogo)
        batch.update(jogoRef, "disponivel", true)

        batch.commit().await()
    }

    /**
     * Cancela a reserva pendente do jogo e libera a sua disponibilidade no acervo de forma atômica.
     */
    suspend fun cancelarReservaJogo(idAluguel: String, idJogo: String) {
        val batch = db.batch()

        // 1. Deleta a solicitação de aluguel pendente
        val aluguelRef = db.collection("alugueis").document(idAluguel)
        batch.delete(aluguelRef)

        // 2. Libera a disponibilidade do jogo no acervo
        val jogoRef = db.collection("jogos").document(idJogo)
        batch.update(jogoRef, "disponivel", true)

        batch.commit().await()
    }

    /**
     * Verifica se o usuário já possui algum aluguel ativo de jogo de tabuleiro no banco.
     * Retorna o título do jogo alugado ou null se não houver jogos em uso.
     */
    suspend fun verificarJogoAtivoUsuario(idUsuario: String): String? {
        val result = db.collection("alugueis")
            .whereEqualTo("idUsuario", idUsuario)
            .whereEqualTo("tipoItem", "jogo")
            .whereEqualTo("status", "ativo")
            .get()
            .await()
            
        return if (!result.isEmpty) {
            result.documents[0].getString("tituloItem") ?: "um jogo"
        } else {
            null
        }
    }
}
