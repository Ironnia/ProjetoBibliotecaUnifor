package com.example.bibliotecaunifor.usuario.utils

import android.util.Log
import com.example.bibliotecaunifor.crud.Exemplar
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.*

object FirestoreSeedData {
    private val db = Firebase.firestore
    private const val TAG = "FirestoreSeedData"

    fun popularTudo() {
        Log.d(TAG, "Iniciando semeadura do Firestore...")
        popularAcervo()
        popularSalas()
        popularJogos()
    }

    private fun popularAcervo() {
        // Criando exemplares de teste
        val ex1 = Exemplar("2032031", "1", 2009, "Impresso", "Acervo Central", "Disponivel")
        val ex2 = Exemplar("1948573", "2", 2002, "Impresso", "Acervo Central", "Alugado")

        val livros = listOf(
            mapOf(
                "titulo" to "O Pequeno Príncipe",
                "reservaCount" to 25,
                "autor" to "Antoine de Saint-Exupéry",
                "isbn" to "9788575037133",
                "edicao" to "Especial",
                "publicacao" to "Rio de Janeiro: Agir, 2009",
                "cdu" to "840-3",
                "cutter" to "S137p",
                "assuntos" to listOf("Literatura infantojuvenil", "Filosofia"),
                "exemplares" to listOf(ex1, ex2),
                "totalExemplares" to 2,
                "exemplaresAlugados" to 1,
                "exemplaresDisponiveis" to 1
            ),
            mapOf(
                "titulo" to "Dom Casmurro",
                "autor" to "Machado de Assis",
                "isbn" to "9788520931554",
                "status" to "atrasado", // Para testar o resumo do ADM
                "dataDevolucao" to "10/05",
                "exemplares" to listOf(ex2),
                "totalExemplares" to 1,
                "exemplaresAlugados" to 1,
                "exemplaresDisponiveis" to 0
            ),
            mapOf(
                "titulo" to "O Pequeno Príncipe",
                "autor" to "Antoine de Saint-Exupéry",
                "reservaCount" to 42,
                "isbn" to "9788575037133",
                "exemplaresAlugados" to 1
            ),
            mapOf(
                "titulo" to "Dom Casmurro",
                "autor" to "Machado de Assis",
                "reservaCount" to 38,
                "status" to "atrasado",
                "exemplaresAlugados" to 1
            ),
            mapOf(
                "titulo" to "Memórias Póstumas de Brás Cubas",
                "autor" to "Machado de Assis",
                "reservaCount" to 55, // Este será o TOP 1
                "isbn" to "9788572327581",
                "exemplaresAlugados" to 0
            ),
            mapOf(
                "titulo" to "O Cortiço",
                "autor" to "Aluísio Azevedo",
                "reservaCount" to 12,
                "isbn" to "9788572327437",
                "exemplaresAlugados" to 1
            ),
            mapOf(
                "titulo" to "Vidas Secas",
                "autor" to "Graciliano Ramos",
                "reservaCount" to 29,
                "isbn" to "9788501014528",
                "exemplaresAlugados" to 0
            )
        )

        livros.forEach { livro ->
            db.collection("Acervo").add(livro)
                .addOnSuccessListener {
                    Log.d(TAG, "Livro '${livro["titulo"]}' semeado com sucesso no Acervo!")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao semear livro '${livro["titulo"]}': ${e.message}")
                }
            
            // Também adicionamos em 'emprestimos' para alimentar os contadores da Home Admin
            val exemplaresAlugados = livro["exemplaresAlugados"] as? Int ?: 0
            if (exemplaresAlugados > 0) {
                db.collection("emprestimos").add(mapOf(
                    "tituloLivro" to livro["titulo"],
                    "nomeUsuario" to "Aluno Teste",
                    "status" to if (livro.containsKey("status")) livro["status"] else "ativo",
                    "dataDevolucao" to "25/05"
                ))
                .addOnSuccessListener {
                    Log.d(TAG, "Empréstimo para '${livro["titulo"]}' semeado com sucesso!")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao semear empréstimo para '${livro["titulo"]}': ${e.message}")
                }
            }
        }
    }

    private fun popularSalas() {
        val hoje = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())
        val salas = listOf(
            mapOf("nome" to "Sala 01", "status" to "reservado", "data" to hoje),
            mapOf("nome" to "Sala 02", "status" to "ocupada", "data" to hoje),
            mapOf("nome" to "Sala 03", "status" to "reservado", "data" to hoje),
            mapOf("nome" to "Sala 04", "status" to "reservado", "data" to hoje),
            mapOf("nome" to "Sala 05", "status" to "disponivel", "data" to hoje)
        )
        salas.forEach { sala ->
            db.collection("agendamentos").add(sala)
                .addOnSuccessListener {
                    Log.d(TAG, "Sala '${sala["nome"]}' semeada com sucesso!")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao semear sala '${sala["nome"]}': ${e.message}")
                }
        }
    }

    private fun popularJogos() {
        val jogos = listOf(
            mapOf(
                "nome" to "Catan",
                "descricao" to "Um jogo de estratégia sobre colonização de uma ilha.",
                "jogadores" to "3-4",
                "tempoMinutos" to 90, // Campo que estava faltando
                "disponivel" to true
            ),
            mapOf(
                "nome" to "Dixit",
                "descricao" to "Um jogo de cartas ilustradas focado em imaginação.",
                "jogadores" to "3-6",
                "tempoMinutos" to 30,
                "disponivel" to false
            ),
            mapOf(
                "nome" to "War",
                "descricao" to "O clássico jogo de estratégia e conquista de territórios.",
                "jogadores" to "3-6",
                "tempoMinutos" to 120,
                "disponivel" to true
            ),
            mapOf(
                "nome" to "Banco Imobiliário",
                "descricao" to "Gerencie suas propriedades e leve seus oponentes à falência.",
                "jogadores" to "2-6",
                "tempoMinutos" to 60,
                "disponivel" to true
            ),
            mapOf(
                "nome" to "Ticket to Ride",
                "descricao" to "Uma aventura ferroviária cruzando o país.",
                "jogadores" to "2-5",
                "tempoMinutos" to 45,
                "disponivel" to false
            )
        )
        jogos.forEach { jogo ->
            db.collection("jogos").add(jogo)
                .addOnSuccessListener {
                    Log.d(TAG, "Jogo '${jogo["nome"]}' semeado com sucesso!")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao semear jogo '${jogo["nome"]}': ${e.message}")
                }
        }
    }
}
