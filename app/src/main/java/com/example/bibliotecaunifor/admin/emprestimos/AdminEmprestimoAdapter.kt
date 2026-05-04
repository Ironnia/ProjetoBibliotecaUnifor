package com.example.bibliotecaunifor.admin.emprestimos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.ItemAdminEmprestimoBinding

class AdminEmprestimoAdapter(private val items: List<AdminEmprestimo>) :
    RecyclerView.Adapter<AdminEmprestimoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminEmprestimoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminEmprestimoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvBookTitle.text = "${item.titulo} | ${item.autor}"
            tvUserId.text = "Matrícula: ${item.matricula}"
            tvDate.text = item.dataHora

            ivInfo.setOnClickListener {
                android.widget.Toast.makeText(
                    holder.itemView.context,
                    "Detalhes: Livro em posse do aluno(a) ${item.matricula}. Sem atrasos pendentes.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }

            if (item.isParaDevolucao) {
                btnRenovar.text = "Renovar"
                btnRenovar.setBackgroundColor(holder.itemView.context.getColor(R.color.success_green))
                btnRenovar.setTextColor(holder.itemView.context.getColor(R.color.unifor_marinho_dark))
                
                btnRenovar.setOnClickListener {
                    com.google.android.material.dialog.MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Confirmar Renovação")
                        .setMessage("Deseja renovar o empréstimo do livro \"${item.titulo}\" para o aluno ${item.matricula}?")
                        .setPositiveButton("Confirmar") { _, _ ->
                            com.google.android.material.snackbar.Snackbar.make(holder.binding.root, "Renovação solicitada com sucesso!", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }

                btnAprovar.text = "Aprovar"
                btnAprovar.setBackgroundColor(holder.itemView.context.getColor(R.color.unifor_anil_primary))
                btnAprovar.setTextColor(holder.itemView.context.getColor(R.color.white))
                
                btnAprovar.setOnClickListener {
                    com.google.android.material.dialog.MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Confirmar Devolução")
                        .setMessage("Deseja confirmar a devolução do livro \"${item.titulo}\"?")
                        .setPositiveButton("Confirmar") { _, _ ->
                            com.google.android.material.snackbar.Snackbar.make(holder.binding.root, "Devolução processada com sucesso!", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            } else {
                btnRenovar.text = "Cancelar"
                btnRenovar.setBackgroundColor(holder.itemView.context.getColor(R.color.error_red))
                btnRenovar.setTextColor(holder.itemView.context.getColor(R.color.white))
                
                btnRenovar.setOnClickListener {
                    com.google.android.material.dialog.MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Cancelar Solicitação")
                        .setMessage("Deseja realmente cancelar esta reserva de retirada?")
                        .setPositiveButton("Confirmar") { _, _ ->
                            com.google.android.material.snackbar.Snackbar.make(holder.binding.root, "Solicitação cancelada.", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Voltar", null)
                        .show()
                }

                btnAprovar.text = "Aprovar"
                btnAprovar.setBackgroundColor(holder.itemView.context.getColor(R.color.success_green))
                btnAprovar.setTextColor(holder.itemView.context.getColor(R.color.unifor_marinho_dark))
                
                btnAprovar.setOnClickListener {
                    com.google.android.material.dialog.MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Aprovar Retirada")
                        .setMessage("Deseja confirmar a entrega do livro para o aluno ${item.matricula}?")
                        .setPositiveButton("Confirmar") { _, _ ->
                            com.google.android.material.snackbar.Snackbar.make(holder.binding.root, "Retirada aprovada com sucesso!", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }
        }
    }

    override fun getItemCount() = items.size
}
