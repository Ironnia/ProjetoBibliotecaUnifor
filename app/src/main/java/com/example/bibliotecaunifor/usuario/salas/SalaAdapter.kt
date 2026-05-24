package com.example.bibliotecaunifor.usuario.salas
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.Sala
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.ItemSalaBinding
class SalaAdapter(
    private var lista: List<Sala>,
    private val onVerHorariosClick: (Sala) -> Unit
) : RecyclerView.Adapter<SalaAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemSalaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(lista[position])
    override fun getItemCount() = lista.size
    inner class ViewHolder(val binding: ItemSalaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Sala) {
            binding.tvSalaNome.text = item.nome
            binding.tvSalaStatus.text = if (item.disponivel) "Disponível" else "Ocupada"

            val cor = if (item.disponivel) R.color.success_green else R.color.error_red
            binding.tvSalaStatus.setTextColor(binding.root.context.getColor(cor))

            binding.btnVerHorarios.isEnabled = item.disponivel
            binding.btnVerHorarios.setOnClickListener { onVerHorariosClick(item) }
        }
    }
    fun atualizarLista(novaLista: List<Sala>) {
        lista = novaLista
        notifyDataSetChanged()
    }
}