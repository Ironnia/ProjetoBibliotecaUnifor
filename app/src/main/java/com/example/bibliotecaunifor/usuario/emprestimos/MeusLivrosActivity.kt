package com.example.bibliotecaunifor.usuario.emprestimos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaMeusLivrosBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class MeusLivrosActivity : AppCompatActivity() {
    private lateinit var binding: TelaMeusLivrosBinding
    private var showingAlugados = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaMeusLivrosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.chipAlugados.setOnClickListener {
            showingAlugados = true
            setupRecyclerView()
        }

        binding.chipARetirar.setOnClickListener {
            showingAlugados = false
            setupRecyclerView()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        NavigationUtils.setupBottomNavigation(this, binding.bottomNavigation, R.id.navigation_home)
    }

    private fun setupRecyclerView() {
        val items = if (showingAlugados) {
            listOf(
                EmprestimoItem("O Código Da Vinci", "Dan Brown", "15/04/2026", "Renovar"),
                EmprestimoItem("1984", "George Orwell", "20/04/2026", "Renovar")
            )
        } else {
            listOf(
                EmprestimoItem("Dom Casmurro", "Machado de Assis", "Retirar até: 10/05", "Ver QR Code")
            )
        }

        binding.rvEmprestimos.layoutManager = LinearLayoutManager(this)
        binding.rvEmprestimos.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            inner class EmprestimoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val titulo: TextView = view.findViewById(R.id.tvLivroTitulo)
                val autor: TextView = view.findViewById(R.id.tvLivroAutor)
                val prazo: TextView = view.findViewById(R.id.tvPrazo)
                val btn: MaterialButton = view.findViewById(R.id.btnAcaoLivro)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_livro_emprestado, parent, false)
                return EmprestimoViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val item = items[position]
                (holder as EmprestimoViewHolder).titulo.text = item.titulo
                holder.autor.text = item.autor
                holder.prazo.text = if (showingAlugados) "Prazo: ${item.prazo}" else item.prazo
                holder.btn.text = item.acao
                
                holder.btn.setOnClickListener {
                    if (showingAlugados) {
                        showRenovacaoDialog(item)
                    } else {
                        showQrCodeDialog(item)
                    }
                }

                holder.itemView.setOnClickListener {
                    val intent = android.content.Intent(this@MeusLivrosActivity, com.example.bibliotecaunifor.usuario.reserva.DetalhesLivroActivity::class.java).apply {
                        putExtra("title", item.titulo)
                        putExtra("author", item.autor)
                        putExtra("available", if (showingAlugados) 0 else 1)
                    }
                    startActivity(intent)
                }
            }

            override fun getItemCount() = items.size
        }
    }

    private fun showRenovacaoDialog(item: EmprestimoItem) {
        AlertDialog.Builder(this)
            .setTitle("Renovar Empréstimo")
            .setMessage("Deseja renovar o livro \"${item.titulo}\" por mais 14 dias?")
            .setPositiveButton("Renovar") { _, _ ->
                Snackbar.make(binding.root, "Renovação solicitada com sucesso!", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showQrCodeDialog(item: EmprestimoItem) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qrcode_reserva, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.tv_qr_instructions).text = "Apresente este código no balcão para retirar o livro \"${item.titulo}\"."
        
        dialogView.findViewById<MaterialButton>(R.id.btn_fechar_qr).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    data class EmprestimoItem(val titulo: String, val autor: String, val prazo: String, val acao: String)
}
