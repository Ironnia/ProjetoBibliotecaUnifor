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
import com.example.bibliotecaunifor.mostrarAviso
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

        val entradaId = intent.getStringExtra("idLivro") ?: intent.getStringExtra("entrada_id")
        if (entradaId != null) {
            loadEntrada(entradaId)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnDetails.setOnClickListener {
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
        with(binding) {
            tvTitle.text = entrada.titulo
            tvAuthor.text = entrada.autor

            // Mostrar botão somente quando há exemplares disponíveis
            btnReservar.visibility = if (entrada.exemplaresDisponiveis > 0)
                android.view.View.VISIBLE
            else
                android.view.View.GONE

            // Mudança para usar apenas ícones
            ivBookCover.setImageResource(R.drawable.ic_livro_do_biblioteca)
            ivBookCover.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.unifor_anil_primary))

            cgAssuntos.removeAllViews()
            entrada.assuntos.forEach { assunto ->
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
        val uid = Firebase.auth.currentUser?.uid ?: return

        db.collection("emprestimos")
            .whereEqualTo("idUsuario", uid)
            .whereEqualTo("tipoItem", "livro")
            .whereIn("status", listOf("pendente", "ativo"))
            .get()
            .addOnSuccessListener { result ->
                if (result.size() >= 5) {
                    mostrarAviso("Você atingiu o limite de 5 livros. Devolva um antes de reservar outro.")
                    return@addOnSuccessListener
                }
                abrirDialogoConfirmacao(entry, uid)
            }
            .addOnFailureListener {
                abrirDialogoConfirmacao(entry, uid)
            }
    }

    private fun abrirDialogoConfirmacao(entry: Entrada, uid: String) {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        val retiradaDate = java.text.SimpleDateFormat("dd/MM/yyyy").format(calendar.time)
        val calendarDevolucao = java.util.Calendar.getInstance()
        calendarDevolucao.add(java.util.Calendar.DAY_OF_YEAR, 15)
        val devolucaoDate = java.text.SimpleDateFormat("dd/MM/yyyy").format(calendarDevolucao.time)
        val devolucaoMillis = calendarDevolucao.timeInMillis

        val dialog = AlertDialog.Builder(this)
            .setTitle("Confirmar Reserva")
            .setMessage("Título: ${entry.titulo}\nAutor: ${entry.autor}\nISBN: ${entry.isbn}\n\nPrazo para retirada: $retiradaDate às 21:00")
            .setPositiveButton("Confirmar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                button.isEnabled = false 
                com.example.bibliotecaunifor.pegarNomeUsuario { nomeUsuario ->
                    db.runTransaction { transaction ->
                        val livroRef = db.collection("Acervo").document(entry.id)
                        val snapshot = transaction.get(livroRef)
                        val entradaDb = snapshot.toObject(com.example.bibliotecaunifor.crud.Entrada::class.java)
                            ?: return@runTransaction null

                        val exemplar = entradaDb.exemplares.firstOrNull { it.situacao == "Disponivel" }

                        if (exemplar != null) {
                            val novosExemplares = entradaDb.exemplares.map { ex ->
                                if (ex.registro == exemplar.registro) ex.copy(situacao = "Reservado")
                                else ex
                            }

                            val aluguelRef = db.collection("emprestimos").document()
                            val novoAluguel = hashMapOf(
                                "idUsuario" to uid,
                                "nomeUsuario" to nomeUsuario,
                                "idLivro" to entry.id,
                                "tituloLivro" to entry.titulo,
                                "autorLivro" to entry.autor,
                                "idExemplar" to exemplar.registro,
                                "dataEmprestimo" to System.currentTimeMillis(),
                                "dataDevolucaoPrevista" to java.util.Date(devolucaoMillis),
                                "status" to "pendente",
                                "tipoItem" to "livro"
                            )

                            transaction.set(aluguelRef, novoAluguel)
                            transaction.update(livroRef, "exemplares", novosExemplares)
                            true
                        } else {
                            false
                        }
                    }.addOnSuccessListener { sucesso ->
                        dialog.dismiss()
                        if (sucesso == true) {
                            exibirSucessoReserva(entry, retiradaDate, devolucaoDate)
                            loadEntrada(entry.id) 
                        } else {
                            mostrarToast("Infelizmente o livro acabou de ficar indisponível.")
                        }
                    }.addOnFailureListener {
                        button.isEnabled = true
                        mostrarToast("Erro ao processar reserva. Tente novamente.")
                    }
                }
            }
        }
        dialog.show()
    }

    private fun exibirSucessoReserva(entry: Entrada, retiradaDate: String, devolucaoDate: String) {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 32, 32, 32)

            val iv = android.widget.ImageView(context).apply {
                setImageResource(R.drawable.ic_qrcode)
                layoutParams = LinearLayout.LayoutParams(500, 500)
            }
            val tv = TextView(context).apply {
                text = "(Apresente no balcão)"
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(0, 16, 0, 0)
            }
            addView(iv)
            addView(tv)
        }

        mostrarDialogo(
            titulo = "Solicitação Enviada!",
            mensagem = "Sua reserva para \"${entry.titulo}\" foi enviada com sucesso.\n\n" +
                    "📅 Retire no balcão até: $retiradaDate às 21:00\n" + 
                    "⏳ Prazo de devolução: $devolucaoDate",
            layoutCustomizado = dialogView
        )
    }
}
