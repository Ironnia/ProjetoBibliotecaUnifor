package com.example.bibliotecaunifor.admin.acervo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliotecaunifor.crud.Entrada
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.crud.buscarEntradaPorId
import com.example.bibliotecaunifor.databinding.TelaAdminDetalhesLivroBinding
import com.example.bibliotecaunifor.crud.excluirEntrada
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AdminDetalhesLivroActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminDetalhesLivroBinding
    private var entrada: Entrada? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminDetalhesLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val entradaId = intent.getStringExtra("entrada_id")
        if (entradaId != null) {
            loadEntrada(entradaId)
        }

        binding.includeToolbar.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEditarDados.setOnClickListener {
            entrada?.let {
                val intent = Intent(this, AdminCriarLivroActivity::class.java).apply {
                    putExtra("isEdit", true)
                    putExtra("entrada_id", it.id)
                    putExtra("titulo", it.titulo)
                    putExtra("autor", it.autor)
                    putExtra("isbn", it.isbn)
                    putExtra("edicao", it.edicao)
                    putExtra("publicacao", it.publicacao)
                    putExtra("cdu", it.cdu)
                    putExtra("cutter", it.cutter)
                    putStringArrayListExtra("assuntos", ArrayList(it.assuntos))
                }
                startActivity(intent)
            }
        }

        binding.btnDetalhes.setOnClickListener {
            if (binding.layoutInfoTecnica.visibility == View.VISIBLE) {
                binding.layoutInfoTecnica.visibility = View.GONE
                binding.btnDetalhes.strokeWidth = 1
                binding.btnDetalhes.setBackgroundColor(getColor(android.R.color.transparent))
                binding.btnDetalhes.setTextColor(getColor(R.color.unifor_anil_primary))
            } else {
                binding.layoutInfoTecnica.visibility = View.VISIBLE
                binding.btnDetalhes.strokeWidth = 0
                binding.btnDetalhes.setBackgroundColor(getColor(R.color.unifor_anil_primary))
                binding.btnDetalhes.setTextColor(getColor(android.R.color.white))
            }
        }

        binding.btnExcluir.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Deseja realmente excluir este livro do catálogo? Esta ação não pode ser desfeita.")
                .setPositiveButton("Excluir") { _, _ ->
                    entrada?.let {
                        lifecycleScope.launch {
                            excluirEntrada(it.id)
                            Snackbar.make(binding.root, "Livro removido com sucesso!", Snackbar.LENGTH_SHORT).show()
                            binding.btnExcluir.postDelayed({ finish() }, 1000)
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
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
        binding.tvIsbn.text = entrada.isbn
        binding.tvCduCutter.text = "${entrada.cdu} ${entrada.cutter}"
        binding.tvEdicao.text = entrada.edicao
        binding.tvPublicacao.text = entrada.publicacao

        binding.chipGroupAssuntos.removeAllViews()
        entrada.assuntos.forEach { assunto ->
            val chip = Chip(this)
            chip.text = assunto
            chip.chipStrokeWidth = 2f
            chip.setChipBackgroundColorResource(android.R.color.white)
            binding.chipGroupAssuntos.addView(chip)
        }

        while (binding.tableExemplares.childCount > 1) {
            binding.tableExemplares.removeViewAt(1)
        }

        entrada.exemplares.forEachIndexed { index, exemplar ->
            val row = TableRow(this).apply {
                setBackgroundResource(android.R.color.white)
                val params = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 1, 0, 0)
                layoutParams = params
            }

            val tvInfo = TextView(this).apply {
                text = "Exemplar ${index + 1} - ${exemplar.localizacao}"
                setPadding(24, 24, 24, 24)
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvSituacao = TextView(this).apply {
                text = exemplar.situacao
                setPadding(24, 24, 24, 24)
                setTextColor(if (exemplar.situacao == "Disponivel") getColor(R.color.success_green) else getColor(R.color.error_red))
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }

            row.addView(tvInfo)
            row.addView(tvSituacao)
            binding.tableExemplares.addView(row)
        }
    }
}
