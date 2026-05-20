package com.example.bibliotecaunifor.admin.acervo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaAdminDetalhesLivroBinding

class AdminDetalhesLivroActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminDetalhesLivroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminDetalhesLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includeToolbar.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEditarDados.setOnClickListener {
            val intent = Intent(this, AdminCriarLivroActivity::class.java)
            intent.putExtra("isEdit", true)
            startActivity(intent)
        }

        binding.btnDetalhes.setOnClickListener {
            if (binding.layoutInfoTecnica.visibility == android.view.View.VISIBLE) {
                binding.layoutInfoTecnica.visibility = android.view.View.GONE
                binding.btnDetalhes.strokeWidth = 1
                binding.btnDetalhes.setBackgroundColor(getColor(android.R.color.transparent))
                binding.btnDetalhes.setTextColor(getColor(R.color.unifor_anil_primary))
            } else {
                binding.layoutInfoTecnica.visibility = android.view.View.VISIBLE
                binding.btnDetalhes.strokeWidth = 0
                binding.btnDetalhes.setBackgroundColor(getColor(R.color.unifor_anil_primary))
                binding.btnDetalhes.setTextColor(getColor(android.R.color.white))
            }
        }

        binding.btnExcluir.setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Deseja realmente excluir este livro do catálogo? Esta ação não pode ser desfeita.")
                .setPositiveButton("Excluir") { _, _ ->
                    com.google.android.material.snackbar.Snackbar.make(binding.root, "Livro removido com sucesso!", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                    binding.btnExcluir.postDelayed({ finish() }, 1000)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}
