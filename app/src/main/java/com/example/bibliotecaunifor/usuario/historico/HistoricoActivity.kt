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
        val list = mutableListOf<HistoryItem>()
        var pendingQueries = 3

        val sdfParse = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
        val sdfDisplay = java.text.SimpleDateFormat("dd MMM", java.util.Locale("pt", "BR"))

        // Decodifica datas resilientes de forma compatível com Long e com objetos Timestamp do Firebase
        fun obterTimestamp(doc: com.google.firebase.firestore.DocumentSnapshot, campos: List<String>): Long {
            for (campo in campos) {
                val longVal = doc.getLong(campo)
                if (longVal != null) return longVal
                
                val tsVal = doc.getTimestamp(campo)
                if (tsVal != null) return tsVal.toDate().time
            }
            return System.currentTimeMillis()
        }

        fun checkAndPublish() {
            pendingQueries--
            if (pendingQueries == 0) {
                list.sortByDescending { it.timestamp }
                
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
                        val item = list[position]
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

                    override fun getItemCount() = list.size
                }
            }
        }

        // 1. Livros
        db.collection("emprestimos")
            .whereEqualTo("idUsuario", uid)
            .whereIn("status", listOf("devolvido", "recusado", "cancelado", "expirado"))
            .get()
            .addOnSuccessListener { result ->
                result.forEach { doc ->
                    val titulo = doc.getString("tituloItem") ?: "Livro"
                    val status = doc.getString("status") ?: ""
                    val itemId = doc.getString("idItem") ?: ""
                    val timestamp = obterTimestamp(doc, listOf("dataEmprestimo"))
                    val desc = when (status) {
                        "devolvido" -> "Empréstimo finalizado"
                        "recusado" -> "Reserva recusada"
                        "cancelado" -> "Reserva cancelada"
                        "expirado" -> "Reserva expirada"
                        else -> "Histórico"
                    }
                    val dateStr = sdfDisplay.format(java.util.Date(timestamp))
                    list.add(HistoryItem(titulo, desc, dateStr, "LIVRO", R.drawable.menu_book_24, timestamp, itemId))
                }
                checkAndPublish()
            }
            .addOnFailureListener {
                checkAndPublish()
            }

        // 2. Jogos
        db.collection("alugueis")
            .whereEqualTo("idUsuario", uid)
            .whereIn("status", listOf("devolvido", "recusado", "cancelado", "expirado"))
            .get()
            .addOnSuccessListener { result ->
                result.forEach { doc ->
                    val titulo = doc.getString("tituloItem") ?: doc.getString("nomeJogo") ?: "Jogo"
                    val status = doc.getString("status") ?: ""
                    val itemId = doc.getString("idItem") ?: ""
                    val timestamp = obterTimestamp(doc, listOf("dataEmprestimo", "dataAluguel"))
                    val desc = when (status) {
                        "devolvido" -> "Devolvido no balcão"
                        "recusado" -> "Aluguel recusado"
                        "cancelado" -> "Aluguel cancelada"
                        "expirado" -> "Reserva expirada"
                        else -> "Histórico"
                    }
                    val dateStr = sdfDisplay.format(java.util.Date(timestamp))
                    list.add(HistoryItem(titulo, desc, dateStr, "JOGO", R.drawable.casino_24, timestamp, itemId))
                }
                checkAndPublish()
            }
            .addOnFailureListener {
                checkAndPublish()
            }

        // 3. Salas
        db.collection("agendamentos")
            .whereEqualTo("idUsuario", uid)
            .whereIn("status", listOf("expirado", "concluido", "finalizado", "cancelado"))
            .get()
            .addOnSuccessListener { result ->
                result.forEach { doc ->
                    val sala = doc.getString("nomeSala") ?: "Sala/Mesa"
                    val status = doc.getString("status") ?: ""
                    val idSala = doc.getString("idSala") ?: ""
                    val dataStr = doc.getString("data") ?: ""
                    var timestamp = System.currentTimeMillis()
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
                            // Keep default
                        }
                    }
                    val desc = when (status) {
                        "concluido", "finalizado" -> "Uso finalizado"
                        "expirado" -> "Agendamento expirado"
                        "cancelado" -> "Agendamento cancelado"
                        else -> "Histórico"
                    }
                    list.add(HistoryItem(sala, desc, dateStr, "SALA", R.drawable.calendar_clock_24, timestamp, idSala))
                }
                checkAndPublish()
            }
            .addOnFailureListener {
                checkAndPublish()
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
