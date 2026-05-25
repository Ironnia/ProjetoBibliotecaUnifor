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

    private var agendamentos: List<AgendamentoDb> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemSalaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(lista[position])
    override fun getItemCount() = lista.size
    
    inner class ViewHolder(val binding: ItemSalaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Sala) {
            binding.tvSalaNome.text = item.nome



            // NOVA LÓGICA COM SALA STATUS HELPER
            val (statusTexto, isLivre) = SalaStatusHelper.calcularStatus(item.id, agendamentos)
            binding.tvSalaStatus.text = statusTexto
            val cor = if (isLivre) R.color.success_green else R.color.error_red
            binding.tvSalaStatus.setTextColor(binding.root.context.getColor(cor))
            
            // Deixa sempre habilitado para ele ver os horários mesmo se estiver ocupado agora
            binding.btnVerHorarios.isEnabled = true



            binding.btnVerHorarios.text = "Ver horários"
            
            binding.btnVerHorarios.setOnClickListener { onVerHorariosClick(item) }
        }
    }



    // NOVAS ASSINATURAS DE ATUALIZAÇÃO
    fun atualizarLista(novaLista: List<Sala>) {
        atualizarLista(novaLista, emptyList())
    }

    fun atualizarLista(novaLista: List<Sala>, novosAgs: List<AgendamentoDb>) {
        lista = novaLista
        agendamentos = novosAgs
        notifyDataSetChanged()
    }
}