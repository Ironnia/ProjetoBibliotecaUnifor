package com.example.bibliotecaunifor.admin.agendamentos

import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.Sala
import com.example.bibliotecaunifor.crud.SalasRepository
import com.example.bibliotecaunifor.databinding.DialogAdminGerenciarHorariosBinding
import com.example.bibliotecaunifor.databinding.TelaAdminAgendamentosBinding
import com.example.bibliotecaunifor.usuario.salas.AgendamentoDb
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.time.LocalTime

class AdminAgendamentosActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminAgendamentosBinding
    
    private lateinit var mesaAdapter: AdminMesaAdapter
    private lateinit var horarioAdapter: AdminHorarioAdapter

    private var allSalas = listOf<Sala>()
    private var allAgendamentos = listOf<AgendamentoDb>()

    private val db = Firebase.firestore
    
    // Controladores de Calendário
    private var mesAtual = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var diaSelecionado = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

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
        // RecyclerView de Mesas (Aba Estações)
        mesaAdapter = AdminMesaAdapter(listOf(), listOf()) { mesa ->
            showGerenciarHorariosDialog(mesa)
        }
        binding.recyclerMesas.layoutManager = LinearLayoutManager(this)
        binding.recyclerMesas.adapter = mesaAdapter

        // RecyclerView de Horários da Sala (Aba Cordelteca)
        horarioAdapter = AdminHorarioAdapter(listOf())
        binding.recyclerHorarios.layoutManager = LinearLayoutManager(this)
        binding.recyclerHorarios.adapter = horarioAdapter
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

        // Troca de abas via chips
        binding.chipEstacoes.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.viewFlipper.displayedChild = 0
                updateUI()
            }
        }

        binding.chipCordelteca.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.viewFlipper.displayedChild = 1
                updateUI()
            }
        }
    }

    private fun updateUI() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        val isEstacoes = binding.chipEstacoes.isChecked

        if (isEstacoes) {
            // Filtra mesas por busca textual
            val mesasFiltradas = allSalas.filter { sala ->
                sala.tipo == "mesa" && (query.isEmpty() || sala.nome.lowercase().contains(query))
            }
            mesaAdapter.updateList(mesasFiltradas, allAgendamentos)
        } else {
            // Atualiza calendário e horários de salas da Cordelteca
            setupCalendario()
            mostrarHorarios(diaSelecionado)
        }
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

    private fun setupCalendario() {
        val grid = binding.gridCalendario
        grid.removeAllViews()

        // Cabeçalho dos dias da semana
        val diasSemana = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
        for (dia in diasSemana) {
            val header = android.widget.TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = dpToPx(36)
            params.height = dpToPx(20)
            params.setMargins(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(4))
            header.layoutParams = params
            header.text = dia
            header.textSize = 10f
            header.gravity = android.view.Gravity.CENTER
            header.setTextColor(getColor(R.color.unifor_marinho_dark))
            header.setTypeface(null, android.graphics.Typeface.BOLD)
            grid.addView(header)
        }

        // Dias do Mês Dinâmicos (Calculados por calendário real)
        val maxDias = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        for (diaNumero in 1..maxDias) {
            val dataStr = String.format("%02d/%02d", diaNumero, mesAtual)
            
            // Um dia é considerado "Ocupado" se possuir agendamentos ativos na coleção "agendamentos" para salas
            val isOcupado = allAgendamentos.any { ag ->
                ag.data == dataStr && ag.status == "reservado" && allSalas.any { it.id == ag.idSala && it.tipo == "sala" }
            }

            val circulo = android.widget.TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = dpToPx(36)
            params.height = dpToPx(36)
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            circulo.layoutParams = params
            circulo.text = diaNumero.toString()
            circulo.textSize = 13f
            circulo.gravity = android.view.Gravity.CENTER
            circulo.setTextColor(getColor(R.color.white))
            circulo.setTypeface(null, android.graphics.Typeface.BOLD)

            // Bolinha verde (Livre) ou vermelha (Ocupada)
            circulo.setBackgroundResource(
                if (isOcupado) R.drawable.bg_circle_red else R.drawable.bg_circle_green
            )

            // Destaque visual leve para o dia selecionado atualmente
            if (diaNumero == diaSelecionado) {
                circulo.alpha = 1.0f
                circulo.elevation = 6f
            } else {
                circulo.alpha = 0.85f
                circulo.elevation = 0f
            }

            circulo.setOnClickListener {
                diaSelecionado = diaNumero
                updateUI()
            }

            grid.addView(circulo)
        }
    }

    private fun mostrarHorarios(diaNumero: Int) {
        val query = binding.etSearch.text.toString().trim().lowercase()
        binding.tvHorariosTitulo.visibility = View.VISIBLE
        binding.tvHorariosTitulo.text = "Horários — Dia ${String.format("%02d/%02d", diaNumero, mesAtual)}"
        binding.recyclerHorarios.visibility = View.VISIBLE

        val salasEstudo = allSalas.filter { sala ->
            sala.tipo == "sala" && (query.isEmpty() || sala.nome.lowercase().contains(query))
        }

        // Slots padrão da Cordelteca/Salas de estudos
        val slots = listOf("08:00 - 09:30", "10:00 - 11:30", "14:00 - 15:30", "16:00 - 17:30")
        val dataSelecionada = String.format("%02d/%02d", diaNumero, mesAtual)
        val listHorarios = mutableListOf<AdminHorario>()

        for (sala in salasEstudo) {
            for (slot in slots) {
                val ag = allAgendamentos.firstOrNull { 
                    it.idSala == sala.id && it.data == dataSelecionada && it.horario == slot && it.status == "reservado"
                }
                listHorarios.add(
                    AdminHorario(
                        horario = "${sala.nome}: $slot",
                        isOcupado = ag != null,
                        email = ag?.nomeUsuario ?: "",
                        idAgendamento = ag?.id,
                        idSala = sala.id,
                        nomeSala = sala.nome,
                        data = dataSelecionada
                    )
                )
            }
        }

        horarioAdapter.updateList(listHorarios)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
