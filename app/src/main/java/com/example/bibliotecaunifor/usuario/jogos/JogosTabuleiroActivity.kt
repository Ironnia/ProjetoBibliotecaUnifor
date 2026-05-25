package com.example.bibliotecaunifor.usuario.jogos

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.launch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaJogosTabuleiroBinding
import com.example.bibliotecaunifor.mostrarAviso
import com.example.bibliotecaunifor.mostrarDialogoSimples
import com.example.bibliotecaunifor.pegarEmailUsuario
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects

class JogosTabuleiroActivity : AppCompatActivity() {
    private lateinit var binding: TelaJogosTabuleiroBinding
    private var showingMeusJogos = false

    // firebase e puxar o adaptor
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private lateinit var adapter: JogoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaJogosTabuleiroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.chipDisponivelJogos.setOnClickListener {
            showingMeusJogos = false
            carregarJogos()
        }

        binding.chipMeusJogos.setOnClickListener {
            showingMeusJogos = true
            carregarJogos()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        carregarJogos()

        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, R.id.navigation_perfil_aluno)
    }

    private fun setupRecyclerView() {
        adapter = JogoAdapter(emptyList()) { jogo ->
            if (showingMeusJogos) {
                confirmarDevolucaoJogo(jogo)
            } else {
                verificarLimiteEReservar(jogo)
            }
        }
        binding.rvJogos.layoutManager = LinearLayoutManager(this)
        binding.rvJogos.adapter = adapter
    }

    private fun verificarLimiteEReservar(jogo: Jogo) {
        val uid = auth.currentUser?.uid ?: return

        // verifica limite no firestore
        db.collection("alugueis")
            .whereEqualTo("idUsuario", uid)
            .whereEqualTo("tipoItem", "jogo")
            .whereIn("status", listOf("ativo", "pendente"))
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    mostrarAviso("Você já possui um jogo em uso. Devolva-o primeiro.")
                } else {
                    confirmarReservaJogo(jogo)
                }
            }
    }

    private fun carregarJogos() {
        val uid = auth.currentUser?.uid ?: return

        binding.progressBarJogos.visibility = View.VISIBLE
        binding.tvEmptyStateJogos.visibility = View.GONE
        binding.rvJogos.visibility = View.GONE

        if (showingMeusJogos) {
            db.collection("alugueis")
                .whereEqualTo("idUsuario", uid)
                .whereEqualTo("tipoItem", "jogo")
                .whereIn("status", listOf("pendente", "ativo"))
                .get()
                .addOnSuccessListener { result ->
                    binding.progressBarJogos.visibility = View.GONE
                    val lista = result.map { doc ->
                        val status = doc.getString("status") ?: ""
                        val dataEmprestimo = doc.getLong("dataEmprestimo") ?: 0L
                        val desc = if (status == "pendente") {
                            val horaLimite = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(dataEmprestimo + 15 * 60 * 1000))
                            "Reservado (Retirar até: $horaLimite)"
                        } else {
                            "Em uso"
                        }
                        Jogo(
                            id = doc.id,
                            nome = doc.getString("tituloItem") ?: "",
                            descricao = desc,
                            idUsuarioComJogo = doc.getString("idItem") ?: "",
                            disponivel = false
                        )
                    }
                    adapter.atualizarLista(lista, true)
                    if (lista.isEmpty()) {
                        binding.tvEmptyStateJogos.text = "Você não possui jogos alugados."
                        binding.tvEmptyStateJogos.visibility = View.VISIBLE
                        binding.rvJogos.visibility = View.GONE
                    } else {
                        binding.tvEmptyStateJogos.visibility = View.GONE
                        binding.rvJogos.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener {
                    binding.progressBarJogos.visibility = View.GONE
                    mostrarAviso("Erro ao carregar dados.")
                }
        } else {
            db.collection("jogos")
                .get()
                .addOnSuccessListener { result ->
                    binding.progressBarJogos.visibility = View.GONE
                    val lista = result.toObjects<Jogo>()
                    adapter.atualizarLista(lista, false)
                    if (lista.isEmpty()) {
                        binding.tvEmptyStateJogos.text = "Nenhum jogo disponível no momento."
                        binding.tvEmptyStateJogos.visibility = View.VISIBLE
                        binding.rvJogos.visibility = View.GONE
                    } else {
                        binding.tvEmptyStateJogos.visibility = View.GONE
                        binding.rvJogos.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener {
                    binding.progressBarJogos.visibility = View.GONE
                    mostrarAviso("Erro ao carregar dados.")
                }
        }
    }

    private fun confirmarReservaJogo(item: Jogo) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Reserva")
            .setMessage("Deseja reservar o jogo \"${item.nome}\" por 2 horas?")
            .setPositiveButton("Confirmar") { _, _ ->
                    salvarReservaNoFirebase(item)
//                AlertDialog.Builder(this)
//                    .setTitle("Reserva Realizada")
//                    .setMessage("O jogo foi reservado. Retire no balcão em até 15 minutos.")
//                    .setPositiveButton("Ok", null)
//                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun salvarReservaNoFirebase(jogo: Jogo) {
        val uid = auth.currentUser?.uid ?: return
        val emailUsuario = pegarEmailUsuario() // precisa pegar o email do aluno.

        // emprestismo precisar ser igual do livro
        val novoAluguel = hashMapOf(
            "idUsuario" to uid,
            "emailUsuario" to emailUsuario,
            "idItem" to jogo.id,
            "tituloItem" to jogo.nome,
            "tipoItem" to "jogo",
            "status" to "pendente",
            "dataEmprestimo" to System.currentTimeMillis(),
            "dataDevolucao" to System.currentTimeMillis() + (2 * 60 * 60 * 1000) // Isso é 2 horas. Firestore é assim emsmo.

        )

        // 2. Salvar no Firestore e atualizar o status do jogo
        db.collection("alugueis").add(novoAluguel)
            .addOnSuccessListener {
                // Marca o jogo como indisponível no acervo
                db.collection("jogos").document(jogo.id).update("disponivel", false)
                    .addOnSuccessListener {
                        mostrarDialogoSimples(
                            "Reserva Realizada",
                            "O jogo \"${jogo.nome}\" foi reservado. Retire no balcão em até 15 minutos."
                        )
                        carregarJogos() // Atualiza a lista na tela
                    }
            }
            .addOnFailureListener {
                mostrarAviso("Erro ao processar reserva.")
            }
    }


    private fun confirmarDevolucaoJogo(item: Jogo) {
        val idDoJogoReal = item.idUsuarioComJogo // firebase não aceita poss´pivel nulo em .document
        AlertDialog.Builder(this)
            .setTitle("Devolver Jogo")
            .setMessage("Deseja confirmar a devolução de \"${item.nome}\"?")
            .setPositiveButton("Confirmar") { _, _ ->
                //atualizar no firestore
                db.collection("alugueis").document(item.id).update("status", "devolvido")
                    .addOnSuccessListener {
                        //libera o jogo no acervo
                        idDoJogoReal?.let { idReal ->
                            db.collection("jogos").document(idReal).update("disponivel", true)
                                .addOnSuccessListener {
                                    mostrarAviso("Jogo devolvido! Agora você pode alugar outro.")
                                    carregarJogos()
                                }
                        }
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
//        AlertDialog.Builder(this)
//            .setTitle("Devolver Jogo")
//            .setMessage("Apresente o jogo no balcão para confirmar a devolução.")
//            .setPositiveButton("Ok", null)
//            .show()
    }

    // data class Jogo(val nome: String, val descricao: String, val status: String, val acao: String)
}
