package com.example.bibliotecaunifor.crud

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

object EmprestimosRepository {
    private val db = Firebase.firestore

    suspend fun aprovarRetirada(idEmprestimo: String, idLivro: String, registroExemplar: String) {
        val batch = db.batch()

        // 1. Atualiza status do empréstimo para "ativo" e registra data de retirada do servidor
        val emprestimoRef = db.collection("emprestimos").document(idEmprestimo)
        batch.update(emprestimoRef, mapOf(
            "status" to "ativo",
            "dataRetirada" to FieldValue.serverTimestamp()
        ))

        // 2. Busca e atualiza a situação do exemplar no Acervo para "Alugado"
        val livroRef = db.collection("Acervo").document(idLivro)
        val livroSnapshot = livroRef.get().await()
        val entrada = livroSnapshot.toObject(Entrada::class.java)
        
        if (entrada != null) {
            val novosExemplares = entrada.exemplares.map { exemplar ->
                if (exemplar.registro == registroExemplar) {
                    exemplar.copy(situacao = "Alugado")
                } else {
                    exemplar
                }
            }
            batch.update(livroRef, "exemplares", novosExemplares)
            
            // 3. Incrementa o reservaCount para o ranking (Home ADM)
            batch.update(livroRef, "reservaCount", FieldValue.increment(1))
        }

        batch.commit().await()
    }

    suspend fun registrarDevolucao(idEmprestimo: String, idLivro: String, registroExemplar: String, idUsuario: String, concederPontos: Boolean) {
        val batch = db.batch()

        // 1. Atualiza status do empréstimo para "devolvido" e registra a data real de devolução do servidor
        val emprestimoRef = db.collection("emprestimos").document(idEmprestimo)
        batch.update(emprestimoRef, mapOf(
            "status" to "devolvido",
            "dataDevolucaoReal" to FieldValue.serverTimestamp()
        ))

        // 2. Busca e atualiza a situação do exemplar no Acervo de volta para "Disponivel"
        val livroRef = db.collection("Acervo").document(idLivro)
        val livroSnapshot = livroRef.get().await()
        val entrada = livroSnapshot.toObject(Entrada::class.java)
        
        if (entrada != null) {
            val novosExemplares = entrada.exemplares.map { exemplar ->
                if (exemplar.registro == registroExemplar) {
                    exemplar.copy(situacao = "Disponivel")
                } else {
                    exemplar
                }
            }
            batch.update(livroRef, "exemplares", novosExemplares)
        }

        // 3. Se devolvido no prazo, concede +10 pontos ao perfil do usuário (Gamificação B13)
        if (concederPontos) {
            val usuarioRef = db.collection("usuario").document(idUsuario)
            batch.update(usuarioRef, "pontos", FieldValue.increment(10))
        }

        batch.commit().await()
    }

    suspend fun cancelarReserva(idEmprestimo: String) {
        db.collection("emprestimos").document(idEmprestimo).delete().await()
    }
}
