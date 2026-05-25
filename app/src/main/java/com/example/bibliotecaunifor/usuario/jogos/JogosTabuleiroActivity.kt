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
import com.example.bibliotecaunifor.crud.JogosRepository
import kotlinx.coroutines.launch

class JogosTabuleiroActivity : AppCompatActivity() {
    private lateinit var binding: TelaJogosTabuleiroBinding
    private var showingMeusJogos = false

    // firebase e puxar o adaptor
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private lateinit var adapter: JogoAdapter
    private var alugueisListener: com.google.firebase.firestore.ListenerRegistration? = null

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

        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, -1) // tirar a selação do menu de navegação.
    }

    private fun setupRecyclerView() {
        adapter = JogoAdapter(emptyList()) { jogo ->
            if (showingMeusJogos) {
                /*
                confirmarDevolucaoJogo(jogo)
                */
                if (jogo.status == "pendente") {
                    cancelarReservaJogo(jogo)
                } else {
                    confirmarDevolucaoJogo(jogo)
                }
            } else {
                verificarLimiteEReservar(jogo)
            }
        }
        binding.rvJogos.layoutManager = LinearLayoutManager(this)
        binding.rvJogos.adapter = adapter
    }

    private fun cancelarReservaJogo(item: Jogo) {
        val idDoJogoReal = item.idUsuarioComJogo ?: return
        AlertDialog.Builder(this)
            .setTitle("Cancelar Reserva")
            .setMessage("Deseja realmente cancelar a reserva do jogo \"${item.nome}\"?")
            .setPositiveButton("Sim, Cancelar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        JogosRepository.cancelarReservaJogo(item.id, idDoJogoReal)
                        mostrarAviso("Reserva de jogo cancelada com sucesso.")
                    } catch (e: Exception) {
                        mostrarAviso("Erro ao cancelar reserva.")
                    }
                }
            }
            .setNegativeButton("Voltar", null)
            .show()
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

        alugueisListener?.remove()

        if (showingMeusJogos) {
            /*
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
                            jogadores = doc.getString("jogadores") ?: "",
                            tempoMinutos = doc.getLong("tempoMinutos")?.toInt() ?: 0,
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
            */
            alugueisListener = db.collection("alugueis")
                .whereEqualTo("idUsuario", uid)
                .whereEqualTo("tipoItem", "jogo")
                .whereIn("status", listOf("pendente", "ativo"))
                .addSnapshotListener { snapshot, error ->
                    binding.progressBarJogos.visibility = View.GONE
                    if (error != null) {
                        mostrarAviso("Erro ao carregar dados.")
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener
                    val lista = snapshot.map { doc ->
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
                            jogadores = doc.getString("jogadores") ?: "",
                            tempoMinutos = doc.getLong("tempoMinutos")?.toInt() ?: 0,
                            idUsuarioComJogo = doc.getString("idItem") ?: "",
                            disponivel = false,
                            status = status
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
        } else {
            /*
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
            */
            alugueisListener = db.collection("jogos")
                .addSnapshotListener { snapshot, error ->
                    binding.progressBarJogos.visibility = View.GONE
                    if (error != null) {
                        mostrarAviso("Erro ao carregar dados.")
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener
                    val lista = snapshot.toObjects<Jogo>()
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
        }
    }

    private fun confirmarReservaJogo(item: Jogo) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Confirmar Reserva")
            .setMessage("Deseja reservar o jogo \"${item.nome}\" por 2 horas?")
            .setPositiveButton("Confirmar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                button.isEnabled = false // Evita cliques múltiplos
                dialog.dismiss()
                salvarReservaNoFirebase(item)
            }
        }
        dialog.show()
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
            "jogadores" to jogo.jogadores,
            "tempoMinutos" to jogo.tempoMinutos,
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
        MaterialAlertDialogBuilder(this)
            .setTitle("Devolver Jogo")
            .setMessage("Apresente o jogo no balcão para confirmar a devolução com o bibliotecário.")
            .setPositiveButton("Ok", null)
            .show()
    }

    // data class Jogo(val nome: String, val descricao: String, val status: String, val acao: String)
    override fun onDestroy() {
        super.onDestroy()
        alugueisListener?.remove()
    }
}
