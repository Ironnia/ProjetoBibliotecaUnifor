package com.example.bibliotecaunifor.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.bibliotecaunifor.databinding.RecuperarsenhaBinding
import com.example.bibliotecaunifor.mostrarToast
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RecuperarSenhaActivity : AppCompatActivity() {
    private lateinit var binding: RecuperarsenhaBinding
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        binding = RecuperarsenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editTextEmail.doOnTextChanged { _, _, _, _ ->
            binding.layoutEmail.error = null
            // remove o espaço em branco.
            binding.layoutEmail.isErrorEnabled = false
        }

        binding.buttonEnviarLink.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            if (email.isNotBlank()) {
                binding.buttonEnviarLink.visibility = android.view.View.INVISIBLE
                binding.progressBar.visibility = android.view.View.VISIBLE

                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        binding.buttonEnviarLink.visibility = android.view.View.VISIBLE
                        binding.progressBar.visibility = android.view.View.GONE
                        val emailCensurado = email.take(3) + "***" + email.substringAfter("@", "")
                        AlertDialog.Builder(this)
                            .setTitle("Recuperação solicitada")
                            .setMessage("Enviado um email de redefinição de senha para $emailCensurado")
                            .setPositiveButton("Voltar ao Login") { _, _ ->
                                finish()
                            }
                            .setCancelable(false) // Não permite sair do alerta clicando fora dele.
                            .show() // mostra na dela
                    }
                    .addOnFailureListener { exception ->
                        binding.apply {
                            buttonEnviarLink.visibility = android.view.View.VISIBLE
                            progressBar.visibility = android.view.View.GONE

                            when (exception) {
                                is FirebaseAuthInvalidCredentialsException -> {
                                    layoutEmail.isErrorEnabled = true
                                    layoutEmail.error = "Por favor, digite um e-mail válido"
                                    editTextEmail.requestFocus()
                                }
                                else -> {
                                    // Só funciona para problemas de verdade do Firebase. Por questões de hack descobrindo quais email tinham cadastro pela resposta. Todos agora dão Sucesso.
                                    mostrarToast("Erro ao enviar e-mail de recuperação")
                                }
                            }
                        }
                    }
            } else {
                binding.layoutEmail.error = "Por favor, insira um e-mail"


            }

        }
        binding.textViewFazerLogin.setOnClickListener {
            finish()
        }
    }
}