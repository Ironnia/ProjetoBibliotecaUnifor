package com.example.bibliotecaunifor.usuario.alugueis

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
import com.example.bibliotecaunifor.databinding.TelaAlugueisBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.android.material.button.MaterialButton

class AlugueisActivity : AppCompatActivity() {
    private lateinit var binding: TelaAlugueisBinding
    private var showingAlugados = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAlugueisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.chipParaRetirar.setOnClickListener {
            showingAlugados = false
            setupRecyclerView()
        }

        binding.chipAlugados.setOnClickListener {
            showingAlugados = true
            setupRecyclerView()
        }

        NavigationUtils.setupBottomNavigation(this, binding.bottomNavigation, R.id.navigation_catalogo)
    }

    private fun setupRecyclerView() {
        val items = if (showingAlugados) {
            listOf(
                AluguelItem("Dom Casmurro", "Machado de Assis", "Vence em: 20/04/2026", "Renovar"),
                AluguelItem("A Hora da Estrela", "Clarice Lispector", "Vence em: 25/04/2026", "Renovar")
            )
        } else {
            listOf(
                AluguelItem("O Pequeno Príncipe", "Antoine de Saint-Exupéry", "Retirar até: 05/05/2026", "Ver QR Code"),
                AluguelItem("1984", "George Orwell", "Retirar até: 06/05/2026", "Ver QR Code")
            )
        }

        binding.rvAlugueis.layoutManager = LinearLayoutManager(this)
        binding.rvAlugueis.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            inner class AluguelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val titulo: TextView = view.findViewById(R.id.tvLivroTitulo)
                val autor: TextView = view.findViewById(R.id.tvLivroAutor)
                val status: TextView = view.findViewById(R.id.tvStatusOuVencimento)
                val btn: MaterialButton = view.findViewById(R.id.btnAcaoAluguel)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_aluguel, parent, false)
                return AluguelViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val item = items[position]
                (holder as AluguelViewHolder).titulo.text = item.titulo
                holder.autor.text = item.autor
                holder.status.text = item.status
                holder.btn.text = item.acao
                
                if (showingAlugados) {
                    holder.status.setTextColor(getColor(R.color.error_red))
                } else {
                    holder.status.setTextColor(getColor(R.color.success_green))
                }

                holder.btn.setOnClickListener {
                    if (showingAlugados) {
                        showRenovarDialog(item)
                    } else {
                        showQRCodeDialog(item)
                    }
                }
            }

            override fun getItemCount() = items.size
        }
    }

    private fun showRenovarDialog(item: AluguelItem) {
        AlertDialog.Builder(this)
            .setTitle("Renovar Livro")
            .setMessage("Deseja solicitar a renovação de \"${item.titulo}\"?")
            .setPositiveButton("Confirmar") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Sucesso")
                    .setMessage("Renovação solicitada com sucesso!")
                    .setPositiveButton("Ok", null)
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showQRCodeDialog(item: AluguelItem) {
        AlertDialog.Builder(this)
            .setTitle("QR Code de Retirada")
            .setMessage("Apresente este código no balcão para retirar:\n${item.titulo}\n\n[QR CODE MOCK]")
            .setPositiveButton("Fechar", null)
            .show()
    }

    data class AluguelItem(val titulo: String, val autor: String, val status: String, val acao: String)
}
