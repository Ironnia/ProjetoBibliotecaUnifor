package com.example.bibliotecaunifor.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.admin.AdminHomeActivity
import com.example.bibliotecaunifor.databinding.Login1Binding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: Login1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = Login1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonEntrar.setOnClickListener {
            val email = binding.editTextMatricula.text.toString()
            val senha = binding.editTextSenha.text.toString()

            if (email.isBlank() || senha.isBlank()) {
                Toast.makeText(this, "Usuário(a) ou senha incorretos", Toast.LENGTH_SHORT).show()
            } else if (email == "admin" && senha == "admin") {
                startActivity(Intent(this, AdminHomeActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        binding.textViewCadastrarAqui.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }

        binding.textViewEsqueciSenha.setOnClickListener {
            startActivity(Intent(this, RecuperarSenhaActivity::class.java))
        }
    }
}
