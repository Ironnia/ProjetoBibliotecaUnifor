package com.example.bibliotecaunifor.usuario.salas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.ItemAgendamentoBinding
class AgendamentoAdapter(
    private var lista: List<AgendamentoDb>,
    private val onQrCodeClick: (AgendamentoDb) -> Unit,
    private val onCancelarClick: (AgendamentoDb) -> Unit
) : RecyclerView.Adapter<AgendamentoAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemAgendamentoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(lista[position])
    override fun getItemCount() = lista.size
    inner class ViewHolder(val binding: ItemAgendamentoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AgendamentoDb) {
            binding.tvAgendamentoSala.text = item.nomeSala
            binding.tvAgendamentoData.text = "Data: ${item.data}"
            binding.tvAgendamentoHorario.text = "Horário: ${item.horario}"

            // ver o qr
            when (item.status) {
                "confirmado" -> {
                    binding.root.alpha = 0.6f
                    binding.btnCancelar.visibility = View.GONE
                    binding.root.setOnClickListener(null)
                    binding.tvAgendamentoHorario.text = "Horário: ${item.horario} (Confirmado)"
                    binding.tvAgendamentoHorario.setTextColor(binding.root.context.getColor(android.R.color.darker_gray))
                }
                "expirado" -> {
                    binding.root.alpha = 0.4f
                    binding.btnCancelar.visibility = View.GONE
                    binding.root.setOnClickListener(null)
                    binding.tvAgendamentoHorario.text = "Horário: ${item.horario} (Expirado)"
                    binding.tvAgendamentoHorario.setTextColor(binding.root.context.getColor(R.color.error_red))
                }
                else -> {
                    binding.root.alpha = 1.0f
                    binding.btnCancelar.visibility = View.VISIBLE
                    binding.root.setOnClickListener { onQrCodeClick(item) }
                    binding.tvAgendamentoHorario.text = "Horário: ${item.horario}"
                    binding.tvAgendamentoHorario.setTextColor(binding.root.context.getColor(R.color.unifor_marinho_dark))
                }
            }

            binding.btnCancelar.setOnClickListener { onCancelarClick(item) }
        }
    }
    fun atualizarLista(novaLista: List<AgendamentoDb>) {
        lista = novaLista
        notifyDataSetChanged()
    }
}