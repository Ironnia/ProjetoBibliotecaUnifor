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
            mapOf("nome" to "Sala 01", "andar" to "1º", "status" to "reservado", "data" to hoje),
            mapOf("nome" to "Sala 02", "andar" to "1º", "status" to "ocupada", "data" to hoje),
            mapOf("nome" to "Sala 03", "andar" to "2º", "status" to "reservado", "data" to hoje)
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
            mapOf("nome" to "Catan", "disponivel" to true, "jogadores" to "3-4"),
            mapOf("nome" to "Dixit", "disponivel" to false, "jogadores" to "3-6")
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
