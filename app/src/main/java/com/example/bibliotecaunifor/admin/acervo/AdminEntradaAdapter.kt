package com.example.bibliotecaunifor.admin.acervo

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.crud.Entrada
import com.example.bibliotecaunifor.databinding.ItemAdminLivroBinding
import com.example.bibliotecaunifor.crud.excluirEntrada
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminEntradaAdapter(
    private var items: List<Entrada>,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<AdminEntradaAdapter.ViewHolder>() {

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
                val intent = Intent(holder.itemView.context, AdminDetalhesLivroActivity::class.java).apply {
                    putExtra("entrada_id", item.id)
                }
                holder.itemView.context.startActivity(intent)
            }

            btnEditar.setOnClickListener {
                val intent = Intent(holder.itemView.context, AdminCriarLivroActivity::class.java).apply {
                    putExtra("isEdit", true)
                    putExtra("entrada_id", item.id)
                    putExtra("titulo", item.titulo)
                    putExtra("autor", item.autor)
                    putExtra("isbn", item.isbn)
                    putExtra("edicao", item.edicao)
                    putExtra("publicacao", item.publicacao)
                    putExtra("cdu", item.cdu)
                    putExtra("cutter", item.cutter)
                    putStringArrayListExtra("assuntos", ArrayList(item.assuntos))
                }
                holder.itemView.context.startActivity(intent)
            }

            btnExcluir.setOnClickListener {
                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Confirmar Exclusão")
                    .setMessage("Deseja realmente excluir \"${item.titulo}\" do catálogo? Esta ação não pode ser desfeita.")
                    .setPositiveButton("Excluir") { _, _ ->
                        CoroutineScope(Dispatchers.Main).launch {
                            excluirEntrada(item.id)
                            Snackbar.make(holder.binding.root, "Livro removido com sucesso!", Snackbar.LENGTH_SHORT).show()
                            onDataChanged()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<Entrada>) {
        items = newItems
        notifyDataSetChanged()
    }
}
