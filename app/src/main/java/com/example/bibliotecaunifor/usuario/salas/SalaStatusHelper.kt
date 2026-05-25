package com.example.bibliotecaunifor.usuario.salas

import java.time.LocalTime
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

object SalaStatusHelper {
    val slots = listOf(
        "07:10 - 08:00", "08:00 - 08:50", "08:50 - 09:40", "09:40 - 10:30",
        "10:40 - 11:30", "11:30 - 12:20", "12:20 - 13:10", "13:10 - 14:00",
        "14:00 - 14:50", "14:50 - 15:40", "15:40 - 16:30", "16:30 - 17:20",
        "17:20 - 18:10", "18:10 - 19:00", "19:00 - 19:50", "19:50 - 20:40",
        "20:40 - 21:30"
    )

    fun calcularStatus(idSala: String, agendamentos: List<AgendamentoDb>): Pair<String, Boolean> {
        val agora = LocalTime.now()
        val dataHoje = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())

        // Filtra agendamentos ativos da sala para hoje
        val agsSala = agendamentos.filter { 
            it.idSala == idSala && it.data == dataHoje && (it.status == "reservado" || it.status == "pendente") 
        }

        // 1. Verifica se está ocupada AGORA
        val slotAtual = slots.find { slot ->
            val partes = slot.split(" - ").map { LocalTime.parse(it) }
            !agora.isBefore(partes[0]) && !agora.isAfter(partes[1])
        }
        val ocupadaAgora = agsSala.any { it.horario == slotAtual }

        // 2. Encontra o próximo slot livre (que ainda não começou)
        val proximoLivre = slots.filter { 
            LocalTime.parse(it.split(" - ")[0]).isAfter(agora) 
        }.find { slot ->
            agsSala.none { it.horario == slot }
        }

        return when {
            ocupadaAgora -> {
                val texto = if (proximoLivre != null) "Ocupada (Livre às ${proximoLivre.split(" - ")[0]})" else "Lotada hoje"
                Pair(texto, false) // false = vermelho
            }
            proximoLivre != null -> {
                val texto = "Livre (Próximo: ${proximoLivre.split(" - ")[0]})"
                Pair(texto, true) // true = verde
            }
            else -> Pair("Lotada por hoje", false)
        }
    }
}
