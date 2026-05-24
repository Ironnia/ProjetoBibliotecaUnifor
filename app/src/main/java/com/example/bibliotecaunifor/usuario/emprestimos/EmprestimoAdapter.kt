package com.example.bibliotecaunifor.usuario.emprestimos
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.Emprestimo
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
    private val onItemClick: (Emprestimo) -> Unit
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
            val data = sdf.format(Date(item.dataDevolucao))

            val isPendente = item.status == "pendente"
            // tem que ver se funciona, é só ideal de UX: É isso ou o de baixo.
//            val cor = when (item.status) {
//                "pendente" -> R.color.unifor_anil_primary
//                "atrasado" -> R.color.error_red
//                else -> R.color.unifor_marinho_dark
//            }

            with(binding) {
                tvLivroTitulo.text = item.tituloItem
                tvLivroAutor.text = item.autorItem

                val corAlerta = when (item.status) {
                    "pendente" -> R.color.unifor_anil_primary
                    "atrasado" -> R.color.error_red
                    else -> R.color.unifor_marinho_dark
                }

                tvPrazo.text = if (isPendente) "Retirar até: $data" else "Prazo: $data"
                tvPrazo.setTextColor(root.context.getColor(corAlerta))

                btnAcaoLivro.isEnabled = true
                btnAcaoLivro.alpha = 1.0f
                btnAcaoLivro.text = if (isPendente) "Ver QR Code" else "Renovar"

                // vai desabilitar a renovação por causa do aluno ter atrasado.
                if (item.status == "atrasado") {
                    btnAcaoLivro.isEnabled = false
                    btnAcaoLivro.text = "Procure a biblioteca"
                    btnAcaoLivro.alpha = 0.5f
                }
//                val cor = if (isPendente) R.color.unifor_anil_primary else R.color.unifor_marinho_dark
//                tvPrazo.setTextColor(root.context.getColor(cor))

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