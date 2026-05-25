package com.example.bibliotecaunifor.usuario.emprestimos

import android.content.Intent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.Emprestimo
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaMeusLivrosBinding
import com.example.bibliotecaunifor.mostrarAviso
import com.example.bibliotecaunifor.usuario.reserva.DetalhesLivroActivity
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, R.id.navigation_home_admin)
    }

    private fun setupRecyclerView() {
        // agora vamos usar o adapter que lima legal.
        adapter = EmprestimoAdapter(
            lista = emptyList(),onAcaoClick = { emprestimo ->
                if (statusFiltro == "ativo") showRenovacaoDialog(emprestimo)
                else showQrCodeDialog(emprestimo)
            },
            onItemClick = { emprestimo ->
                val intent = Intent(this, DetalhesLivroActivity::class.java).apply {
                    putExtra("idLivro", emprestimo.idItem)
                }
                startActivity(intent)
            }
        )
        binding.rvEmprestimos.layoutManager = LinearLayoutManager(this)
        binding.rvEmprestimos.adapter = adapter

        // Isso era o mokado para funcionar na primeira entrega.
//        val items = if (showingAlugados) {
//            listOf(
//                EmprestimoItem("O Código Da Vinci", "Dan Brown", "15/04/2026", "Renovar"),
//                EmprestimoItem("1984", "George Orwell", "20/04/2026", "Renovar")
//            )
//        } else {
//            listOf(
//                EmprestimoItem("Dom Casmurro", "Machado de Assis", "Retirar até: 10/05", "Ver QR Code")
//            )
//        }
//
//        binding.rvEmprestimos.layoutManager = LinearLayoutManager(this)
//        binding.rvEmprestimos.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//            inner class EmprestimoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//                val titulo: TextView = view.findViewById(R.id.tvLivroTitulo)
//                val autor: TextView = view.findViewById(R.id.tvLivroAutor)
//                val prazo: TextView = view.findViewById(R.id.tvPrazo)
//                val btn: MaterialButton = view.findViewById(R.id.btnAcaoLivro)
//            }
//
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_livro_emprestado, parent, false)
//                return EmprestimoViewHolder(view)
//            }
//
//            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//                val item = items[position]
//                (holder as EmprestimoViewHolder).titulo.text = item.titulo
//                holder.autor.text = item.autor
//                holder.prazo.text = if (showingAlugados) "Prazo: ${item.prazo}" else item.prazo
//                holder.btn.text = item.acao
//
//                holder.btn.setOnClickListener {
//                    if (showingAlugados) {
//                        showRenovacaoDialog(item)
//                    } else {
//                        showQrCodeDialog(item)
//                    }
//                }
//
//                holder.itemView.setOnClickListener {
//                    val intent = android.content.Intent(this@MeusLivrosActivity, com.example.bibliotecaunifor.usuario.reserva.DetalhesLivroActivity::class.java).apply {
//                        putExtra("title", item.titulo)
//                        putExtra("author", item.autor)
//                        putExtra("available", if (showingAlugados) 0 else 1)
//                    }
//                    startActivity(intent)
//                }
//            }
//
//            override fun getItemCount() = items.size
    }

    private fun showRenovacaoDialog(item: Emprestimo) {
        // maldito firestore salva assim:
        val seteDiasEmMillis = 7 * 24 * 60 * 60 * 1000L
        val novaDataMillis = item.dataDevolucao + seteDiasEmMillis
        val dataFormatada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(novaDataMillis))

        AlertDialog.Builder(this)
            .setTitle("Renovar Empréstimo")
            .setMessage("Deseja estender o prazo de \"${item.tituloItem}\" até o dia $dataFormatada?")
            .setPositiveButton("Confirmar Renovação") { _, _ ->
                db.collection("alugueis").document(item.id)
                    .update("dataDevolucao", novaDataMillis)
                    .addOnSuccessListener {
                        mostrarAviso("Renovação solicitada com sucesso!")
                        carregarEmprestimos()
                    }
            }
            .setNegativeButton("Agora não", null)
            .show()

    }

    private fun showQrCodeDialog(item: Emprestimo) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qrcode_reserva, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.tv_qr_instructions).text = "Apresente este código no balcão para retirar o livro \"${item.tituloItem}\"."
        
        dialogView.findViewById<MaterialButton>(R.id.btn_fechar_qr).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
// criado o adapter para solucionar isso.
    //data class Emprestimo(val titulo: String, val autor: String, val prazo: String, val acao: String)

    private fun carregarEmprestimos() {
        val uid = auth.currentUser?.uid ?: return
        // pegar os livros do aluno e o status correto.
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        binding.rvEmprestimos.visibility = View.GONE

        db.collection("alugueis")
            .whereEqualTo("idUsuario", uid)
            .whereEqualTo("status", statusFiltro)
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
