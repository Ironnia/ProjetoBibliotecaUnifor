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
        Log.d(TAG, "Iniciando semeadura de dados no Firestore...")
        popularAcervo()
        popularSalas()
        popularJogos()
        popularUsuarios()
    }

    private fun popularAcervo() {
        // Criando exemplares de teste reais conforme especificações
        val exLivro1_1 = mapOf("registro" to "100101", "edicao" to "1", "ano" to 2010, "suporte" to "Impresso", "localizacao" to "Estante A1", "situacao" to "Alugado")
        val exLivro1_2 = mapOf("registro" to "100102", "edicao" to "1", "ano" to 2010, "suporte" to "Impresso", "localizacao" to "Estante A1", "situacao" to "Disponivel")
        
        val exLivro2_1 = mapOf("registro" to "200201", "edicao" to "3", "ano" to 2015, "suporte" to "Impresso", "localizacao" to "Estante B3", "situacao" to "Disponivel")
        val exLivro2_2 = mapOf("registro" to "200202", "edicao" to "3", "ano" to 2015, "suporte" to "Impresso", "localizacao" to "Estante B3", "situacao" to "Disponivel")

        val exLivro3_1 = mapOf("registro" to "300301", "edicao" to "Especial", "ano" to 2009, "suporte" to "Impresso", "localizacao" to "Estante A2", "situacao" to "Alugado")
        
        val exLivro4_1 = mapOf("registro" to "400401", "edicao" to "2", "ano" to 2012, "suporte" to "Impresso", "localizacao" to "Estante C1", "situacao" to "Disponivel")
        
        val exLivro5_1 = mapOf("registro" to "500501", "edicao" to "1", "ano" to 1983, "suporte" to "Impresso", "localizacao" to "Estante D2", "situacao" to "Disponivel")

        val livros = listOf(
            mapOf(
                "titulo" to "O Alquimista",
                "autor" to "Paulo Coelho",
                "isbn" to "9788575427583",
                "edicao" to "1ª",
                "publicacao" to "Rio de Janeiro: Sextante, 2010",
                "cdu" to "869.0(81)-31",
                "cutter" to "C672a",
                "assuntos" to listOf("Ficção brasileira", "Espiritualidade"),
                "exemplares" to listOf(exLivro1_1, exLivro1_2),
                "totalExemplares" to 2,
                "exemplaresAlugados" to 1,
                "exemplaresDisponiveis" to 1,
                "reservaCount" to 15
            ),
            mapOf(
                "titulo" to "Dom Casmurro",
                "autor" to "Machado de Assis",
                "isbn" to "9788520931554",
                "edicao" to "3ª",
                "publicacao" to "Rio de Janeiro: Nova Fronteira, 2015",
                "cdu" to "821.134.3(81)-31",
                "cutter" to "M149d",
                "assuntos" to listOf("Literatura brasileira", "Romance clássico"),
                "exemplares" to listOf(exLivro2_1, exLivro2_2),
                "totalExemplares" to 2,
                "exemplaresAlugados" to 0,
                "exemplaresDisponiveis" to 2,
                "reservaCount" to 38
            ),
            mapOf(
                "titulo" to "O Pequeno Príncipe",
                "autor" to "Antoine de Saint-Exupéry",
                "isbn" to "9788575037133",
                "edicao" to "Especial",
                "publicacao" to "Rio de Janeiro: Agir, 2009",
                "cdu" to "840-3",
                "cutter" to "S137p",
                "assuntos" to listOf("Literatura infantojuvenil", "Filosofia"),
                "exemplares" to listOf(exLivro3_1),
                "totalExemplares" to 1,
                "exemplaresAlugados" to 1,
                "exemplaresDisponiveis" to 0,
                "reservaCount" to 55 // Top 1 mais alugado
            ),
            mapOf(
                "titulo" to "Memórias Póstumas de Brás Cubas",
                "autor" to "Machado de Assis",
                "isbn" to "9788572327581",
                "edicao" to "2ª",
                "publicacao" to "São Paulo: Martin Claret, 2012",
                "cdu" to "821.134.3(81)-31",
                "cutter" to "M149m",
                "assuntos" to listOf("Realismo brasileiro", "Sátira"),
                "exemplares" to listOf(exLivro4_1),
                "totalExemplares" to 1,
                "exemplaresAlugados" to 0,
                "exemplaresDisponiveis" to 1,
                "reservaCount" to 29
            ),
            mapOf(
                "titulo" to "Vidas Secas",
                "autor" to "Graciliano Ramos",
                "isbn" to "9788501014528",
                "edicao" to "1ª",
                "publicacao" to "Rio de Janeiro: Record, 1983",
                "cdu" to "821.134.3(81)-31",
                "cutter" to "R175v",
                "assuntos" to listOf("Regionalismo", "Sertão"),
                "exemplares" to listOf(exLivro5_1),
                "totalExemplares" to 1,
                "exemplaresAlugados" to 0,
                "exemplaresDisponiveis" to 1,
                "reservaCount" to 12
            )
        )

        livros.forEach { livro ->
            db.collection("Acervo").add(livro)
                .addOnSuccessListener { doc ->
                    Log.d(TAG, "Livro '${livro["titulo"]}' semeado no Acervo!")
                    
                    // Adiciona empréstimos correspondentes para testar a Home/Gerenciamento do ADM
                    val exemplaresAlugados = livro["exemplaresAlugados"] as? Int ?: 0
                    if (exemplaresAlugados > 0) {
                        val exemplares = livro["exemplares"] as List<Map<String, Any>>
                        val exemplarAlugado = exemplares.first { it["situacao"] == "Alugado" }
                        
                        db.collection("emprestimos").add(mapOf(
                            "idLivro" to doc.id,
                            "idExemplar" to exemplarAlugado["registro"],
                            "idUsuario" to "4snislpVRtY5Lkshu4bsV7VW3Jr1",
                            "nomeUsuario" to "Vitor Eduardo",
                            "tituloLivro" to livro["titulo"],
                            "autorLivro" to livro["autor"],
                            "status" to "ativo",
                            "dataRetirada" to Date(),
                            "dataDevolucaoPrevista" to Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
                        ))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao semear livro '${livro["titulo"]}': ${e.message}")
                }
        }
    }

    private fun popularSalas() {
        val hoje = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())
        
        // 1. Semear salas e mesas físicas na coleção "salas"
        val salasFisicas = listOf(
            mapOf("nome" to "Estação 01", "tipo" to "mesa", "disponivel" to true, "capacidade" to 1),
            mapOf("nome" to "Estação 02", "tipo" to "mesa", "disponivel" to true, "capacidade" to 1),
            mapOf("nome" to "Estação 03", "tipo" to "mesa", "disponivel" to true, "capacidade" to 1),
            mapOf("nome" to "Sala Temática 01", "tipo" to "sala", "disponivel" to true, "capacidade" to 6),
            mapOf("nome" to "Sala de Estudos 02", "tipo" to "sala", "disponivel" to true, "capacidade" to 4)
        )

        salasFisicas.forEach { sala ->
            db.collection("salas").add(sala)
                .addOnSuccessListener { doc ->
                    Log.d(TAG, "Sala/Mesa '${sala["nome"]}' semeada com sucesso!")
                    
                    // 2. Cria alguns agendamentos correspondentes hoje na coleção "agendamentos"
                    if (sala["nome"] == "Estação 01") {
                        db.collection("agendamentos").add(mapOf(
                            "idUsuario" to "aluno_teste_uid",
                            "nomeUsuario" to "Josué Pereira",
                            "idSala" to doc.id,
                            "nomeSala" to sala["nome"],
                            "data" to hoje,
                            "horario" to "08:00 - 08:50",
                            "status" to "reservado"
                        ))
                    }
                    if (sala["nome"] == "Sala Temática 01") {
                        db.collection("agendamentos").add(mapOf(
                            "idUsuario" to "outro_aluno_uid",
                            "nomeUsuario" to "Ana Souza",
                            "idSala" to doc.id,
                            "nomeSala" to sala["nome"],
                            "data" to hoje,
                            "horario" to "10:00 - 11:30",
                            "status" to "reservado"
                        ))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao semear sala/mesa '${sala["nome"]}': ${e.message}")
                }
        }
    }

    private fun popularJogos() {
        val jogosFisicos = listOf(
            mapOf("nome" to "Catan", "descricao" to "Um jogo de estratégia sobre colonização de uma ilha.", "jogadores" to "3-4", "tempoMinutos" to 90, "disponivel" to true),
            mapOf("nome" to "Dixit", "descricao" to "Um jogo de cartas ilustradas focado em imaginação.", "jogadores" to "3-6", "tempoMinutos" to 30, "disponivel" to true),
            mapOf("nome" to "War", "descricao" to "O clássico jogo de estratégia e conquista de territórios.", "jogadores" to "3-6", "tempoMinutos" to 120, "disponivel" to true),
            mapOf("nome" to "Banco Imobiliário", "descricao" to "Gerencie suas propriedades e leve seus oponentes à falência.", "jogadores" to "2-6", "tempoMinutos" to 60, "disponivel" to true),
            mapOf("nome" to "Ticket to Ride", "descricao" to "Uma aventura ferroviária cruzando o país.", "jogadores" to "2-5", "tempoMinutos" to 45, "disponivel" to true)
        )

        jogosFisicos.forEach { jogo ->
            db.collection("jogos").add(jogo)
                .addOnSuccessListener { doc ->
                    Log.d(TAG, "Jogo '${jogo["nome"]}' semeado no acervo!")
                    
                    // Adiciona aluguéis correspondentes de teste
                    if (jogo["nome"] == "Dixit") {
                        db.collection("alugueis").add(mapOf(
                            "idUsuario" to "4snislpVRtY5Lkshu4bsV7VW3Jr1",
                            "emailUsuario" to "vitor.javas.06@gmail.com",
                            "idItem" to doc.id,
                            "tituloItem" to jogo["nome"],
                            "tipoItem" to "jogo",
                            "status" to "pendente",
                            "dataEmprestimo" to System.currentTimeMillis(),
                            "dataDevolucao" to System.currentTimeMillis() + 2 * 60 * 60 * 1000
                        ))
                    }
                    if (jogo["nome"] == "Ticket to Ride") {
                        db.collection("alugueis").add(mapOf(
                            "idUsuario" to "2l7dc6Pbafdgv1KecMjxpUTks3X2",
                            "emailUsuario" to "test@test.com.br",
                            "idItem" to doc.id,
                            "tituloItem" to jogo["nome"],
                            "tipoItem" to "jogo",
                            "status" to "ativo",
                            "dataEmprestimo" to System.currentTimeMillis(),
                            "dataDevolucao" to System.currentTimeMillis() + 2 * 60 * 60 * 1000
                        ))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao semear jogo '${jogo["nome"]}': ${e.message}")
                }
        }
    }

    private fun popularUsuarios() {
        val usuarios = listOf(
            mapOf("nome" to "João Victor Silva", "email" to "joao.silva@unifor.br", "tipo" to "usuario", "pontos" to 150),
            mapOf("nome" to "Maria Clara Santos", "email" to "maria.santos@unifor.br", "tipo" to "usuario", "pontos" to 120),
            mapOf("nome" to "Pedro Gomes Lima", "email" to "pedro.lima@unifor.br", "tipo" to "usuario", "pontos" to 90),
            mapOf("nome" to "Ana Carolina Souza", "email" to "ana.souza@unifor.br", "tipo" to "usuario", "pontos" to 60),
            mapOf("nome" to "Carlos Eduardo Oliveira", "email" to "carlos.oliveira@unifor.br", "tipo" to "usuario", "pontos" to 30)
        )

        usuarios.forEach { usuario ->
            db.collection("usuario").add(usuario)
                .addOnSuccessListener {
                    Log.d(TAG, "Usuário '${usuario["nome"]}' semeado no banco!")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao semear usuário '${usuario["nome"]}': ${e.message}")
                }
        }
    }
}
