package com.example.bibliotecaunifor.usuario.historico

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaHistoricoBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class HistoricoActivity : AppCompatActivity() {
    private lateinit var binding: TelaHistoricoBinding
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // Listas globais para cada categoria
    private val listaLivros = mutableListOf<HistoryItem>()
    private val listaJogos = mutableListOf<HistoryItem>()
    private val listaSalas = mutableListOf<HistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaHistoricoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includeToolbar.btnBack.setOnClickListener {
            finish()
        }

        setupHistoryList()

        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, R.id.navigation_perfil_aluno)
    }

    private fun setupHistoryList() {
        val uid = auth.currentUser?.uid ?: return
        val sdfDisplay = java.text.SimpleDateFormat("dd MMM", java.util.Locale("pt", "BR"))

        // Limpa as listas para a nova consulta
        listaLivros.clear()
        listaJogos.clear()
        listaSalas.clear()

        // Configuração do comportamento dos Chips de Filtro
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chip_livros -> atualizarRecyclerView(listaLivros)
                R.id.chip_jogos -> atualizarRecyclerView(listaJogos)
                R.id.chip_salas -> atualizarRecyclerView(listaSalas)
            }
        }

        // Decodifica datas de forma ultra-resiliente (aceita Long, Timestamp, Date e String)
        fun obterTimestamp(doc: com.google.firebase.firestore.DocumentSnapshot, campos: List<String>): Long {
            for (campo in campos) {
                val valor = doc.get(campo) ?: continue // Pega o objeto genérico e pula se for nulo

                when (valor) {
                    is Number -> return valor.toLong()
                    is com.google.firebase.Timestamp -> return valor.toDate().time
                    is java.util.Date -> return valor.time
                    is String -> return valor.toLongOrNull() ?: 0L
                }
            }
            return 0L
        }

        // 1. Carregar Empréstimos de Livros devolvidos/cancelados
        db.collection("emprestimos")
            .whereEqualTo("idUsuario", uid)
            .whereIn("status", listOf("devolvido", "recusado", "cancelado", "expirado"))
            .get()
            .addOnSuccessListener { result ->
                listaLivros.clear()
                result.forEach { doc ->
                    val titulo = doc.getString("tituloLivro") ?: doc.getString("tituloItem") ?: doc.getString("titulo") ?: "Livro"
                    val status = doc.getString("status") ?: ""
                    val itemId = doc.getString("idItem") ?: doc.getString("idLivro") ?: ""
                    val timestamp = obterTimestamp(doc, listOf("dataDevolucaoReal", "dataStatus", "dataDevolucao", "dataEmprestimo", "dataRetirada"))
                    val desc = when (status) {
                        "devolvido" -> "Empréstimo finalizado"
                        "recusado" -> "Reserva recusada"
                        "cancelado" -> "Reserva cancelada"
                        "expirado" -> "Reserva expirada"
                        else -> "Histórico"
                    }
                    val dateStr = if (timestamp > 0) sdfDisplay.format(java.util.Date(timestamp)) else "--/--"
                    listaLivros.add(HistoryItem(titulo, desc, dateStr, "LIVRO", R.drawable.menu_book_24, timestamp, itemId))
                }
                listaLivros.sortByDescending { it.timestamp }
                
                // Se o chip de Livros for o ativo, atualiza a tela
                if (binding.chipLivros.isChecked) {
                    atualizarRecyclerView(listaLivros)
                }
            }

        // 2. Carregar Aluguéis de Jogos finalizados
        db.collection("alugueis")
            .whereEqualTo("idUsuario", uid)
            .whereIn("status", listOf("devolvido", "recusado", "cancelado", "expirado"))
            .get()
            .addOnSuccessListener { result ->
                listaJogos.clear()
                result.forEach { doc ->
                    val titulo = doc.getString("tituloItem") ?: doc.getString("nomeJogo") ?: doc.getString("titulo") ?: "Jogo"
                    val status = doc.getString("status") ?: ""
                    val itemId = doc.getString("idItem") ?: ""
                    val timestamp = obterTimestamp(doc, listOf("dataDevolucaoReal", "dataStatus", "dataDevolucao", "dataAluguel", "dataEmprestimo"))
                    val desc = when (status) {
                        "devolvido" -> "Devolvido no balcão"
                        "recusado" -> "Aluguel recusado"
                        "cancelado" -> "Aluguel cancelado"
                        "expirado" -> "Reserva expirada"
                        else -> "Histórico"
                    }
                    val dateStr = if (timestamp > 0) sdfDisplay.format(java.util.Date(timestamp)) else "--/--"
                    listaJogos.add(HistoryItem(titulo, desc, dateStr, "JOGO", R.drawable.casino_24, timestamp, itemId))
                }
                listaJogos.sortByDescending { it.timestamp }

                // Se o chip de Jogos for o ativo, atualiza a tela
                if (binding.chipJogos.isChecked) {
                    atualizarRecyclerView(listaJogos)
                }
            }

        // 3. Carregar Agendamentos de Salas/Mesas concluídos
        db.collection("agendamentos")
            .whereEqualTo("idUsuario", uid)
            .whereIn("status", listOf("expirado", "concluido", "finalizado", "cancelado"))
            .get()
            .addOnSuccessListener { result ->
                listaSalas.clear()
                result.forEach { doc ->
                    val sala = doc.getString("nomeSala") ?: doc.getString("sala") ?: "Sala/Mesa"
                    val status = doc.getString("status") ?: ""
                    val idSala = doc.getString("idSala") ?: ""
                    val dataStr = doc.getString("data") ?: ""
                    var timestamp = 0L
                    var dateStr = dataStr
                    if (dataStr.isNotEmpty()) {
                        try {
                            val anoAtual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                            val dataCompleta = "$dataStr/$anoAtual"
                            val sdfParseCompleto = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                            val parsedDate = sdfParseCompleto.parse(dataCompleta)
                            if (parsedDate != null) {
                                val horarioStr = doc.getString("horario") ?: "" // Ex: "07:10 - 08:00"
                                var extraMillis = 0L
                                if (horarioStr.isNotEmpty()) {
                                    try {
                                        val horaInicio = horarioStr.substringBefore(" ").trim() // Ex: "07:10"
                                        val partes = horaInicio.split(":")
                                        if (partes.size == 2) {
                                            val minTotal = partes[0].toInt() * 60 + partes[1].toInt()
                                            extraMillis = minTotal * 60 * 1000L
                                        }
                                    } catch (e: Exception) {}
                                }
                                timestamp = parsedDate.time + extraMillis
                                dateStr = sdfDisplay.format(parsedDate)
                            }
                        } catch (e: Exception) {
                            // Mantém o padrão do Firestore se falhar
                        }
                    }
                    val desc = when (status) {
                        "concluido", "finalizado" -> "Uso finalizado"
                        "expirado" -> "Agendamento expirado"
                        "cancelado" -> "Agendamento cancelado"
                        else -> "Histórico"
                    }
                    listaSalas.add(HistoryItem(sala, desc, dateStr, "SALA", R.drawable.calendar_clock_24, timestamp, idSala))
                }
                listaSalas.sortByDescending { it.timestamp }

                // Se o chip de Salas for o ativo, atualiza a tela
                if (binding.chipSalas.isChecked) {
                    atualizarRecyclerView(listaSalas)
                }
            }
    }

    // Função de preenchimento e vinculação do RecyclerView com a lista selecionada
    private fun atualizarRecyclerView(itens: List<HistoryItem>) {
        binding.rvHistory.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@HistoricoActivity)
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
                val item = itens[position]
                (holder as HistoryViewHolder).apply {
                    title.text = item.title
                    desc.text = item.description
                    date.text = item.date
                    type.text = item.type
                    icon.setImageResource(item.iconRes)
                    
                    val isErrorStatus = item.description.contains("expirada", ignoreCase = true) || 
                                        item.description.contains("recusada", ignoreCase = true) || 
                                        item.description.contains("cancelada", ignoreCase = true) ||
                                        item.description.contains("recusado", ignoreCase = true) || 
                                        item.description.contains("cancelado", ignoreCase = true) ||
                                        item.description.contains("expirado", ignoreCase = true)
                    if (isErrorStatus) {
                        icon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
                        icon.setBackgroundResource(R.drawable.bg_circle_red)
                    } else {
                        icon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#004AF7"))
                        icon.setBackgroundResource(R.drawable.bg_circle_white)
                    }
                    
                    itemView.setOnClickListener {
                        when (item.type) {
                            "LIVRO" -> {
                                val intent = Intent(this@HistoricoActivity, com.example.bibliotecaunifor.usuario.reserva.DetalhesLivroActivity::class.java).apply {
                                    putExtra("entrada_id", item.itemId)
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

            override fun getItemCount() = itens.size
        }
    }

    data class HistoryItem(
        val title: String,
        val description: String,
        val date: String,
        val type: String,
        val iconRes: Int,
        val timestamp: Long,
        val itemId: String
    )
}
