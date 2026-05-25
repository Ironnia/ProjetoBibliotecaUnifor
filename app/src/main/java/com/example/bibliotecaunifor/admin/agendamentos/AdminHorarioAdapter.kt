package com.example.bibliotecaunifor.admin.agendamentos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.crud.SalasRepository
import com.example.bibliotecaunifor.databinding.ItemAdminHorarioBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminHorarioAdapter(private var items: List<AdminHorario>) :
    RecyclerView.Adapter<AdminHorarioAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminHorarioBinding) : RecyclerView.ViewHolder(binding.root)

    fun updateList(newItems: List<AdminHorario>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminHorarioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        with(holder.binding) {
            tvHorario.text = item.horario

            // Bolinha indicadora
            if (item.isOcupado) {
                ivSlotStatus.visibility = View.VISIBLE
                ivSlotStatus.setBackgroundResource(R.drawable.bg_circle_red)
                tvEmail.text = "Ocupado por: ${item.email}"
                
                // Botões contextuais
                btnOcupar.visibility = View.GONE
                btnLiberar.visibility = View.VISIBLE
            } else {
                ivSlotStatus.visibility = View.GONE
                tvEmail.text = "Livre"
                
                btnOcupar.visibility = View.VISIBLE
                btnLiberar.visibility = View.GONE
            }

            // Exibir/esconder detalhe
            layoutDetalhe.visibility = View.GONE
            layoutHeader.setOnClickListener {
                val isVisible = layoutDetalhe.isVisible
                layoutDetalhe.visibility = if (isVisible) View.GONE else View.VISIBLE
            }

            // Botão Ocupar (Bloqueio ADM)
            btnOcupar.setOnClickListener {
                MaterialAlertDialogBuilder(context)
                    .setTitle("Ocupar Horário")
                    .setMessage("Bloquear o slot \"${item.horario}\" para a sala ${item.nomeSala} no dia ${item.data}?")
                    .setPositiveButton("Confirmar") { _, _ ->
                        CoroutineScope(Dispatchers.Main).launch {
                            SalasRepository.ocuparHorarioADM(item.idSala, item.nomeSala, item.data, item.horario)
                            Snackbar.make(root, "Horário ocupado pelo ADM.", Snackbar.LENGTH_SHORT).show()
                            layoutDetalhe.visibility = View.GONE
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            // Botão Liberar (Cancela agendamento)
            btnLiberar.setOnClickListener {
                MaterialAlertDialogBuilder(context)
                    .setTitle("Liberar Horário")
                    .setMessage("Deseja cancelar o agendamento de \"${item.horario}\"?")
                    .setPositiveButton("Confirmar") { _, _ ->
                        CoroutineScope(Dispatchers.Main).launch {
                            item.idAgendamento?.let { id ->
                                SalasRepository.liberarHorario(id)
                                Snackbar.make(root, "Horário liberado com sucesso!", Snackbar.LENGTH_SHORT).show()
                                layoutDetalhe.visibility = View.GONE
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    override fun getItemCount() = items.size
}
