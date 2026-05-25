package com.example.bibliotecaunifor.usuario.emprestimos
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.crud.Emprestimo
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.ItemLivroEmprestadoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Isos aqui é para a gente conseguir meio que "gerenciar a lista que vem doFirestore.
// fazer mocakdo por enquanto.
class EmprestimoAdapter(
    private var lista: List<Emprestimo>,
    private val onAcaoClick: (Emprestimo) -> Unit,
    private val onItemClick: (Emprestimo) -> Unit,
    private val onCancelarClick: ((Emprestimo) -> Unit)? = null
) : RecyclerView.Adapter<EmprestimoAdapter.ViewHolder>() {

    // vai colocar o espaçõ para os dados.
    override fun onCreateViewHolder(p: ViewGroup, vt: Int) = ViewHolder(
        ItemLivroEmprestadoBinding.inflate(LayoutInflater.from(p.context), p, false)
    )
// configura a posição
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) = holder.bind(lista[pos])
// isso organiza o quanto a tela vai ter de rolamento
    override fun getItemCount() = lista.size

    //
    inner class ViewHolder(val binding: ItemLivroEmprestadoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Emprestimo) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            

            val dataDevolucaoTime = item.dataDevolucaoPrevista?.time ?: 0L
            val data = sdf.format(Date(dataDevolucaoTime))

            val isPendente = item.status == "pendente"

            with(binding) {
                tvLivroTitulo.text = item.tituloLivro
                tvLivroAutor.text = item.autorLivro

                val corAlerta = when (item.status) {
                    "pendente" -> R.color.unifor_anil_primary
                    "atrasado" -> R.color.error_red
                    else -> R.color.unifor_marinho_dark
                }

                tvPrazo.text = if (isPendente) "Retirar até: $data" else "Prazo: $data"
                tvPrazo.setTextColor(root.context.getColor(corAlerta))

                // Configuração de botões condicionados ao status do empréstimo
                if (isPendente) {
                    btnAcaoLivro.visibility = android.view.View.VISIBLE
                    btnAcaoLivro.text = "Ver QR Code"
                    btnAcaoLivro.isEnabled = true
                    btnAcaoLivro.alpha = 1.0f
                    btnCancelarReserva.visibility = android.view.View.VISIBLE
                    btnCancelarReserva.setOnClickListener { onCancelarClick?.invoke(item) }
                } else if (item.status == "atrasado") {
                    btnAcaoLivro.visibility = android.view.View.VISIBLE
                    btnAcaoLivro.text = "Procure a biblioteca"
                    btnAcaoLivro.isEnabled = false
                    btnAcaoLivro.alpha = 0.5f
                    btnCancelarReserva.visibility = android.view.View.GONE
                } else {
                    // Para status "ativo" (alugado no prazo), ocultamos os botões pois a renovação foi expurgada do MVP
                    btnAcaoLivro.visibility = android.view.View.GONE
                    btnCancelarReserva.visibility = android.view.View.GONE
                }

                btnAcaoLivro.setOnClickListener { onAcaoClick(item) }
                root.setOnClickListener { onItemClick(item) }
            }
        }
    }
    fun atualizarLista(novaLista: List<Emprestimo>) {
        lista = novaLista
        notifyDataSetChanged()
    }
}