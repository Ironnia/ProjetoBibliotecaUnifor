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

            // Lógica de disponibilidade
            if (ehModoMinhaLista) {
                // Se estou na aba "Meus Aluguéis"
                binding.tvJogoDesc.text = "${item.jogadores} | ${item.tempoMinutos} min\n(${item.descricao})"
                


                if (item.status == "pendente") {
                    binding.btnAcaoJogo.visibility = android.view.View.VISIBLE
                    binding.btnAcaoJogo.text = "Cancelar"
                    binding.btnAcaoJogo.isEnabled = true
                    binding.btnAcaoJogo.alpha = 1f
                    binding.btnAcaoJogo.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFF44336.toInt())
                    binding.btnAcaoJogo.setTextColor(0xFFFFFFFF.toInt())
                } else if (item.status == "ativo") {
                    binding.btnAcaoJogo.visibility = android.view.View.GONE
                } else {
                    binding.btnAcaoJogo.visibility = android.view.View.VISIBLE
                    binding.btnAcaoJogo.text = "Devolver"
                    binding.btnAcaoJogo.isEnabled = true
                    binding.btnAcaoJogo.alpha = 1f
                }
            } else {
                // Se estou na aba "Disponíveis"
                binding.btnAcaoJogo.visibility = android.view.View.VISIBLE
                binding.tvJogoDesc.text = "${item.jogadores} | ${item.tempoMinutos} min"
                val disponivel = item.disponivel
                binding.btnAcaoJogo.text = if (disponivel) "Reservar" else "Ocupado"
                binding.btnAcaoJogo.isEnabled = disponivel
                binding.btnAcaoJogo.alpha = if (disponivel) 1f else 0.5f
                
                if (disponivel) {
                    binding.btnAcaoJogo.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF4CAF50.toInt())
                    binding.btnAcaoJogo.setTextColor(0xFFFFFFFF.toInt())
                } else {
                    binding.btnAcaoJogo.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE0E0E0.toInt())
                    binding.btnAcaoJogo.setTextColor(0xFF757575.toInt())
                }
            }

            binding.btnAcaoJogo.setOnClickListener { onAcaoClick(item) }
        }
    }

    fun atualizarLista(novaLista: List<Jogo>, modo: Boolean = false) {
        this.lista = novaLista
        this.ehModoMinhaLista = modo // Salva o modo atual
        notifyDataSetChanged()
    }
}