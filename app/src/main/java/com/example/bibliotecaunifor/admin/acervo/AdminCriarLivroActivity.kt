package com.example.bibliotecaunifor.admin.acervo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.databinding.TelaAdminEditarLivroBinding
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class AdminCriarLivroActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminEditarLivroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminEditarLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isEdit = intent.getBooleanExtra("isEdit", false)

        // Preenche ou limpa campos baseado no modo
        if (isEdit) {
            binding.etIsbn.setText("9788567097091")
            binding.etNome.setText("Dom Casmurro")
            binding.etAutor.setText("Machado de Assis")
            binding.etEdicao.setText("2ª reimpressão")
            binding.etPublicacao.setText("São Paulo : Via Leitura, 2019")
            binding.etCdu.setText("869.0(81)-31")
            binding.etCutter.setText("A848d")
            // Assuntos pré-existentes
            adicionarChipAssunto("Romance")
            adicionarChipAssunto("Literatura Brasileira")
        } else {
            binding.etIsbn.setText("")
            binding.etNome.setText("")
            binding.etAutor.setText("")
            binding.etEdicao.setText("")
            binding.etPublicacao.setText("")
            binding.etCdu.setText("")
            binding.etCutter.setText("")
        }

        // Botão voltar
        binding.btnBack.setOnClickListener { finish() }

        // Botão cancelar
        binding.btnCancelar.setOnClickListener { finish() }

        // Botão adicionar assunto
        binding.btnAddAssunto.setOnClickListener {
            val texto = binding.etNovoAssunto.text.toString().trim()
            if (texto.isNotEmpty()) {
                adicionarChipAssunto(texto)
                binding.etNovoAssunto.setText("")
            }
        }

        // Botão concluir
        binding.btnConcluir.setOnClickListener {
            val message = if (isEdit) "Livro atualizado com sucesso!" else "Livro cadastrado com sucesso!"
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            binding.btnConcluir.postDelayed({ finish() }, 1000)
        }
    }

    private fun adicionarChipAssunto(texto: String) {
        val chip = Chip(this)
        chip.text = texto
        chip.isCloseIconVisible = true
        chip.setChipBackgroundColorResource(android.R.color.white)
        chip.chipStrokeWidth = 2f
        chip.setOnCloseIconClickListener {
            binding.chipGroupAssuntos.removeView(chip)
        }
        binding.chipGroupAssuntos.addView(chip)
    }
}
