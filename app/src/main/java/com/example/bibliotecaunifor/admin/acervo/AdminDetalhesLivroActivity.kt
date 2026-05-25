package com.example.bibliotecaunifor.admin.acervo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var entradaId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminDetalhesLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        entradaId = intent.getStringExtra("entrada_id")

        binding.includeToolbar.btnBack.setOnClickListener {
            finish()
        }

        binding.exemplares.layoutManager = LinearLayoutManager(this)

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

        binding.btnEditarDados.setOnClickListener {
            entrada?.let {
                val intent = Intent(this, AdminCriarEntradaActivity::class.java).apply {
                    putExtra("isEdit", true)
                    putExtra("entrada_id", it.id)
                }
                startActivity(intent)
            }
        }

        binding.btnExcluir.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Deseja excluir este livro e todos os seus exemplares?")
                .setPositiveButton("Excluir") { _, _ ->
                    entradaId?.let { id ->
                        lifecycleScope.launch {
                            excluirEntrada(id)
                            Snackbar.make(binding.root, "Livro removido com sucesso!", Snackbar.LENGTH_SHORT).show()
                            binding.btnExcluir.postDelayed({ finish() }, 1000)
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        entradaId?.let { loadEntrada(it) }
    }

    private fun loadEntrada(id: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            // Oculta visualização técnica no carregamento
            binding.layoutInfoTecnica.visibility = View.GONE
            binding.btnDetalhes.strokeWidth = 1
            binding.btnDetalhes.setBackgroundColor(getColor(android.R.color.transparent))
            binding.btnDetalhes.setTextColor(getColor(R.color.unifor_anil_primary))

            entrada = buscarEntradaPorId(id)
            entrada?.let { updateUI(it) }
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun updateUI(entrada: Entrada) {
        binding.tvTitle.text = entrada.titulo
        binding.tvAuthor.text = entrada.autor
        setupBox(binding.boxIsbn, binding.tvIsbn, entrada.isbn)
        setupBox(binding.boxCduCutter, binding.tvCduCutter, "${entrada.cdu} ${entrada.cutter}".trim())
        setupBox(binding.boxEdicao, binding.tvEdicao, entrada.edicao)
        setupBox(binding.boxPublicacao, binding.tvPublicacao, entrada.publicacao)

        binding.chipGroupAssuntos.removeAllViews()
        entrada.assuntos.forEach { assunto ->
            val chip = Chip(this)
            chip.text = assunto
            chip.chipStrokeWidth = 2f
            chip.setChipBackgroundColorResource(android.R.color.white)
            binding.chipGroupAssuntos.addView(chip)
        }

        binding.exemplares.adapter = AdminExemplarAdapter(entrada.exemplares)
    }

    private fun setupBox(box: View, textView: android.widget.TextView, value: String?) {
        if (value.isNullOrBlank()) {
            box.visibility = View.GONE
        } else {
            box.visibility = View.VISIBLE
            textView.text = value
        }
    }
}
