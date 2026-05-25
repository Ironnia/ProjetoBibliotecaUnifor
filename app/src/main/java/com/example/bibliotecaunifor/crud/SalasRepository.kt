package com.example.bibliotecaunifor.crud

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

object SalasRepository {
    private val db = Firebase.firestore

    /**
     * Cancela um agendamento específico no Firestore removendo seu documento.
     */
    suspend fun liberarHorario(idAgendamento: String) {
        db.collection("agendamentos")
            .document(idAgendamento)
            .delete()
            .await()
    }

    /**
     * Cria um agendamento administrativo manual para ocupar um slot de horário.
     */
    suspend fun ocuparHorarioADM(idSala: String, nomeSala: String, data: String, horario: String) {
        val novoAgendamento = hashMapOf(
            "idUsuario" to "admin_bloqueio",
            "nomeUsuario" to "Administrador (Bloqueado)",
            "idSala" to idSala,
            "nomeSala" to nomeSala,
            "data" to data,
            "horario" to horario,
            "status" to "reservado"
        )
        db.collection("agendamentos")
            .add(novoAgendamento)
            .await()
    }

    /**
     * Libera todos os agendamentos ativos de uma mesa ou sala específica para o dia de hoje.
     */
    suspend fun liberarMesaCompleta(idSala: String, dataHoje: String) {
        val snapshot = db.collection("agendamentos")
            .whereEqualTo("idSala", idSala)
            .whereEqualTo("data", dataHoje)
            .whereIn("status", listOf("pendente", "reservado"))
            .get()
            .await()

        if (!snapshot.isEmpty) {
            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
        }
    }
}
