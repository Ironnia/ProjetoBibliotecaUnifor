package com.example.bibliotecaunifor.usuario.salas
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.databinding.ItemAgendamentoBinding
class AgendamentoAdapter(
    private var lista: List<AgendamentoDb>,
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

            binding.btnCancelar.setOnClickListener { onCancelarClick(item) }
        }
    }
    fun atualizarLista(novaLista: List<AgendamentoDb>) {
        lista = novaLista
        notifyDataSetChanged()
    }
}