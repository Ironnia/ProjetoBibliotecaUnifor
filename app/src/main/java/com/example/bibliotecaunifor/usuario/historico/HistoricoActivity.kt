package com.example.bibliotecaunifor.usuario.historico

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaHistoricoBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils

class HistoricoActivity : AppCompatActivity() {
    private lateinit var binding: TelaHistoricoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaHistoricoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includeToolbar.btnBack.setOnClickListener {
            finish()
        }

        setupHistoryList()

        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, R.id.navigation_perfil) // tentando melhorar a navegação.
    }

    private fun setupHistoryList() {
        val historyItems = listOf(
            HistoryItem("O Código Da Vinci", "Empréstimo finalizado", "15 Abr", "LIVRO", R.drawable.menu_book_24),
            HistoryItem("Sala de Reunião B", "Agendamento concluído", "12 Abr", "SALA", R.drawable.calendar_clock_24),
            HistoryItem("Xadrez Profissional", "Devolvido no balcão", "10 Abr", "JOGO", R.drawable.casino_24),
            HistoryItem("1984 - George Orwell", "Renovação solicitada", "08 Abr", "LIVRO", R.drawable.menu_book_24),
            HistoryItem("Catan: Board Game", "Reserva expirada", "05 Abr", "JOGO", R.drawable.casino_24),
            HistoryItem("Estudo Individual 4", "Uso finalizado", "02 Abr", "SALA", R.drawable.calendar_clock_24)
        )

        binding.rvHistory.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvHistory.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            inner class HistoryViewHolder(view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
                val icon: android.widget.ImageView = view.findViewById(R.id.iv_type_icon)
                val title: android.widget.TextView = view.findViewById(R.id.tv_item_title)
                val desc: android.widget.TextView = view.findViewById(R.id.tv_item_description)
                val date: android.widget.TextView = view.findViewById(R.id.tv_item_date)
                val type: android.widget.TextView = view.findViewById(R.id.tv_item_type)
            }

            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
                val view = android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
                return HistoryViewHolder(view)
            }

            override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
                val item = historyItems[position]
                (holder as HistoryViewHolder).apply {
                    title.text = item.title
                    desc.text = item.description
                    date.text = item.date
                    type.text = item.type
                    icon.setImageResource(item.iconRes)
                    
                    itemView.setOnClickListener {
                        when (item.type) {
                            "LIVRO" -> {
                                val intent = Intent(this@HistoricoActivity, com.example.bibliotecaunifor.usuario.reserva.DetalhesLivroActivity::class.java).apply {
                                    putExtra("title", item.title)
                                    putExtra("author", "Autor do Histórico")
                                    putExtra("available", 1)
                                }
                                startActivity(intent)
                            }
                            "JOGO" -> {
                                val intent = Intent(this@HistoricoActivity, com.example.bibliotecaunifor.usuario.jogos.JogosTabuleiroActivity::class.java)
                                startActivity(intent)
                            }
                            "SALA" -> {
                                val intent = Intent(this@HistoricoActivity, com.example.bibliotecaunifor.usuario.salas.SalasActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }

            override fun getItemCount() = historyItems.size
        }
    }

    data class HistoryItem(
        val title: String,
        val description: String,
        val date: String,
        val type: String,
        val iconRes: Int
    )
}
