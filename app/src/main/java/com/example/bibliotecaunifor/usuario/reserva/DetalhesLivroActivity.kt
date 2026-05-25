package com.example.bibliotecaunifor.usuario.reserva

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliotecaunifor.crud.Entrada
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.crud.buscarEntradaPorId
import com.example.bibliotecaunifor.databinding.TelaDetalhesLivroBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import androidx.core.view.isVisible
import com.example.bibliotecaunifor.mostrarDialogo
import com.example.bibliotecaunifor.mostrarDialogoSimples
import com.example.bibliotecaunifor.mostrarToast
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DetalhesLivroActivity : AppCompatActivity() {
    private lateinit var binding: TelaDetalhesLivroBinding
    private var entrada: Entrada? = null

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaDetalhesLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val entradaId = intent.getStringExtra("entrada_id")
        if (entradaId != null) {
            loadEntrada(entradaId)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnDetails.setOnClickListener {
//            entrada?.let {
//                AlertDialog.Builder(this)
//                    .setTitle("Detalhes do Livro")
//                    .setMessage("Título: ${it.titulo}\nAutor: ${it.autor}\nISBN: ${it.isbn}\nEdição: ${it.edicao}\nPublicação: ${it.publicacao}\nCDU/Cutter: ${it.cdu} ${it.cutter}")
//                    .setPositiveButton("Ok", null)
//                    .show()
//            }
            // deixando igual do adm como o prof. pediu
        with(binding) {
            val estaVisivel = layoutDetalhesExpansivel.isVisible

            if (estaVisivel) { //vai fechar
                layoutDetalhesExpansivel.visibility = android.view.View.GONE
                btnDetails.text = "Mostrar Detalhes"
            } else {//abre
                entrada?.let {
                    tvDetalhesConteudo.text =
                        "ISBN: ${it.isbn}\nEdição: ${it.edicao}\nPublicação: ${it.publicacao}\nCDU/Cutter: ${it.cdu} ${it.cutter}"
                }
                layoutDetalhesExpansivel.visibility = android.view.View.VISIBLE
                btnDetails.text = "Ocultar Detalhes"
            }
        }

        }

        binding.btnReservar.setOnClickListener {
            entrada?.let {
                if (it.exemplaresDisponiveis > 0) {
                    confirmarReserva(it)
                } else {
                    mostrarDialogoSimples("Indisponível", "No momento este livro não está disponível!", "Voltar")
                }
            }
        }

        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, R.id.navigation_catalogo_aluno)
    }

    private fun loadEntrada(id: String) {
        lifecycleScope.launch {
            entrada = buscarEntradaPorId(id)
            entrada?.let { updateUI(it) }
        }
    }

    private fun updateUI(entrada: Entrada) {
        // dnv ktx,
        with(binding) {
            tvTitle.text = entrada.titulo
            tvAuthor.text = entrada.autor

            btnReservar.visibility = android.view.View.GONE

            if (entrada.imageUrl.isNotEmpty()) {
                try {
                    com.bumptech.glide.Glide.with(this@DetalhesLivroActivity)
                        .load(entrada.imageUrl)
                        .into(ivBookCover)
                    tvBookCoverPlaceholder.visibility = android.view.View.GONE
                } catch (e: Exception) {
                    ivBookCover.setImageDrawable(null)
                    tvBookCoverPlaceholder.visibility = android.view.View.VISIBLE
                }
            } else {
                ivBookCover.setImageDrawable(null)
                tvBookCoverPlaceholder.visibility = android.view.View.VISIBLE
            }

            cgAssuntos.removeAllViews()
            entrada.assuntos.forEach { assunto ->
                // só ktx, aproveitando que estou tentando ler uma lógica melhro.
                val chip = Chip(this@DetalhesLivroActivity).apply {
                    text = assunto
                    setTextColor(getColor(R.color.unifor_anil_primary))
                    chipStrokeColor =
                        android.content.res.ColorStateList.valueOf(getColor(R.color.unifor_anil_primary))
                    isClickable = false
                    isFocusable = false
                }
                cgAssuntos.addView(chip)
            }

            layoutExemplaresData.removeAllViews()
            entrada.exemplares.forEach { exemplar ->
                val row = LinearLayout(this@DetalhesLivroActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    weightSum = 2f
                }

                val tvInfo = TextView(this@DetalhesLivroActivity).apply {
                    text = "Exemplar - ${exemplar.localizacao}"
                    gravity = Gravity.START
                    // Talvez modificar um pouco? verificar!
                    setPadding(36, 36, 36, 36)
                    setBackgroundResource(R.drawable.bg_border)
                    textSize = 12f
                    setTextColor(getColor(android.R.color.black))
                    layoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val tvSituacao = TextView(this@DetalhesLivroActivity).apply {
                    text = exemplar.situacao
                    gravity = Gravity.CENTER
                    setPadding(36, 36, 36, 36)
                    setBackgroundResource(R.drawable.bg_border)
                    textSize = 12f
                    
                    val isDisponivel = exemplar.situacao.equals("Disponivel", ignoreCase = true) || exemplar.situacao.equals("Disponível", ignoreCase = true)
                    val isAlugado = exemplar.situacao.equals("Alugado", ignoreCase = true)
                    if (isDisponivel) {
                        setTextColor(getColor(R.color.success_green))
                    } else if (isAlugado) {
                        setTextColor(getColor(R.color.unifor_anil_primary))
                    } else {
                        setTextColor(getColor(R.color.error_red))
                    }
                    
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                row.addView(tvInfo)
                row.addView(tvSituacao)
                // Melhorar a separação na hud:
                val divisor = android.view.View(this@DetalhesLivroActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    setBackgroundColor(getColor(R.color.unifor_gelo_bg))
                }
                layoutExemplaresData.addView(divisor)
                layoutExemplaresData.addView(row)
            }
        }
    }

    private fun confirmarReserva(entry: Entrada) {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        val retiradaDate = java.text.SimpleDateFormat("dd/MM/yyyy").format(calendar.time)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 14)
        val devolucaoDate = java.text.SimpleDateFormat("dd/MM/yyyy").format(calendar.time)

        AlertDialog.Builder(this)
            .setTitle("Confirmar Reserva")
            .setMessage("Título: ${entry.titulo}\nAutor: ${entry.autor}\nISBN: ${entry.isbn}\n\nDeseja confirmar a reserva deste item?")
            .setPositiveButton("Confirmar") { _, _ ->
                val uid = Firebase.auth.currentUser?.uid ?: return@setPositiveButton
                
                val calendarDevolucao = java.util.Calendar.getInstance()
                calendarDevolucao.add(java.util.Calendar.DAY_OF_YEAR, 15)
                val devolucaoMillis = calendarDevolucao.timeInMillis

                val novoAluguel = hashMapOf(
                    "idUsuario" to uid,
                    "idItem" to entry.id,
                    "tituloItem" to entry.titulo,
                    "autorItem" to entry.autor,
                    "dataEmprestimo" to System.currentTimeMillis(),
                    "dataDevolucao" to devolucaoMillis,
                    "status" to "pendente",
                    "tipoItem" to "livro"
                )
                db.runTransaction { transaction ->
                    val livroRef = db.collection("Acervo").document(entry.id)
                    val snapshot = transaction.get(livroRef)
                    val qtdAtual = snapshot.getLong("exemplaresDisponiveis") ?: 0

                    if (qtdAtual > 0) {
                        val aluguelRef = db.collection("emprestimos").document()
                        transaction.set(aluguelRef, novoAluguel)

                        transaction.update(livroRef, "exemplaresDisponiveis", qtdAtual - 1)
                        true
                    } else {
                        false
                    }
                }.addOnSuccessListener { sucesso ->
                    if (sucesso) {
                        exibirSucessoReserva(entry, retiradaDate, devolucaoDate)                        // atualiza na tela.
                        loadEntrada(entry.id)
                    } else {
                        mostrarToast("Infelizmente o livro acabou de ficar indisponível.")
                    }
                }
                    .addOnFailureListener {
                        mostrarToast("Erro ao processar reserva. Tente novamente.")
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun exibirSucessoReserva(entry: Entrada, retiradaDate: String, devolucaoDate: String) {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 32, 32, 32)

            // Esse aqui é nosso QRCODE! Só sinalizando
            val iv = android.widget.ImageView(context).apply {
                setImageResource(R.drawable.ic_qrcode)
                layoutParams = LinearLayout.LayoutParams(500, 500)
            }
            val tv = TextView(context).apply {
                text = "(Apresente no balcão)"
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(0, 16, 0, 0)
                // setTextColor(getColor(android.R.color.darker_gray))
            }
            addView(iv)
            addView(tv)
        }

//        AlertDialog.Builder(this)
//            .setTitle("Reserva Realizada!")
//            .setMessage("O livro \"${entry.titulo}\" foi reservado com sucesso.\n\nRetirar até: $retiradaDate às 21:00\nDevolver em: $devolucaoDate")
//            .setView(dialogView)
//            .setPositiveButton("Fechar", null)
//            .show()
        mostrarDialogo(
            titulo = "Solicitação Enviada!",
            mensagem = "Sua reserva para \"${entry.titulo}\" foi enviada com sucesso.\n\n" +
                    "📅 Retire no balcão até: $retiradaDate às 21:00\n" + // Mudar isso para icones de emotes (todos os icones de todo o app) de verdade do googlefont!
                    "⏳ Prazo de devolução: $devolucaoDate",
            layoutCustomizado = dialogView // Lembrar de colocaro QRCODE aqui depois.
        )
    }
}
