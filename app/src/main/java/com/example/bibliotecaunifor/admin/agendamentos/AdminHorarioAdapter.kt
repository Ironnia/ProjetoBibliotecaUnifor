package com.example.bibliotecaunifor.admin.agendamentos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.ItemAdminHorarioBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.isVisible

class AdminHorarioAdapter(private val items: List<AdminHorario>) :
    RecyclerView.Adapter<AdminHorarioAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminHorarioBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminHorarioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvHorario.text = item.horario

            // Indicador de ocupado
            if (item.isOcupado) {
                ivSlotStatus.visibility = View.VISIBLE
                ivSlotStatus.setBackgroundResource(R.drawable.bg_circle_red)
            } else {
                ivSlotStatus.visibility = View.GONE
            }

            // Expandir/recolher ao clicar
            layoutDetalhe.visibility = View.GONE
            tvEmail.text = if (item.isOcupado) "Email: ${item.email}" else "Livre"

            layoutHeader.setOnClickListener {
                val isVisible = layoutDetalhe.isVisible
                layoutDetalhe.visibility = if (isVisible) View.GONE else View.VISIBLE
            }

            // Botão Ocupar
            btnOcupar.setOnClickListener {
                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Ocupar Horário")
                    .setMessage("Marcar ${item.horario} como ocupado?")
                    .setPositiveButton("Confirmar") { _, _ ->
                        Snackbar.make(root, "Horário ${item.horario} ocupado.", Snackbar.LENGTH_SHORT).show()
                        layoutDetalhe.visibility = View.GONE
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            // Botão Liberar
            btnLiberar.setOnClickListener {
                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Liberar Horário")
                    .setMessage("Liberar ${item.horario}?")
                    .setPositiveButton("Confirmar") { _, _ ->
                        Snackbar.make(root, "Horário ${item.horario} liberado!", Snackbar.LENGTH_SHORT).show()
                        layoutDetalhe.visibility = View.GONE
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    override fun getItemCount() = items.size
}
