package com.example.bibliotecaunifor.usuario.salas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.admin.agendamentos.AdminHorario
import com.example.bibliotecaunifor.databinding.ItemAdminHorarioPopupBinding
import java.time.LocalTime

class StudentHorarioPopupAdapter(
    private val items: List<AdminHorario>,
    private val onReserveClicked: (AdminHorario) -> Unit
) : RecyclerView.Adapter<StudentHorarioPopupAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminHorarioPopupBinding) : RecyclerView.ViewHolder(binding.root)

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

            // Verifica se o horário já passou com base no relógio local do dispositivo
            val isPast = try {
                val horaInicioStr = item.horario.split(" - ")[0]
                val horaInicio = LocalTime.parse(horaInicioStr)
                LocalTime.now().isAfter(horaInicio)
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
                ivStatusDot.setBackgroundResource(R.drawable.bg_circle_red)
                btnAcao.text = "Ocupado"
                btnAcao.isEnabled = false
                btnAcao.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE0E0E0.toInt())
                btnAcao.setTextColor(0xFF757575.toInt())
            } else {
                ivStatusDot.setBackgroundResource(R.drawable.bg_circle_green)
                btnAcao.text = "Reservar"
                btnAcao.isEnabled = true
                btnAcao.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE8F5E9.toInt())
                btnAcao.setTextColor(0xFF2E7D32.toInt())
                btnAcao.setOnClickListener {
                    onReserveClicked(item)
                }
            }
        }
    }

    override fun getItemCount() = items.size
}
