package com.example.bibliotecaunifor.usuario.utils

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.*

object FirestoreSeedData {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private const val TAG = "FirestoreSeedData"

    fun popularTudo() {
        Log.d(TAG, "Iniciando limpeza e semeadura completa no Firestore...")
        
        // Limpeza sequencial em cascata de todas as coleções envolvidas para evitar qualquer corrida de rede
        limparColecao("Acervo") {
            limparColecao("emprestimos") {
                limparColecao("salas") {
                    limparColecao("agendamentos") {
                        limparColecao("jogos") {
                            limparColecao("alugueis") {
                                limparColecao("usuario") {
                                    Log.d(TAG, "Todas as coleções limpas com sucesso! Iniciando semeadura de novos mocks...")
                                    popularAcervo()
                                    popularSalas()
                                    popularJogos()
                                    popularUsuarios()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun limparColecao(nomeColecao: String, onComplete: () -> Unit) {
        db.collection(nomeColecao).get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    onComplete()
                    return@addOnSuccessListener
                }
                var deletados = 0
                val total = result.size()
                result.forEach { doc ->
                    db.collection(nomeColecao).document(doc.id).delete()
                        .addOnCompleteListener {
                            deletados++
                            if (deletados == total) {
                                Log.d(TAG, "Coleção '$nomeColecao' totalmente limpa.")
                                onComplete()
                            }
                        }
                }
            }
            .addOnFailureListener {
                onComplete()
            }
    }

    private fun popularAcervo() {
        val uidAtual = auth.currentUser?.uid ?: "aluno_teste_uid"
        val nomeAtual = auth.currentUser?.displayName ?: "Aluno Ativo"

        // Criando exemplares de teste reais conforme especificações com IDs e situações consistentes
        val exLivro1_1 = mapOf("registro" to "100101", "edicao" to "3ª", "ano" to 2015, "suporte" to "Impresso", "localizacao" to "Estante B3", "situacao" to "Alugado")
        val exLivro1_2 = mapOf("registro" to "100102", "edicao" to "3ª", "ano" to 2015, "suporte" to "Impresso", "localizacao" to "Estante B3", "situacao" to "Disponivel")
        
        val exLivro2_1 = mapOf("registro" to "200201", "edicao" to "Especial", "ano" to 2009, "suporte" to "Impresso", "localizacao" to "Estante A2", "situacao" to "Disponivel")
        
        val exLivro3_1 = mapOf("registro" to "300301", "edicao" to "1ª", "ano" to 2010, "suporte" to "Impresso", "localizacao" to "Estante A1", "situacao" to "Disponivel")
        
        val exLivro4_1 = mapOf("registro" to "400401", "edicao" to "1ª", "ano" to 1983, "suporte" to "Impresso", "localizacao" to "Estante D2", "situacao" to "Alugado")

        val livros = listOf(
            mapOf(
                "titulo" to "Dom Casmurro",
                "autor" to "Machado de Assis",
                "isbn" to "9788520931554",
                "edicao" to "3ª",
                "publicacao" to "Rio de Janeiro: Nova Fronteira, 2015",
                "cdu" to "821.134.3(81)-31",
                "cutter" to "M149d",
                "assuntos" to listOf("Literatura brasileira", "Romance clássico"),
                "exemplares" to listOf(exLivro1_1, exLivro1_2),
                "totalExemplares" to 2,
                "exemplaresAlugados" to 1,
                "exemplaresDisponiveis" to 1,
                "reservaCount" to 38,
                "imageUrl" to "https://images-na.ssl-images-amazon.com/images/I/81k392G-L5L.jpg"
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
                "exemplares" to listOf(exLivro2_1),
                "totalExemplares" to 1,
                "exemplaresAlugados" to 0,
                "exemplaresDisponiveis" to 1,
                "reservaCount" to 55,
                "imageUrl" to "https://images-na.ssl-images-amazon.com/images/I/71uGr7ZUXaL.jpg"
            ),
            mapOf(
                "titulo" to "O Alquimista",
                "autor" to "Paulo Coelho",
                "isbn" to "9788575427583",
                "edicao" to "1ª",
                "publicacao" to "Rio de Janeiro: Sextante, 2010",
                "cdu" to "869.0(81)-31",
                "cutter" to "C672a",
                "assuntos" to listOf("Ficção brasileira", "Espiritualidade"),
                "exemplares" to listOf(exLivro3_1),
                "totalExemplares" to 1,
                "exemplaresAlugados" to 0,
                "exemplaresDisponiveis" to 1,
                "reservaCount" to 15,
                "imageUrl" to "https://images-na.ssl-images-amazon.com/images/I/51ZBY56q0kL.jpg"
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
                "exemplares" to listOf(exLivro4_1),
                "totalExemplares" to 1,
                "exemplaresAlugados" to 1,
                "exemplaresDisponiveis" to 0,
                "reservaCount" to 12,
                "imageUrl" to "https://images-na.ssl-images-amazon.com/images/I/81j8vI-rRSL.jpg"
            )
        )

        livros.forEach { livro ->
            db.collection("Acervo").add(livro)
                .addOnSuccessListener { doc ->
                    Log.d(TAG, "Livro '${livro["titulo"]}' semeado no Acervo!")

                    val titulo = livro["titulo"] as String
                    
                    // Vincula empréstimos correspondentes de teste associados dinamicamente ao aluno ativo
                    if (titulo == "Dom Casmurro") {
                        // 1. Empréstimo ATIVO (aparece em "Alugados" e em "Próximas Devoluções")
                        db.collection("emprestimos").add(mapOf(
                            "idLivro" to doc.id,
                            "idExemplar" to "100101",
                            "idUsuario" to uidAtual,
                            "nomeUsuario" to nomeAtual,
                            "tituloLivro" to "Dom Casmurro",
                            "autorLivro" to "Machado de Assis",
                            "status" to "ativo",
                            "tipoItem" to "livro",
                            "dataRetirada" to Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L),
                            "dataDevolucaoPrevista" to Date(System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000L)
                        ))
                    }
                    if (titulo == "O Pequeno Príncipe") {
                        // 2. Empréstimo PENDENTE (aparece em "A retirar" para ver o QR Code)
                        db.collection("emprestimos").add(mapOf(
                            "idLivro" to doc.id,
                            "idExemplar" to "200201",
                            "idUsuario" to uidAtual,
                            "nomeUsuario" to nomeAtual,
                            "tituloLivro" to "O Pequeno Príncipe",
                            "autorLivro" to "Antoine de Saint-Exupéry",
                            "status" to "pendente",
                            "tipoItem" to "livro",
                            "dataRetirada" to null,
                            "dataDevolucaoPrevista" to Date(System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000L)
                        ))
                    }
                    if (titulo == "O Alquimista") {
                        // 3. Empréstimo DEVOLVIDO (aparece no Histórico!)
                        db.collection("emprestimos").add(mapOf(
                            "idLivro" to doc.id,
                            "idExemplar" to "300301",
                            "idUsuario" to uidAtual,
                            "nomeUsuario" to nomeAtual,
                            "tituloLivro" to "O Alquimista",
                            "autorLivro" to "Paulo Coelho",
                            "status" to "devolvido",
                            "tipoItem" to "livro",
                            "dataRetirada" to Date(System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L),
                            "dataDevolucaoPrevista" to Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L),
                            "dataDevolucaoReal" to Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L)
                        ))
                    }
                    if (titulo == "Vidas Secas") {
                        // 4. Empréstimo ATRASADO (para testar multas!)
                        db.collection("emprestimos").add(mapOf(
                            "idLivro" to doc.id,
                            "idExemplar" to "400401",
                            "idUsuario" to uidAtual,
                            "nomeUsuario" to nomeAtual,
                            "tituloLivro" to "Vidas Secas",
                            "autorLivro" to "Graciliano Ramos",
                            "status" to "ativo",
                            "tipoItem" to "livro",
                            "dataRetirada" to Date(System.currentTimeMillis() - 12 * 24 * 60 * 60 * 1000L),
                            "dataDevolucaoPrevista" to Date(System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000L) // Atrasado há 4 dias
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
        val uidAtual = auth.currentUser?.uid ?: "aluno_teste_uid"
        val nomeAtual = auth.currentUser?.displayName ?: "Aluno Ativo"

        val salasFisicas = listOf(
            mapOf("nome" to "Mesa de Estudos 01", "tipo" to "mesa", "disponivel" to true, "capacidade" to 1),
            mapOf("nome" to "Mesa de Estudos 02", "tipo" to "mesa", "disponivel" to true, "capacidade" to 1),
            mapOf("nome" to "Mesa de Estudos 03", "tipo" to "mesa", "disponivel" to true, "capacidade" to 1),
            mapOf("nome" to "Sala Temática 01", "tipo" to "sala", "disponivel" to true, "capacidade" to 1)
        )

        salasFisicas.forEach { sala ->
            db.collection("salas").add(sala)
                .addOnSuccessListener { doc ->
                    Log.d(TAG, "Sala/Mesa '${sala["nome"]}' semeada com sucesso!")
                    
                    val nome = sala["nome"] as String
                    
                    // Semear agendamentos de teste vinculados reativamente ao usuário logado
                    if (nome == "Mesa de Estudos 01") {
                        // Agendamento ativo hoje do aluno logado
                        db.collection("agendamentos").add(mapOf(
                            "idUsuario" to uidAtual,
                            "nomeUsuario" to nomeAtual,
                            "idSala" to doc.id,
                            "nomeSala" to nome,
                            "data" to hoje,
                            "horario" to "08:00 - 08:50",
                            "status" to "reservado"
                        ))
                    }
                    if (nome == "Sala Temática 01") {
                        // Agendamento concluído no passado do aluno logado (aparece no Histórico!)
                        db.collection("agendamentos").add(mapOf(
                            "idUsuario" to uidAtual,
                            "nomeUsuario" to nomeAtual,
                            "idSala" to doc.id,
                            "nomeSala" to nome,
                            "data" to "20/05",
                            "horario" to "14:00 - 15:30",
                            "status" to "concluido"
                        ))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao semear sala/mesa '${sala["nome"]}': ${e.message}")
                }
        }
    }

    private fun popularJogos() {
        val uidAtual = auth.currentUser?.uid ?: "aluno_teste_uid"
        val emailAtual = auth.currentUser?.email ?: "aluno@teste.com"

        val jogosFisicos = listOf(
            mapOf("nome" to "Catan", "descricao" to "Um jogo de estratégia sobre colonização de uma ilha.", "jogadores" to "3-4", "tempoMinutos" to 90, "disponivel" to true),
            mapOf("nome" to "Dixit", "descricao" to "Um jogo de cartas ilustradas focado em imaginação.", "jogadores" to "3-6", "tempoMinutos" to 30, "disponivel" to true),
            mapOf("nome" to "War", "descricao" to "O clássico jogo de estratégia e conquista de territórios.", "jogadores" to "3-6", "tempoMinutos" to 120, "disponivel" to true)
        )

        jogosFisicos.forEach { jogo ->
            db.collection("jogos").add(jogo)
                .addOnSuccessListener { doc ->
                    Log.d(TAG, "Jogo '${jogo["nome"]}' semeado no acervo!")
                    
                    val nome = jogo["nome"] as String
                    
                    // Semear aluguéis em tempo real vinculados ao usuário logado
                    if (nome == "Catan") {
                        // 1. Aluguel de Jogo Concluído no passado (aparece no Histórico!)
                        db.collection("alugueis").add(mapOf(
                            "idUsuario" to uidAtual,
                            "emailUsuario" to emailAtual,
                            "idItem" to doc.id,
                            "tituloItem" to nome,
                            "tipoItem" to "jogo",
                            "status" to "devolvido",
                            "dataEmprestimo" to System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L,
                            "dataDevolucao" to System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L + 2 * 60 * 60 * 1000L,
                            "jogadores" to "3-4",
                            "tempoMinutos" to 90L
                        ))
                    }
                    if (nome == "Dixit") {
                        // 2. Aluguel de Jogo Ativo hoje
                        db.collection("alugueis").add(mapOf(
                            "idUsuario" to uidAtual,
                            "emailUsuario" to emailAtual,
                            "idItem" to doc.id,
                            "tituloItem" to nome,
                            "tipoItem" to "jogo",
                            "status" to "ativo",
                            "dataEmprestimo" to System.currentTimeMillis() - 1 * 60 * 60 * 1000L,
                            "dataDevolucao" to System.currentTimeMillis() + 1 * 60 * 60 * 1000L,
                            "jogadores" to "3-6",
                            "tempoMinutos" to 30L
                        ))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao semear jogo '${jogo["nome"]}': ${e.message}")
                }
        }
    }

    private fun popularUsuarios() {
        val uidAtual = auth.currentUser?.uid
        
        // 1. Se houver aluno logado no momento, garante a recriação ou atualização dos dados dele com 110 pontos (Top 3) de forma resiliente
        if (uidAtual != null) {
            val emailAtual = auth.currentUser?.email ?: "aluno@teste.com"
            val nomeAtual = auth.currentUser?.displayName ?: "Aluno Ativo"
            // Preserva o privilégio administrativo se o email ativo contiver 'admin'
            val tipoAtual = if (emailAtual.contains("admin", ignoreCase = true)) "admin" else "usuario"
            
            db.collection("usuario").document(uidAtual).set(mapOf(
                "nome" to nomeAtual,
                "email" to emailAtual,
                "tipo" to tipoAtual,
                "pontos" to 110
            ), com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Dados do usuário logado ($tipoAtual) sincronizados com 110 pontos no Firestore.")
                }
        }

        // 2. Criação dos outros alunos do Ranking + Admin Padrão no FirebaseAuth + Firestore para sincronismo total
        val usuarios = listOf(
            mapOf("nome" to "João Victor Silva", "email" to "joao.silva@unifor.br", "pontos" to 150, "tipo" to "usuario"),
            mapOf("nome" to "Maria Clara Santos", "email" to "maria.santos@unifor.br", "pontos" to 130, "tipo" to "usuario"),
            mapOf("nome" to "Pedro Gomes Lima", "email" to "pedro.lima@unifor.br", "pontos" to 90, "tipo" to "usuario"),
            mapOf("nome" to "Ana Carolina Souza", "email" to "ana.souza@unifor.br", "pontos" to 60, "tipo" to "usuario"),
            mapOf("nome" to "Carlos Eduardo Oliveira", "email" to "carlos.oliveira@unifor.br", "pontos" to 30, "tipo" to "usuario"),
            mapOf("nome" to "Administrador Geral", "email" to "admin@unifor.br", "pontos" to 0, "tipo" to "admin")
        )

        usuarios.forEach { usuario ->
            val email = usuario["email"] as String
            val nome = usuario["nome"] as String
            val pontos = usuario["pontos"] as Int
            val tipo = usuario["tipo"] as String
            
            // Tenta registrar o usuário no Firebase Auth
            auth.createUserWithEmailAndPassword(email, "123456")
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: return@addOnSuccessListener
                    // Salva no Firestore usando o UID especial retornado do Auth
                    db.collection("usuario").document(uid).set(mapOf(
                        "nome" to nome,
                        "email" to email,
                        "tipo" to tipo,
                        "pontos" to pontos
                    ))
                    Log.d(TAG, "Usuário '$nome' ($tipo) criado no Auth e semeado no Firestore com sucesso!")
                }
                .addOnFailureListener { e ->
                    // Caso o usuário já exista no FirebaseAuth (Colisão de email)
                    Log.w(TAG, "Usuário '$nome' já existe no Auth. Semeando direto ou atualizando no Firestore... ${e.message}")
                    
                    // Buscamos o documento no Firestore pelo email e atualizamos a pontuação
                    db.collection("usuario")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener { result ->
                            if (!result.isEmpty) {
                                val docId = result.documents.first().id
                                db.collection("usuario").document(docId).update(mapOf(
                                    "nome" to nome,
                                    "pontos" to pontos,
                                    "tipo" to tipo
                                ))
                                Log.d(TAG, "Dados do usuário '$nome' ($tipo) sincronizados no Firestore.")
                            } else {
                                // Se não houver no Firestore mas existir no Auth, geramos com id temporário consistente
                                val tempId = "user_auth_collision_" + email.replace(".", "_")
                                db.collection("usuario").document(tempId).set(mapOf(
                                    "nome" to nome,
                                    "email" to email,
                                    "tipo" to tipo,
                                    "pontos" to pontos
                                ))
                            }
                        }
                }
        }
    }
}
