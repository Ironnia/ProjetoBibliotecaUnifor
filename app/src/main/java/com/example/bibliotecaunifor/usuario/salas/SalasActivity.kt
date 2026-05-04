package com.example.bibliotecaunifor.usuario.salas

import android.content.Intent
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
import com.example.bibliotecaunifor.databinding.TelaSalasBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.android.material.button.MaterialButton

class SalasActivity : AppCompatActivity() {
    private lateinit var binding: TelaSalasBinding
    private var showingMeusAgendamentos = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaSalasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        setupRecyclerView()

        binding.btnMeusAgendamentos.setOnClickListener {
            showingMeusAgendamentos = !showingMeusAgendamentos
            if (showingMeusAgendamentos) {
                binding.tvTitle.text = "Meus Agendamentos"
                binding.btnMeusAgendamentos.text = "Agendar Sala"
                
                // Atualizar chips para filtros de agendamentos
                binding.chipDisponivel.text = "Em breve"
                binding.chipOcupadas.text = "Já ocorrida"
                binding.chipTodas.text = "Todos"
                binding.chipTodas.isChecked = true
                
                setupMeusAgendamentosRecyclerView()
            } else {
                binding.tvTitle.text = "Agendar sala de estudos"
                binding.btnMeusAgendamentos.text = "Meus Agendamentos"
                
                // Voltar chips para filtros de salas
                binding.chipDisponivel.text = "Disponível"
                binding.chipOcupadas.text = "Ocupadas"
                binding.chipTodas.text = "Todas as salas"
                binding.chipTodas.isChecked = true
                
                setupRecyclerView()
            }
        }

        NavigationUtils.setupBottomNavigation(this, binding.bottomNavigation, R.id.navigation_salas)
    }

    private fun setupRecyclerView() {
        val salas = listOf(
            Sala("Estudo individual 2", "Disponível", true),
            Sala("Sala Reunião", "Disponível", true),
            Sala("Estudo individual 3", "Disponível", true),
            Sala("Estudo Coletivo 4", "Ocupada", false),
            Sala("Estudo Individual 5", "Disponível", true)
        )

        binding.rvSalas.layoutManager = LinearLayoutManager(this)
        binding.rvSalas.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            inner class SalaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val nome: TextView = view.findViewById(R.id.tvSalaNome)
                val status: TextView = view.findViewById(R.id.tvSalaStatus)
                val btn: MaterialButton = view.findViewById(R.id.btnVerHorarios)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sala, parent, false)
                return SalaViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val sala = salas[position]
                (holder as SalaViewHolder).nome.text = sala.nome
                holder.status.text = sala.status
                holder.status.setTextColor(if (sala.disponivel) getColor(R.color.success_green) else getColor(R.color.error_red))
                holder.btn.isEnabled = sala.disponivel
                holder.btn.setOnClickListener { showHorariosDialog(sala.nome) }
            }

            override fun getItemCount() = salas.size
        }
    }

    private fun setupMeusAgendamentosRecyclerView() {
        val agendamentos = listOf(
            Agendamento("Sala Reunião", "15/04", "09:00 - 11:30"),
            Agendamento("Estudo Individual", "01/04", "09:00 - 11:00")
        )

        binding.rvSalas.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            inner class AgendamentoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val nome: TextView = view.findViewById(R.id.tvAgendamentoSala)
                val data: TextView = view.findViewById(R.id.tvAgendamentoData)
                val horario: TextView = view.findViewById(R.id.tvAgendamentoHorario)
                val btn: MaterialButton = view.findViewById(R.id.btnCancelar)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_agendamento, parent, false)
                return AgendamentoViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val ag = agendamentos[position]
                (holder as AgendamentoViewHolder).nome.text = ag.sala
                holder.data.text = "Data: ${ag.data}"
                holder.horario.text = "Horário: ${ag.horario}"
                holder.btn.setOnClickListener { showCancelDialog(ag) }
            }

            override fun getItemCount() = agendamentos.size
        }
    }

    private fun showHorariosDialog(salaNome: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sala_horarios, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.tv_dialog_title).text = "Selecionar horário\n($salaNome)"

        val slotClickListener = View.OnClickListener { v ->
            val horario = (v as MaterialButton).text.toString()
            dialog.dismiss()
            showSuccessDialog(salaNome, "15/04", horario)
        }

        dialogView.findViewById<MaterialButton>(R.id.btn_slot_1).setOnClickListener(slotClickListener)
        dialogView.findViewById<MaterialButton>(R.id.btn_slot_2).setOnClickListener(slotClickListener)
        dialogView.findViewById<MaterialButton>(R.id.btn_slot_3).setOnClickListener(slotClickListener)
        dialogView.findViewById<MaterialButton>(R.id.btn_slot_4).setOnClickListener(slotClickListener)

        dialogView.findViewById<MaterialButton>(R.id.btn_personalizar).setOnClickListener {
            dialog.dismiss()
            showCalendarDialog(salaNome)
        }

        dialog.show()
    }

    private fun showCalendarDialog(salaNome: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sala_calendario, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<MaterialButton>(R.id.btn_confirmar_reserva).setOnClickListener {
            val inicio = dialogView.findViewById<TextView>(R.id.et_hora_inicio).text.toString()
            val fim = dialogView.findViewById<TextView>(R.id.et_hora_fim).text.toString()
            dialog.dismiss()
            showSuccessDialog(salaNome, "15/04", "$inicio - $fim")
        }

        dialog.show()
    }

    private fun showSuccessDialog(sala: String, data: String, horario: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sala_sucesso, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.tv_success_data).text = "Data: $data"
        dialogView.findViewById<TextView>(R.id.tv_success_horario).text = "Horário: $horario"
        
        dialogView.findViewById<MaterialButton>(R.id.btn_success_voltar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCancelDialog(ag: Agendamento) {
        AlertDialog.Builder(this)
            .setTitle("ATENÇÃO!")
            .setMessage("Você deseja cancelar o agendamento?\n\nSala: ${ag.sala}\nData: ${ag.data}\nHorário: ${ag.horario}")
            .setPositiveButton("Sim, Cancelar") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Agendamento cancelado")
                    .setMessage("O agendamento para a sala \"${ag.sala}\" foi removido com sucesso.\n\nData: ${ag.data}\nHorário: ${ag.horario}")
                    .setPositiveButton("Ok", null)
                    .show()
            }
            .setNegativeButton("Voltar", null)
            .show()
    }

    data class Sala(val nome: String, val status: String, val disponivel: Boolean)
    data class Agendamento(val sala: String, val data: String, val horario: String)
}
