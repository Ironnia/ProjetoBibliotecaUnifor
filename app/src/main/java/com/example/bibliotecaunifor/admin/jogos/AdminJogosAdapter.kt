package com.example.bibliotecaunifor.admin.jogos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.crud.AluguelJogo
import com.example.bibliotecaunifor.crud.JogosRepository
import com.example.bibliotecaunifor.databinding.ItemAdminJogoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminJogosAdapter(private var items: List<AluguelJogo>) :
    RecyclerView.Adapter<AdminJogosAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminJogoBinding) : RecyclerView.ViewHolder(binding.root)

    fun updateList(newItems: List<AluguelJogo>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminJogoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        val status = item.status.lowercase()

        with(holder.binding) {
            tvGameTitle.text = item.tituloItem
            tvUserId.text = "Aluno(a): ${item.emailUsuario}"

            // Formatação do período do empréstimo de 2 horas
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dataDevolucaoStr = sdf.format(Date(item.dataDevolucao))
            tvDate.text = "Devolver até: $dataDevolucaoStr"

            // 1. Estilização visual semântica de borda baseado no status e atrasos
            val estaAtrasado = status == "ativo" && System.currentTimeMillis() > item.dataDevolucao

            when {
                status == "pendente" -> {
                    root.strokeColor = Color.parseColor("#FFD54F") // Amarelo (Solicitado)
                }
                estaAtrasado -> {
                    root.strokeColor = Color.parseColor("#EF5350") // Vermelho (Atrasado)
                }
                status == "ativo" -> {
                    root.strokeColor = Color.parseColor("#004AF7") // Azul (Em uso no prazo)
                }
                else -> {
                    root.strokeColor = Color.parseColor("#EEEEEE") // Neutro
                }
            }

            // Diálogo rápido de informações extras
            ivInfo.visibility = View.GONE


            // 2. Configurações contextuais dos botões de ação do ADM
            if (status == "pendente") {
                btnCancelar.visibility = View.VISIBLE
                btnCancelar.text = "Cancelar"
                btnCancelar.setBackgroundColor(Color.parseColor("#EF5350"))
                btnCancelar.setTextColor(Color.WHITE)

                btnCancelar.setOnClickListener {
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Recusar Reserva")
                        .setMessage("Deseja realmente recusar e cancelar a reserva do jogo \"${item.tituloItem}\" para ${item.emailUsuario}?")
                        .setPositiveButton("Confirmar") { _, _ ->
                            CoroutineScope(Dispatchers.Main).launch {
                                JogosRepository.cancelarReservaJogo(item.id, item.idItem)
                                Snackbar.make(root, "Reserva recusada e jogo liberado.", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Voltar", null)
                        .show()
                }

                btnAcao.visibility = View.VISIBLE
                btnAcao.text = "Aprovar"
                btnAcao.setBackgroundColor(Color.parseColor("#4CAF50"))
                btnAcao.setTextColor(Color.WHITE)

                btnAcao.setOnClickListener {
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Aprovar Retirada")
                        .setMessage("Confirma a entrega do jogo \"${item.tituloItem}\" para o aluno ${item.emailUsuario}?")
                        .setPositiveButton("Confirmar") { _, _ ->
                            CoroutineScope(Dispatchers.Main).launch {
                                // 1. Verifica regra RF011 (Limite de 1 Jogo ativo)
                                val jogoAtivo = JogosRepository.verificarJogoAtivoUsuario(item.idUsuario)
                                if (jogoAtivo != null) {
                                    MaterialAlertDialogBuilder(context)
                                        .setTitle("Aprovação Negada")
                                        .setMessage("Este aluno já possui o jogo \"$jogoAtivo\" em uso.")
                                        .setPositiveButton("Ok", null)
                                        .show()
                                } else {
                                    // 2. Procede com a aprovação regular
                                    JogosRepository.aprovarRetiradaJogo(item.id)
                                    Snackbar.make(root, "Retirada de jogo aprovada com sucesso!", Snackbar.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            } else { // status == "ativo" (Em uso)
                btnCancelar.visibility = View.GONE

                btnAcao.visibility = View.VISIBLE
                btnAcao.text = "Devolver"
                btnAcao.setBackgroundColor(Color.parseColor("#004AF7"))
                btnAcao.setTextColor(Color.WHITE)

                btnAcao.setOnClickListener {
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Registrar Devolução")
                        .setMessage("Deseja registrar o retorno do jogo \"${item.tituloItem}\" entregue por ${item.emailUsuario}?")
                        .setPositiveButton("Confirmar") { _, _ ->
                            CoroutineScope(Dispatchers.Main).launch {
                                JogosRepository.registrarDevolucaoJogo(item.id, item.idItem)
                                Snackbar.make(root, "Devolução concluída e jogo reativado no acervo!", Snackbar.LENGTH_SHORT).show()
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
