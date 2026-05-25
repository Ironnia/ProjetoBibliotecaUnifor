package com.example.bibliotecaunifor.admin.emprestimos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.crud.Emprestimo
import com.example.bibliotecaunifor.crud.EmprestimosRepository
import com.example.bibliotecaunifor.databinding.ItemAdminEmprestimoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminEmprestimoAdapter(private var items: List<Emprestimo>) :
    RecyclerView.Adapter<AdminEmprestimoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminEmprestimoBinding) : RecyclerView.ViewHolder(binding.root)

    fun updateList(newItems: List<Emprestimo>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminEmprestimoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        val status = item.status.lowercase()

        with(holder.binding) {
            tvBookTitle.text = "${item.tituloLivro} | ${item.autorLivro}"
            tvUserId.text = "Aluno(a): ${item.nomeUsuario} (${item.matriculaUsuario})"
            
            // Formatação de data
            val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dataPrevistaStr = item.dataDevolucaoPrevista?.let { df.format(it) } ?: "--/--/----"
            tvDate.text = "Prazo: $dataPrevistaStr"

            // 1. Estilização dinâmica de borda baseado no status
            val estaAtrasado = item.status.equals("atrasado", ignoreCase = true) || 
                    (item.status.equals("ativo", ignoreCase = true) && item.dataDevolucaoPrevista?.let { Date().after(it) } == true)

            when {
                status == "pendente" -> {
                    root.strokeColor = Color.parseColor("#FFD54F") // Amarelo (Pendente)
                }
                estaAtrasado -> {
                    root.strokeColor = Color.parseColor("#EF5350") // Vermelho (Atrasado)
                }
                status == "ativo" -> {
                    root.strokeColor = Color.parseColor("#004AF7") // Azul (Ativo no prazo)
                }
                else -> {
                    root.strokeColor = Color.parseColor("#EEEEEE") // Cinza neutro
                }
            }

            // Exibe informações de detalhes em Toast
            ivInfo.setOnClickListener {
                val dataInfo = if (status == "pendente") "Aguardando retirada no balcão." else "Em posse do aluno(a) com prazo até $dataPrevistaStr."
                android.widget.Toast.makeText(
                    context,
                    "${item.tituloLivro}\nAluno: ${item.nomeUsuario}\nStatus: ${status.uppercase()}\nInfo: $dataInfo",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }

            // 2. Configuração de botões e ações baseado no status
            if (status == "pendente") {
                btnRenovar.visibility = View.VISIBLE
                btnRenovar.text = "Cancelar"
                btnRenovar.setBackgroundColor(Color.parseColor("#EF5350"))
                btnRenovar.setTextColor(Color.WHITE)
                
                btnRenovar.setOnClickListener {
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Cancelar Solicitação")
                        .setMessage("Deseja realmente cancelar a reserva do livro \"${item.tituloLivro}\" para o aluno ${item.nomeUsuario}?")
                        .setPositiveButton("Confirmar") { _, _ ->
                            CoroutineScope(Dispatchers.Main).launch {
                                EmprestimosRepository.cancelarReserva(item.id)
                                Snackbar.make(root, "Reserva cancelada com sucesso.", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Voltar", null)
                        .show()
                }

                btnAprovar.visibility = View.VISIBLE
                btnAprovar.text = "Aprovar"
                btnAprovar.setBackgroundColor(Color.parseColor("#4CAF50"))
                btnAprovar.setTextColor(Color.WHITE)
                
                btnAprovar.setOnClickListener {
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Aprovar Retirada")
                        .setMessage("Deseja confirmar a entrega do livro \"${item.tituloLivro}\" para o aluno ${item.nomeUsuario} (Exemplar: ${item.idExemplar})?")
                        .setPositiveButton("Confirmar") { _, _ ->
                            CoroutineScope(Dispatchers.Main).launch {
                                EmprestimosRepository.aprovarRetirada(item.id, item.idLivro, item.idExemplar)
                                Snackbar.make(root, "Retirada aprovada com sucesso!", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            } else { // "ativo" ou "atrasado"
                // Ocultamos o botão esquerdo para empréstimos ativos/atrasados no balcão
                btnRenovar.visibility = View.GONE

                btnAprovar.visibility = View.VISIBLE
                btnAprovar.text = "Devolver"
                btnAprovar.setBackgroundColor(Color.parseColor("#004AF7"))
                btnAprovar.setTextColor(Color.WHITE)
                
                btnAprovar.setOnClickListener {
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Confirmar Devolução")
                        .setMessage("Deseja registrar a devolução do livro \"${item.tituloLivro}\" entregue por ${item.nomeUsuario}?")
                        .setPositiveButton("Confirmar") { _, _ ->
                            CoroutineScope(Dispatchers.Main).launch {
                                // Verifica se a devolução está no prazo (ou antes) para conceder pontos de gamificação
                                val noPrazo = item.dataDevolucaoPrevista?.let { !Date().after(it) } ?: true
                                EmprestimosRepository.registrarDevolucao(item.id, item.idLivro, item.idExemplar, item.idUsuario, noPrazo)
                                
                                val feedbackMsg = if (noPrazo) {
                                    "Devolução processada! +10 pontos concedidos ao aluno."
                                } else {
                                    "Devolução processada com sucesso!"
                                }
                                Snackbar.make(root, feedbackMsg, Snackbar.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }
        }
    }

    override fun getItemCount() = items.size
}
