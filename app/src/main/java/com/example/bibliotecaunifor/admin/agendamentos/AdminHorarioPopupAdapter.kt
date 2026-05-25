package com.example.bibliotecaunifor.admin.agendamentos

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.crud.SalasRepository
import com.example.bibliotecaunifor.databinding.ItemAdminHorarioPopupBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminHorarioPopupAdapter(
    private var items: List<AdminHorario>,
    private val onActionTriggered: () -> Unit // Callback para fechar popup e notificar activity
) : RecyclerView.Adapter<AdminHorarioPopupAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminHorarioPopupBinding) : RecyclerView.ViewHolder(binding.root)

    fun updateList(newItems: List<AdminHorario>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminHorarioPopupBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        with(holder.binding) {
            tvHorario.text = item.horario
            
            val isPast = try {
                val horaInicioStr = item.horario.split(" - ")[0]
                val horaInicio = java.time.LocalTime.parse(horaInicioStr)
                java.time.LocalTime.now().isAfter(horaInicio)
            } catch (e: Exception) {
                false
            }

            if (isPast) {
                ivStatusDot.setBackgroundResource(R.drawable.bg_circle_red)
                btnAcao.text = "Expirado"
                btnAcao.isEnabled = false
                btnAcao.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE0E0E0.toInt())
                btnAcao.setTextColor(0xFF757575.toInt())
            } else if (item.isOcupado) {
                btnAcao.isEnabled = true
                ivStatusDot.setBackgroundResource(R.drawable.bg_circle_red)
                btnAcao.text = "Liberar"
                // Fundo verde claro, texto verde escuro
                btnAcao.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE8F5E9.toInt())
                btnAcao.setTextColor(0xFF2E7D32.toInt())
                btnAcao.setOnClickListener {
                    CoroutineScope(Dispatchers.Main).launch {
                        item.idAgendamento?.let { id ->
                            SalasRepository.liberarHorario(id)
                            Toast.makeText(context, "Mesa liberada para o horário ${item.horario}!", Toast.LENGTH_SHORT).show()
                        }
                        onActionTriggered() // Notifica a Activity para recarregar
                    }
                }
            } else {
                btnAcao.isEnabled = true
                ivStatusDot.setBackgroundResource(R.drawable.bg_circle_green)
                btnAcao.text = "Ocupar"
                // Fundo vermelho claro, texto vermelho escuro
                btnAcao.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFFFEBEE.toInt())
                btnAcao.setTextColor(0xFFC62828.toInt())
                btnAcao.setOnClickListener {
                    CoroutineScope(Dispatchers.Main).launch {
                        SalasRepository.ocuparHorarioADM(item.idSala, item.nomeSala, item.data, item.horario)
                        Toast.makeText(context, "Mesa reservada para o horário ${item.horario}!", Toast.LENGTH_SHORT).show()
                        onActionTriggered() // Notifica a Activity para recarregar
                    }
                }
            }
        }
    }

    override fun getItemCount() = items.size
}
