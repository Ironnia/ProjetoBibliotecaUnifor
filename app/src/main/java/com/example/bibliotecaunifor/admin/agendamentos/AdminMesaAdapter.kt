package com.example.bibliotecaunifor.admin.agendamentos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.ItemAdminMesaBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class AdminMesaAdapter(
    private val items: List<AdminMesa>,
    private val onGerenciarHorarios: (AdminMesa) -> Unit
) : RecyclerView.Adapter<AdminMesaAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminMesaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminMesaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvMesaNome.text = "${item.nome} - ${item.andar}"
            tvMesaStatus.text = item.statusTexto

            // Indicador verde/vermelho
            ivStatus.setBackgroundResource(
                if (item.isOcupada) R.drawable.bg_circle_red else R.drawable.bg_circle_green
            )

            // Toggle ações ao clicar Opções
            layoutAcoes.visibility = View.GONE
            btnOpcoes.setOnClickListener {
                val isVisible = layoutAcoes.visibility == View.VISIBLE
                layoutAcoes.visibility = if (isVisible) View.GONE else View.VISIBLE
            }

            // Botão Liberar
            btnLiberar.setOnClickListener {
                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Liberar Mesa")
                    .setMessage("Deseja liberar a ${item.nome}?")
                    .setPositiveButton("Confirmar") { _, _ ->
                        Snackbar.make(root, "${item.nome} liberada com sucesso!", Snackbar.LENGTH_SHORT).show()
                        layoutAcoes.visibility = View.GONE
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            // Botão Gerenciar Horários
            btnGerenciarHorarios.setOnClickListener {
                onGerenciarHorarios(item)
            }
        }
    }

    override fun getItemCount() = items.size
}
