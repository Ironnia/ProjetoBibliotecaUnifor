package com.example.bibliotecaunifor.usuario.emprestimos

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
// import com.example.bibliotecaunifor.Emprestimo
import com.example.bibliotecaunifor.crud.Emprestimo
import com.example.bibliotecaunifor.crud.Entrada
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaMeusLivrosBinding
import com.example.bibliotecaunifor.mostrarAviso
import com.example.bibliotecaunifor.usuario.reserva.DetalhesLivroActivity
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.TextView
import android.widget.ImageView

class MeusLivrosActivity : AppCompatActivity() {
    private lateinit var binding: TelaMeusLivrosBinding
    // private var showingAlugados = true
    private var statusFiltro = "ativo"
    private lateinit var adapter: EmprestimoAdapter
    private val db = Firebase.firestore
    private val auth = Firebase.auth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaMeusLivrosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        carregarEmprestimos()

        binding.chipAlugados.setOnClickListener {
            statusFiltro = "ativo"
            carregarEmprestimos()
        }

        binding.chipARetirar.setOnClickListener {
            statusFiltro = "pendente"
            carregarEmprestimos()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, R.id.navigation_home_aluno)
    }

    private fun setupRecyclerView() {
        // agora vamos usar o adapter que lima legal.
        /*
        adapter = EmprestimoAdapter(
            lista = emptyList(), onAcaoClick = { emprestimo ->
                if (statusFiltro == "ativo") showRenovacaoDialog(emprestimo)
                else showCancelamentoDialog(emprestimo) // Cancela reserva pendente
            },
            onItemClick = { emprestimo ->
                val intent = Intent(this, DetalhesLivroActivity::class.java).apply {
                    putExtra("idLivro", emprestimo.idItem)
                }
                startActivity(intent)
            }
        )
        */
        adapter = EmprestimoAdapter(
            lista = emptyList(),
            onAcaoClick = { emprestimo ->
                if (statusFiltro == "ativo") {
                    showRenovacaoDialog(emprestimo)
                } else {
                    showQrCodeDialog(emprestimo) // "Ver QR Code"
                }
            },
            onItemClick = { emprestimo ->
                val intent = Intent(this, DetalhesLivroActivity::class.java).apply {
                    putExtra("idLivro", emprestimo.idLivro)
                }
                startActivity(intent)
            },
            onCancelarClick = { emprestimo ->
                showCancelamentoDialog(emprestimo)
            }
        )
        binding.rvEmprestimos.layoutManager = LinearLayoutManager(this)
        binding.rvEmprestimos.adapter = adapter
    }

    /*
    private fun showRenovacaoDialog(item: Emprestimo) {
        db.collection("emprestimos")
            .whereEqualTo("idItem", item.idItem)
            .whereEqualTo("status", "pendente")
            .get()
            .addOnSuccessListener { query ->
                if (!query.isEmpty) {
                    mostrarAviso("Não é possível renovar este empréstimo")
                } else {
                    processarRenovacao(item)
                }
            }
            .addOnFailureListener {
                processarRenovacao(item)
            }
    }
    */
    private fun showRenovacaoDialog(item: Emprestimo) {
        db.collection("emprestimos")
            .whereEqualTo("idLivro", item.idLivro)
            .whereEqualTo("status", "pendente")
            .get()
            .addOnSuccessListener { query ->
                if (!query.isEmpty) {
                    mostrarAviso("Não é possível renovar este empréstimo")
                } else {
                    processarRenovacao(item)
                }
            }
            .addOnFailureListener {
                processarRenovacao(item)
            }
    }

    /*
    private fun processarRenovacao(item: Emprestimo) {
        val seteDiasEmMillis = 7 * 24 * 60 * 60 * 1000L
        val novaDataMillis = item.dataDevolucao + seteDiasEmMillis
        val dataFormatada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(novaDataMillis))

        AlertDialog.Builder(this)
            .setTitle("Renovar Empréstimo")
            .setMessage("Deseja estender o prazo de \"${item.tituloItem}\" até o dia $dataFormatada?")
            .setPositiveButton("Confirmar Renovação") { _, _ ->
                db.collection("emprestimos").document(item.id)
                    .update("dataDevolucao", novaDataMillis)
                    .addOnSuccessListener {
                        mostrarAviso("Renovação solicitada com sucesso!")
                        carregarEmprestimos()
                    }
            }
            .setNegativeButton("Agora não", null)
            .show()
    }
    */
    private fun processarRenovacao(item: Emprestimo) {
        val seteDiasEmMillis = 7 * 24 * 60 * 60 * 1000L
        val dataDevolucaoTime = item.dataDevolucaoPrevista?.time ?: 0L
        val novaDataMillis = dataDevolucaoTime + seteDiasEmMillis
        val novaDataDate = Date(novaDataMillis)
        val dataFormatada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(novaDataDate)

        AlertDialog.Builder(this)
            .setTitle("Renovar Empréstimo")
            .setMessage("Deseja estender o prazo de \"${item.tituloLivro}\" até o dia $dataFormatada?")
            .setPositiveButton("Confirmar Renovação") { _, _ ->
                db.collection("emprestimos").document(item.id)
                    .update("dataDevolucaoPrevista", novaDataDate)
                    .addOnSuccessListener {
                        mostrarAviso("Renovação solicitada com sucesso!")
                        carregarEmprestimos()
                    }
            }
            .setNegativeButton("Agora não", null)
            .show()
    }

    /**
     * Exibe diálogo de confirmação para cancelar uma reserva com status "pendente".
     * Ao confirmar, cancela o empréstimo no Firestore e libera o exemplar no acervo.
     */
    /*
    private fun showCancelamentoDialog(item: Emprestimo) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Cancelar Reserva")
            .setMessage("Deseja cancelar a reserva do livro \"${item.tituloItem}\"?\n\nO livro voltará ao acervo imediatamente.")
            .setPositiveButton("Cancelar Reserva") { _, _ ->
                db.collection("emprestimos").document(item.id)
                    .update("status", "cancelado")
                    .addOnSuccessListener {
                        // Libera o exemplar no acervo (+1 disponível)
                        db.collection("Acervo").document(item.idItem)
                            .update("exemplaresDisponiveis", com.google.firebase.firestore.FieldValue.increment(1))
                        mostrarAviso("Reserva cancelada. O livro está disponível novamente.")
                        carregarEmprestimos()
                    }
                    .addOnFailureListener {
                        mostrarAviso("Erro ao cancelar reserva. Tente novamente.")
                    }
            }
            .setNegativeButton("Manter Reserva", null)
            .show()
    }
    */
    private fun showCancelamentoDialog(item: Emprestimo) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Cancelar Reserva")
            .setMessage("Deseja cancelar a reserva do livro \"${item.tituloLivro}\"?\n\nO livro voltará ao acervo imediatamente.")
            .setPositiveButton("Cancelar Reserva") { _, _ ->
                db.runTransaction { transaction ->
                    val livroRef = db.collection("Acervo").document(item.idLivro)
                    val snapshot = transaction.get(livroRef)
                    val entradaDb = snapshot.toObject(Entrada::class.java)

                    if (entradaDb != null) {
                        val novosExemplares = entradaDb.exemplares.map { ex ->
                            if (ex.registro == item.idExemplar) {
                                ex.copy(situacao = "Disponivel")
                            } else {
                                ex
                            }
                        }
                        transaction.update(livroRef, "exemplares", novosExemplares)
                    }

                    val aluguelRef = db.collection("emprestimos").document(item.id)
                    transaction.update(aluguelRef, "status", "cancelado")
                    null
                }.addOnSuccessListener {
                    mostrarAviso("Reserva cancelada. O livro está disponível novamente.")
                    carregarEmprestimos()
                }.addOnFailureListener { e ->
                    mostrarAviso("Erro ao cancelar reserva: ${e.message}")
                }
            }
            .setNegativeButton("Manter Reserva", null)
            .show()
    }

    private fun showQrCodeDialog(item: Emprestimo) {
        val dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_qrcode_reserva, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this).setView(dialogView).create()

        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_qr_title)
        val tvInstructions = dialogView.findViewById<TextView>(R.id.tv_qr_instructions)
        val btnFechar = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_fechar_qr)

        tvTitle.text = "Check-in: Retirada de Livro"
        tvInstructions.text = "Apresente este código no balcão para retirar o seu livro.\nLivro: ${item.tituloLivro}\nExemplar: ${item.idExemplar}"

        btnFechar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun carregarEmprestimos() {
        val uid = auth.currentUser?.uid ?: return
        // pegar os livros do aluno e o status correto.
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        binding.rvEmprestimos.visibility = View.GONE

        db.collection("emprestimos")
            .whereEqualTo("idUsuario", uid)
            .whereEqualTo("status", statusFiltro)
            .whereEqualTo("tipoItem", "livro")
            .get()
            .addOnSuccessListener { result ->
                binding.progressBar.visibility = View.GONE
                val lista = result.toObjects<Emprestimo>()
                adapter.atualizarLista(lista)
                if (lista.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvEmprestimos.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvEmprestimos.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                mostrarAviso("Erro ao carregar dados.")
            }
    }
}
