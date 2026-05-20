package com.example.bibliotecaunifor.admin.usuarios

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.databinding.ItemAdminUsuarioLivroBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class AdminUsuarioLivroAdapter(private val items: List<AdminUsuarioLivro>) :
    RecyclerView.Adapter<AdminUsuarioLivroAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminUsuarioLivroBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminUsuarioLivroBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvBookTitle.text = "${item.titulo} | ${item.autor}"
            tvExemplarInfo.text = item.exemplarInfo

            ivInfo.setOnClickListener {
                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle(item.titulo)
                    .setMessage("Autor: ${item.autor}\n${item.exemplarInfo}\n\nStatus: Aguardando ação do administrador.")
                    .setPositiveButton("Fechar", null)
                    .show()
            }

            btnEmprestar.setOnClickListener {
                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Confirmar Empréstimo")
                    .setMessage("Deseja confirmar o empréstimo de \"${item.titulo}\" para este aluno?")
                    .setPositiveButton("Confirmar") { _, _ ->
                        Snackbar.make(holder.binding.root, "Empréstimo realizado com sucesso!", Snackbar.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            btnCancelar.setOnClickListener {
                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Cancelar Solicitação")
                    .setMessage("Deseja recusar a solicitação de \"${item.titulo}\"?")
                    .setPositiveButton("Confirmar") { _, _ ->
                        Snackbar.make(holder.binding.root, "Solicitação cancelada.", Snackbar.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Voltar", null)
                    .show()
            }
        }
    }

    override fun getItemCount() = items.size
}
