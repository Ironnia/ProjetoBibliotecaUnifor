package com.example.bibliotecaunifor.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.databinding.CadastroBinding

class CadastroActivity : AppCompatActivity() {
    private lateinit var binding: CadastroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = CadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonCadastrar.setOnClickListener {
            val nome = binding.editTextNome.text.toString()
            val email = binding.editTextEmail.text.toString()
            val senha = binding.editTextSenha.text.toString()
            val repetir = binding.editTextRepetirSenha.text.toString()

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || repetir.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else if (senha != repetir) {
                Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Cadastro feito com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.textViewFazerLogin.setOnClickListener {
            finish()
        }
    }
}
