package com.example.bibliotecaunifor.usuario.jogos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.usuario.jogos.Jogo
import com.example.bibliotecaunifor.databinding.ItemJogoBinding

class JogoAdapter(
    private var lista: List<Jogo>,
    private var ehModoMinhaLista: Boolean = false, // isso precisa vim antes do click
    private val onAcaoClick: (Jogo) -> Unit
) : RecyclerView.Adapter<JogoAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemJogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(lista[position])

    override fun getItemCount() = lista.size

    inner class ViewHolder(val binding: ItemJogoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Jogo) {
            binding.tvJogoNome.text = item.nome
            binding.tvJogoDesc.text = "${item.jogadores} | ${item.tempoMinutos} min"

            // Lógica de disponibilidade
            if (ehModoMinhaLista) {
                // Se estou na aba "Meus Aluguéis"
                binding.btnAcaoJogo.text = "Devolver"
                binding.btnAcaoJogo.isEnabled = true
                binding.btnAcaoJogo.alpha = 1f
            } else {
                // Se estou na aba "Disponíveis"
                val disponivel = item.disponivel
                binding.btnAcaoJogo.text = if (disponivel) "Reservar" else "Ocupado"
                binding.btnAcaoJogo.isEnabled = disponivel
                binding.btnAcaoJogo.alpha = if (disponivel) 1f else 0.5f
            }
//            val disponivel = item.disponível
//            binding.btnAcaoJogo.text = if (disponivel) "Reservar" else "Ocupado"
//            binding.btnAcaoJogo.isEnabled = disponivel

            binding.btnAcaoJogo.setOnClickListener { onAcaoClick(item) }
        }
    }

    fun atualizarLista(novaLista: List<Jogo>, modo: Boolean = false) {
        this.lista = novaLista
        this.ehModoMinhaLista = modo // Salva o modo atual
        notifyDataSetChanged()
    }
}