package com.example.bibliotecaunifor.admin.acervo

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.crud.Exemplar
import com.example.bibliotecaunifor.databinding.ItemAdminExemplaresBinding

class AdminExemplarAdapter(private var exemplares: List<Exemplar> = emptyList()): RecyclerView.Adapter<AdminExemplarAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminExemplaresBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminExemplaresBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exemplar = exemplares[position]
        holder.binding.nExemplar.text = "Exemplar ${position + 1}"
        setupRow(holder.binding.rowRegistro, holder.binding.tvRegistro, exemplar.registro)
        setupRow(holder.binding.rowEdicao, holder.binding.tvEdicao, exemplar.edicao)
        setupRow(holder.binding.rowAno, holder.binding.tvAno, exemplar.ano.toString())
        setupRow(holder.binding.rowSuporte, holder.binding.tvSuporte, exemplar.suporte)
        setupRow(holder.binding.rowLocalizacao, holder.binding.tvLocalizacao, exemplar.localizacao)
        setupRow(holder.binding.rowSituacao, holder.binding.tvSituacao, exemplar.situacao)
        val situacao = exemplar.situacao
        when {
            situacao.equals("Disponivel", ignoreCase = true) || situacao.equals("Disponível", ignoreCase = true) -> {
                holder.binding.tvSituacao.setTextColor(Color.parseColor("#4CAF50"))
            }
            situacao.equals("Alugado", ignoreCase = true) -> {
                holder.binding.tvSituacao.setTextColor(Color.parseColor("#004AF7"))
            }
            else -> {
                holder.binding.tvSituacao.setTextColor(Color.parseColor("#EF5350"))
            }
        }
    }

    override fun getItemCount(): Int = exemplares.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Exemplar>) {
        this.exemplares = newList
        notifyDataSetChanged()
    }

    private fun setupRow(row: View, textView: android.widget.TextView, value: String?) {
        if (value.isNullOrBlank()) {
            row.visibility = View.GONE
        } else {
            row.visibility = View.VISIBLE
            textView.text = value
        }
    }
}
