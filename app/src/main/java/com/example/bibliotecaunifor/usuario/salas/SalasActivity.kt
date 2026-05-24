package com.example.bibliotecaunifor.usuario.salas

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.Sala
import com.example.bibliotecaunifor.databinding.TelaSalasBinding
import com.example.bibliotecaunifor.mostrarAviso
import com.example.bibliotecaunifor.pegarNomeUsuario
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SalasActivity : AppCompatActivity() {
    private lateinit var binding: TelaSalasBinding
    private var showingMeusAgendamentos = false

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private lateinit var salaAdapter: SalaAdapter
    private lateinit var agendamentoAdapter: AgendamentoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = TelaSalasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAdapters()
        carregarSalas()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnMeusAgendamentos.setOnClickListener {
            showingMeusAgendamentos = !showingMeusAgendamentos
            if (showingMeusAgendamentos) {
                binding.tvTitle.text = "Meus Agendamentos"
                binding.btnMeusAgendamentos.text = "Agendar Sala"
                carregarMeusAgendamentos()
            } else {
                binding.tvTitle.text = "Agendar sala de estudos"
                binding.btnMeusAgendamentos.text = "Meus Agendamentos"
                carregarSalas()
            }
        }

        NavigationUtils.setupBottomNavigation(this, binding.bottomNavigation, R.id.navigation_salas)
    }

    private fun setupAdapters() {
        salaAdapter = SalaAdapter(emptyList()) { sala -> mostrarHorariosSala(sala) }
        agendamentoAdapter = AgendamentoAdapter(emptyList()) { ag -> showCancelDialog(ag) }

        binding.rvSalas.layoutManager = LinearLayoutManager(this)
        binding.rvSalas.adapter = salaAdapter
    }

    private fun carregarSalas() {
        db.collection("salas").get().addOnSuccessListener { result ->
            val lista = result.toObjects<Sala>()
            salaAdapter.atualizarLista(lista)
            binding.rvSalas.adapter = salaAdapter
        }
    }

    private fun carregarMeusAgendamentos() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("agendamentos").whereEqualTo("idUsuario", uid).get()
            .addOnSuccessListener { result ->
                val lista = result.toObjects<AgendamentoDb>()
                agendamentoAdapter.atualizarLista(lista)
                binding.rvSalas.adapter = agendamentoAdapter
            }
    }

    private fun mostrarHorariosSala(sala: Sala) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sala_horarios, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        // Data de hoje como padrão para os slots rápidos
        val dataHoje = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())

        dialogView.findViewById<TextView>(R.id.tv_dialog_title).text = "Selecionar horário\n(${sala.nome})"

        val btnSlot1 = dialogView.findViewById<MaterialButton>(R.id.btn_slot_1)
        val btnSlot2 = dialogView.findViewById<MaterialButton>(R.id.btn_slot_2)
        val btnSlot3 = dialogView.findViewById<MaterialButton>(R.id.btn_slot_3)
        val btnSlot4 = dialogView.findViewById<MaterialButton>(R.id.btn_slot_4)

        val slotClickListener = View.OnClickListener { v ->
            val horario = (v as MaterialButton).text.toString()
            dialog.dismiss()
            verificarDisponibilidadeEReservar(sala, dataHoje, horario)
        }

        btnSlot1.setOnClickListener(slotClickListener)
        btnSlot2.setOnClickListener(slotClickListener)
        btnSlot3.setOnClickListener(slotClickListener)
        btnSlot4.setOnClickListener(slotClickListener)

        // Busca assíncrona no Firestore para desabilitar slots já ocupados hoje
        db.collection("agendamentos")
            .whereEqualTo("idSala", sala.id)
            .whereEqualTo("data", dataHoje)
            .whereIn("status", listOf("pendente", "reservado"))
            .get()
            .addOnSuccessListener { result ->
                val horariosOcupados = result.documents.mapNotNull { it.getString("horario") }
                if (horariosOcupados.contains(btnSlot1.text.toString())) {
                    btnSlot1.isEnabled = false
                    btnSlot1.alpha = 0.4f
                }
                if (horariosOcupados.contains(btnSlot2.text.toString())) {
                    btnSlot2.isEnabled = false
                    btnSlot2.alpha = 0.4f
                }
                if (horariosOcupados.contains(btnSlot3.text.toString())) {
                    btnSlot3.isEnabled = false
                    btnSlot3.alpha = 0.4f
                }
                if (horariosOcupados.contains(btnSlot4.text.toString())) {
                    btnSlot4.isEnabled = false
                    btnSlot4.alpha = 0.4f
                }
            }

        dialogView.findViewById<MaterialButton>(R.id.btn_personalizar).setOnClickListener {
            dialog.dismiss()
            showCalendarDialog(sala)
        }
        dialog.show()
    }

    private fun showTimePicker(textView: TextView) {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)

        android.app.TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            textView.text = String.format(java.util.Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
        }, hour, minute, true).show()
    }

    private fun showCalendarDialog(sala: Sala) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sala_calendario, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val calendarView = dialogView.findViewById<CalendarView>(R.id.calendar_view)
        var dataSelecionada = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(calendarView.date))

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            dataSelecionada = String.format("%02d/%02d", dayOfMonth, month + 1)
        }

        val etHoraInicio = dialogView.findViewById<TextView>(R.id.et_hora_inicio)
        val etHoraFim = dialogView.findViewById<TextView>(R.id.et_hora_fim)

        etHoraInicio.setOnClickListener {
            showTimePicker(etHoraInicio)
        }

        etHoraFim.setOnClickListener {
            showTimePicker(etHoraFim)
        }

        dialogView.findViewById<MaterialButton>(R.id.btn_confirmar_reserva).setOnClickListener {
            val inicio = etHoraInicio.text.toString()
            val fim = etHoraFim.text.toString()
            if (fim <= inicio) {
                mostrarAviso("O horário de término deve ser maior que o de início.")
                return@setOnClickListener
            }
            dialog.dismiss()
            verificarDisponibilidadeEReservar(sala, dataSelecionada, "$inicio - $fim")
        }
        dialog.show()
    }

    private fun verificarDisponibilidadeEReservar(sala: Sala, data: String, horario: String) {
        db.collection("agendamentos")
            .whereEqualTo("idSala", sala.id)
            .whereEqualTo("data", data)
            .whereEqualTo("horario", horario)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    mostrarAviso("Este horário já foi reservado por outro aluno.")
                } else {
                    salvarAgendamento(sala, data, horario)
                }
            }
    }

    private fun salvarAgendamento(sala: Sala, data: String, horario: String) {
        val uid = auth.currentUser?.uid ?: return
        pegarNomeUsuario { nome ->
            val novoAgendamento = hashMapOf(
                "idUsuario" to uid,
                "nomeUsuario" to nome,
                "idSala" to sala.id,
                "nomeSala" to sala.nome,
                "data" to data,
                "horario" to horario,
                "status" to "pendente"
            )
            db.collection("agendamentos").add(novoAgendamento).addOnSuccessListener {
                showSuccessDialog(sala.nome, data, horario)
                if (showingMeusAgendamentos) carregarMeusAgendamentos() else carregarSalas()
            }
        }
    }

    private fun showSuccessDialog(salaNome: String, data: String, horario: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sala_sucesso, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialogView.findViewById<TextView>(R.id.tv_success_data).text = "Data: $data"
        dialogView.findViewById<TextView>(R.id.tv_success_horario).text = "Horário: $horario"
        dialogView.findViewById<MaterialButton>(R.id.btn_success_voltar).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showCancelDialog(ag: AgendamentoDb) {
        AlertDialog.Builder(this)
            .setTitle("ATENÇÃO!")
            .setMessage("Deseja cancelar a reserva?\nSala: ${ag.nomeSala}\nData: ${ag.data}")
            .setPositiveButton("Sim, Cancelar") { _, _ ->
                db.collection("agendamentos").document(ag.id).delete().addOnSuccessListener {
                    mostrarAviso("Cancelado com sucesso.")
                    carregarMeusAgendamentos()
                }
            }
            .setNegativeButton("Voltar", null).show()
    }
}