package com.example.bibliotecaunifor.admin.agendamentos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.Sala
import com.example.bibliotecaunifor.databinding.DialogAdminGerenciarHorariosBinding
import com.example.bibliotecaunifor.databinding.TelaAdminAgendamentosBinding
import com.example.bibliotecaunifor.usuario.salas.AgendamentoDb
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.LocalTime

class AdminAgendamentosActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminAgendamentosBinding
    
    private lateinit var mesaAdapter: AdminMesaAdapter

    private var allSalas = listOf<Sala>()
    private var allAgendamentos = listOf<AgendamentoDb>()

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminAgendamentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includeToolbar.btnBack.setOnClickListener { finish() }

        // Configuração da navegação do ADM
        NavigationUtils.navegacaoAdmin(this, binding.bottomNavigation, R.id.navigation_salas_admin)

        setupRecyclerViews()
        setupRealtimeListeners()
        setupFilters()
    }

    private fun setupRecyclerViews() {
        // RecyclerView de Mesas/Salas (Única aba restante)
        mesaAdapter = AdminMesaAdapter(listOf(), listOf()) { mesa ->
            showGerenciarHorariosDialog(mesa)
        }
        binding.recyclerMesas.layoutManager = LinearLayoutManager(this)
        binding.recyclerMesas.adapter = mesaAdapter
    }

    private fun setupRealtimeListeners() {
        // 1. Escuta em tempo real nas Salas/Mesas
        db.collection("salas").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                allSalas = snapshot.toObjects(Sala::class.java)
                updateUI()
            }
        }

        // 2. Escuta em tempo real nos Agendamentos com AUTO-CLEANUP (tolerância de 15 minutos)
        db.collection("agendamentos").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                val listaBruta = snapshot.toObjects(AgendamentoDb::class.java)
                
                // --- Lógica de Varredura Automática (Tolerância 15 min) ---
                val dataHoje = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())
                val agora = LocalTime.now()
                
                listaBruta.forEach { ag ->
                    if ((ag.status == "reservado" || ag.status == "pendente") && ag.data == dataHoje) {
                        try {
                            val horaInicio = LocalTime.parse(ag.horario.split(" - ")[0])
                            if (agora.isAfter(horaInicio.plusMinutes(15))) {
                                // Se atrasou mais de 15 min, o sistema deleta silenciosamente para liberar a mesa/sala
                                db.collection("agendamentos").document(ag.id).delete()
                            }
                        } catch (e: Exception) { /* ignora erro de parse */ }
                    }
                }
                // ----------------------------------------------------------

                allAgendamentos = listaBruta
                updateUI()
            }
        }
    }

    private fun setupFilters() {
        // Caixa de busca textual para mesas/salas
        binding.etSearch.addTextChangedListener {
            updateUI()
        }
    }

    private fun updateUI() {
        val query = binding.etSearch.text.toString().trim().lowercase()

        // Filtra todas as salas (agora incluindo também as que eram da cordelteca) 
        // e mesas por busca textual
        val mesasFiltradas = allSalas.filter { sala ->
            query.isEmpty() || sala.nome.lowercase().contains(query)
        }
        mesaAdapter.updateList(mesasFiltradas, allAgendamentos)
    }

    private fun showGerenciarHorariosDialog(mesa: Sala) {
        val dialogBinding = DialogAdminGerenciarHorariosBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvTituloDialog.text = "Gerenciar Horários - ${mesa.nome}"

        val dataHoje = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())
        val slots = listOf(
            "07:10 - 08:00", "08:00 - 08:50", "08:50 - 09:40", "09:40 - 10:30",
            "10:40 - 11:30", "11:30 - 12:20", "12:20 - 13:10", "13:10 - 14:00",
            "14:00 - 14:50", "14:50 - 15:40", "15:40 - 16:30", "16:30 - 17:20",
            "17:20 - 18:10", "18:10 - 19:00", "19:00 - 19:50", "19:50 - 20:40",
            "20:40 - 21:30"
        )

        // Mapeia slots baseando-se nos agendamentos reais da mesa
        val horariosPopup = slots.map { slot ->
            val ag = allAgendamentos.firstOrNull { 
                it.idSala == mesa.id && it.data == dataHoje && it.horario == slot && it.status == "reservado"
            }
            AdminHorario(
                horario = slot,
                isOcupado = ag != null,
                email = ag?.nomeUsuario ?: "",
                idAgendamento = ag?.id,
                idSala = mesa.id,
                nomeSala = mesa.nome,
                data = dataHoje
            )
        }

        dialogBinding.recyclerHorariosPopup.layoutManager = LinearLayoutManager(this)
        dialogBinding.recyclerHorariosPopup.adapter = AdminHorarioPopupAdapter(horariosPopup) {
            // Callback disparado ao ocupar ou liberar: o Firestore reativo atualiza a lista automaticamente,
            // mas podemos fechar o BottomSheet para fluidez da UX
            dialog.dismiss()
        }

        dialog.show()
    }
}
