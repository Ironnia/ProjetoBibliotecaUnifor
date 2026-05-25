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
import com.google.firebase.firestore.ListenerRegistration
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


    private var emprestimosListener: ListenerRegistration? = null

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
        adapter = EmprestimoAdapter(
            lista = emptyList(),
            onAcaoClick = { emprestimo ->
                if (statusFiltro == "pendente") {
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
        
        emprestimosListener?.remove()

        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE

        emprestimosListener = db.collection("emprestimos")
            .whereEqualTo("idUsuario", uid)
            .whereEqualTo("status", statusFiltro)
            .whereEqualTo("tipoItem", "livro")
            .addSnapshotListener { result, error ->
                binding.progressBar.visibility = View.GONE
                if (error != null) {
                    mostrarAviso("Erro ao carregar dados.")
                    return@addSnapshotListener
                }

                val lista = result?.toObjects<Emprestimo>() ?: emptyList()
                adapter.atualizarLista(lista)
                if (lista.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvEmprestimos.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvEmprestimos.visibility = View.VISIBLE
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        emprestimosListener?.remove()
    }
}
