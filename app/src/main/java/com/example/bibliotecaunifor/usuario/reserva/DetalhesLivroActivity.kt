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

class DetalhesLivroActivity : AppCompatActivity() {
    private lateinit var binding: TelaDetalhesLivroBinding
    private var entrada: Entrada? = null

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
            entrada?.let {
                AlertDialog.Builder(this)
                    .setTitle("Detalhes do Livro")
                    .setMessage("Título: ${it.titulo}\nAutor: ${it.autor}\nISBN: ${it.isbn}\nEdição: ${it.edicao}\nPublicação: ${it.publicacao}\nCDU/Cutter: ${it.cdu} ${it.cutter}")
                    .setPositiveButton("Ok", null)
                    .show()
            }
        }

        binding.btnReservar.setOnClickListener {
            entrada?.let {
                if (it.exemplaresDisponiveis > 0) {
                    confirmarReserva(it)
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("Indisponível")
                        .setMessage("No momento este livro não está disponível!")
                        .setPositiveButton("Voltar", null)
                        .show()
                }
            }
        }

        com.example.bibliotecaunifor.usuario.utils.NavigationUtils.setupBottomNavigation(this, binding.bottomNavigation, R.id.navigation_catalogo)
    }

    private fun loadEntrada(id: String) {
        lifecycleScope.launch {
            entrada = buscarEntradaPorId(id)
            entrada?.let { updateUI(it) }
        }
    }

    private fun updateUI(entrada: Entrada) {
        binding.tvTitle.text = entrada.titulo
        binding.tvAuthor.text = entrada.autor

        binding.cgAssuntos.removeAllViews()
        entrada.assuntos.forEach { assunto ->
            val chip = Chip(this)
            chip.text = assunto
            chip.setTextColor(getColor(R.color.unifor_anil_primary))
            chip.chipStrokeColor = android.content.res.ColorStateList.valueOf(getColor(R.color.unifor_anil_primary))
            binding.cgAssuntos.addView(chip)
        }

        binding.layoutExemplaresData.removeAllViews()
        entrada.exemplares.forEach { exemplar ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 2f
            }

            val tvInfo = TextView(this).apply {
                text = "Exemplar - ${exemplar.localizacao}"
                gravity = Gravity.START
                setPadding(36, 36, 36, 36)
                setBackgroundResource(R.drawable.bg_border)
                textSize = 12f
                setTextColor(getColor(android.R.color.black))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvSituacao = TextView(this).apply {
                text = exemplar.situacao
                gravity = Gravity.CENTER
                setPadding(36, 36, 36, 36)
                setBackgroundResource(R.drawable.bg_border)
                textSize = 12f
                val isDisponivel = exemplar.situacao == "Disponivel"
                setTextColor(if (isDisponivel) getColor(R.color.success_green) else getColor(R.color.error_red))
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            row.addView(tvInfo)
            row.addView(tvSituacao)
            binding.layoutExemplaresData.addView(row)
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
                exibirSucessoReserva(entry, retiradaDate, devolucaoDate)
            }
            .setNegativeButton("Cancelar", null)
            .show()
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

        AlertDialog.Builder(this)
            .setTitle("Reserva Realizada!")
            .setMessage("O livro \"${entry.titulo}\" foi reservado com sucesso.\n\nRetirar até: $retiradaDate às 21:00\nDevolver em: $devolucaoDate")
            .setView(dialogView)
            .setPositiveButton("Fechar", null)
            .show()
    }
}
