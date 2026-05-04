package com.example.bibliotecaunifor.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.databinding.RecuperarsenhaBinding

class RecuperarSenhaActivity : AppCompatActivity() {
    private lateinit var binding: RecuperarsenhaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        binding = RecuperarsenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonEnviarLink.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            if (email.isNotBlank()) {
                val emailCensurado = email.take(3) + "***" + email.substringAfter("@", "")
                AlertDialog.Builder(this)
                    .setTitle("Recuperação solicitada")
                    .setMessage("Enviado um email de redefinição de senha para $emailCensurado")
                    .setPositiveButton("Voltar ao Login") { _, _ ->
                        finish()
                    }
                    .show()
            } else {
                Toast.makeText(this, "Por favor, insira um e-mail", Toast.LENGTH_SHORT).show()
            }
        }

        binding.textViewFazerLogin.setOnClickListener {
            finish()
        }
    }
}
