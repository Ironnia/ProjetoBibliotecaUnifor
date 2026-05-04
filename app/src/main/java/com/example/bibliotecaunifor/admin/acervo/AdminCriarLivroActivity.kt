package com.example.bibliotecaunifor.admin.acervo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.databinding.TelaAdminEditarLivroBinding

class AdminCriarLivroActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminEditarLivroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminEditarLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCancelar.setOnClickListener {
            finish()
        }

        binding.btnConcluir.setOnClickListener {
            // Success logic
            finish()
        }
    }
}
