package com.example.bibliotecaunifor.admin.acervo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
            startActivity(Intent(this, AdminCriarLivroActivity::class.java))
        }

        binding.btnExcluir.setOnClickListener {
            // Exclude logic
            finish()
        }
    }
}
