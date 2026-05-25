package com.example.bibliotecaunifor.admin.agendamentos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.Sala
import com.example.bibliotecaunifor.crud.SalasRepository
import com.example.bibliotecaunifor.databinding.ItemAdminMesaBinding
import com.example.bibliotecaunifor.usuario.salas.SalaStatusHelper
import com.example.bibliotecaunifor.usuario.salas.AgendamentoDb
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Date
import java.util.Locale

class AdminMesaAdapter(
    private var salas: List<Sala>,
    private var agendamentos: List<AgendamentoDb>,
    private val onGerenciarHorarios: (Sala) -> Unit
) : RecyclerView.Adapter<AdminMesaAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminMesaBinding) : RecyclerView.ViewHolder(binding.root)

    fun updateList(newSalas: List<Sala>, newAgendamentos: List<AgendamentoDb>) {
        salas = newSalas
        agendamentos = newAgendamentos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminMesaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sala = salas[position]
        val context = holder.itemView.context
        val dataHoje = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())



        // NOVA LÓGICA COM SALA STATUS HELPER
        val (statusTexto, isLivre) = SalaStatusHelper.calcularStatus(sala.id, agendamentos)

        with(holder.binding) {
            tvMesaNome.text = sala.nome
            


            tvMesaStatus.text = statusTexto
            ivStatus.setBackgroundResource(
                if (isLivre) R.drawable.bg_circle_green else R.drawable.bg_circle_red
            )

            // Menu de ações recolhido por padrão
            layoutAcoes.visibility = View.GONE
            btnOpcoes.setOnClickListener {
                val isVisible = layoutAcoes.visibility == View.VISIBLE
                layoutAcoes.visibility = if (isVisible) View.GONE else View.VISIBLE
            }

            // Botão para liberar a sessão atual (caso o aluno saia mais cedo) ou limpar o dia
            btnLiberar.setOnClickListener {


                MaterialAlertDialogBuilder(context)
                    .setTitle("Liberar Mesa")
                    .setMessage("Deseja limpar todas as reservas de hoje para esta mesa?")
                    .setPositiveButton("Confirmar") { _, _ ->
                        CoroutineScope(Dispatchers.Main).launch {

                            SalasRepository.liberarMesaCompleta(sala.id, dataHoje)
                            Snackbar.make(root, "Mesa liberada!", Snackbar.LENGTH_SHORT).show()
                            layoutAcoes.visibility = View.GONE
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            // Botão Gerenciar Horários (Abre BottomSheet dialog com todos os slots)
            btnGerenciarHorarios.setOnClickListener {
                onGerenciarHorarios(sala)
            }
        }
    }

    override fun getItemCount() = salas.size

    private fun estaNaFaixa(faixa: String): Boolean {
        val agora = LocalTime.now()
        return try {
            val partes = faixa.split(" - ")
            val inicio = LocalTime.parse(partes[0])
            val fim = LocalTime.parse(partes[1])
            !agora.isBefore(inicio) && !agora.isAfter(fim)
        } catch (e: Exception) {
            false
        }
    }
}
