package com.example.bibliotecaunifor.admin.agendamentos

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.ItemAdminHorarioPopupBinding

class AdminHorarioPopupAdapter(private val items: List<AdminHorario>) :
    RecyclerView.Adapter<AdminHorarioPopupAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminHorarioPopupBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminHorarioPopupBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvHorario.text = item.horario
            
            if (item.isOcupado) {
                ivStatusDot.setBackgroundResource(R.drawable.bg_circle_red)
                btnAcao.text = "Liberar"
                // Light green background, dark green text
                btnAcao.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE8F5E9.toInt())
                btnAcao.setTextColor(0xFF2E7D32.toInt())
            } else {
                ivStatusDot.setBackgroundResource(R.drawable.bg_circle_green)
                btnAcao.text = "Ocupar"
                // Light red background, dark red text
                btnAcao.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFFFEBEE.toInt())
                btnAcao.setTextColor(0xFFC62828.toInt())
            }

            btnAcao.setOnClickListener {
                val action = if (item.isOcupado) "liberado" else "ocupado"
                Toast.makeText(holder.itemView.context, "Horário ${item.horario} $action com sucesso!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = items.size
}
