package com.example.bibliotecaunifor.admin.acervo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.crud.Exemplar
import com.example.bibliotecaunifor.databinding.ItemExemplarEditBinding

class AdminExemplarEditAdapter(
    private val exemplares: MutableList<Exemplar> = mutableListOf()
) : RecyclerView.Adapter<AdminExemplarEditAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemExemplarEditBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExemplarEditBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exemplar = exemplares[position]
        
        with(holder.binding) {
            tvExemplarTitulo.text = "Exemplar ${position + 1}"
            
            etRegistro.setText(exemplar.registro)
            etEdicaoExemplar.setText(exemplar.edicao)
            etAno.setText(if (exemplar.ano == 0) "" else exemplar.ano.toString())
            etSuporte.setText(exemplar.suporte)
            etLocalizacao.setText(exemplar.localizacao)
            etSituacao.setText(exemplar.situacao)
            etSituacao.isEnabled = false // Evita edicao manual e erros de digitacao

            etRegistro.doAfterTextChanged { exemplares[position] = exemplares[position].copy(registro = it.toString()) }
            etEdicaoExemplar.doAfterTextChanged { exemplares[position] = exemplares[position].copy(edicao = it.toString()) }
            etAno.doAfterTextChanged { exemplares[position] = exemplares[position].copy(ano = it.toString().toIntOrNull() ?: 0) }
            etSuporte.doAfterTextChanged { exemplares[position] = exemplares[position].copy(suporte = it.toString()) }
            etLocalizacao.doAfterTextChanged { exemplares[position] = exemplares[position].copy(localizacao = it.toString()) }
            etSituacao.doAfterTextChanged { exemplares[position] = exemplares[position].copy(situacao = it.toString()) }

            btnExcluirExemplar.setOnClickListener {
                if (position != RecyclerView.NO_POSITION) {
                    val ex = exemplares[position]
                    if (ex.situacao.equals("Alugado", ignoreCase = true)) {
                        com.google.android.material.dialog.MaterialAlertDialogBuilder(holder.itemView.context)
                            .setTitle("Remoção Negada")
                            .setMessage("Não é possível remover este exemplar porque ele está atualmente alugado por um aluno.")
                            .setPositiveButton("Ok", null)
                            .show()
                        return@setOnClickListener
                    }
                    exemplares.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, exemplares.size)
                }
            }
        }
    }

    override fun getItemCount(): Int = exemplares.size

    fun adicionarExemplar() {
        exemplares.add(Exemplar(situacao = "Disponivel"))
        notifyItemInserted(exemplares.size - 1)
    }

    fun setExemplares(novosExemplares: List<Exemplar>) {
        exemplares.clear()
        exemplares.addAll(novosExemplares)
        notifyDataSetChanged()
    }

    fun getExemplares(): List<Exemplar> = exemplares
}
