package com.example.bibliotecaunifor.usuario.reserva

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.databinding.TelaDetalhesLivroBinding

class DetalhesLivroActivity : AppCompatActivity() {
    private lateinit var binding: TelaDetalhesLivroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaDetalhesLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("title") ?: "Sem título"
        val author = intent.getStringExtra("author") ?: "Desconhecido"
        val available = intent.getIntExtra("available", 0)

        binding.tvTitle.text = title
        binding.tvAuthor.text = author

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnReservar.setOnClickListener {
            if (available > 0) {
                AlertDialog.Builder(this)
                    .setTitle("Reservar $title?")
                    .setMessage("Prazo para retirada: 23:59:59")
                    .setPositiveButton("Confirmar") { _, _ ->
                        AlertDialog.Builder(this)
                            .setTitle("O livro \"$title\" foi reservado")
                            .setMessage("Retirar até amanhã às 09:00\nDevolver em 7 dias.")
                            .setPositiveButton("Fechar", null)
                            .show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Indisponível")
                    .setMessage("No momento este livro não está disponível!")
                    .setPositiveButton("Voltar", null)
                    .show()
            }
        }
    }
}
