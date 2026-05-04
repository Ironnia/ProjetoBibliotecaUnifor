package com.example.bibliotecaunifor.admin.acervo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.ItemAdminLivroBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class AdminLivroAdapter(private val items: List<AdminLivro>) :
    RecyclerView.Adapter<AdminLivroAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminLivroBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminLivroBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvBookTitle.text = "${item.titulo} | ${item.autor}"
            tvCopiesInfo.text = "${item.totalExemplares} exemplares | ${item.exemplaresAlugados} alugados"

            ivInfo.setOnClickListener {
                val intent = android.content.Intent(holder.itemView.context, AdminDetalhesLivroActivity::class.java)
                holder.itemView.context.startActivity(intent)
            }

            btnEditar.setOnClickListener {
                val intent = android.content.Intent(holder.itemView.context, AdminCriarLivroActivity::class.java)
                intent.putExtra("isEdit", true)
                holder.itemView.context.startActivity(intent)
            }

            btnExcluir.setOnClickListener {
                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Confirmar Exclusão")
                    .setMessage("Deseja realmente excluir \"${item.titulo}\" do catálogo? Esta ação não pode ser desfeita.")
                    .setPositiveButton("Excluir") { _, _ ->
                        Snackbar.make(holder.binding.root, "Livro removido com sucesso!", Snackbar.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    override fun getItemCount() = items.size
}
