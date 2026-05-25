package com.example.bibliotecaunifor.usuario.configuracoes

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaTrocarSenhaBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth

import com.example.bibliotecaunifor.mostrarToast
import com.example.bibliotecaunifor.mostrarAviso

class TrocarSenhaActivity : AppCompatActivity() {
    private lateinit var binding: TelaTrocarSenhaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaTrocarSenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnConfirmar.setOnClickListener {


            // NOVA LÓGICA DE SENHA PURA E TRATAMENTO DE ERROS AMIGÁVEL
            val atual = binding.etSenhaAtual.text.toString()
            val nova = binding.etNovaSenha.text.toString()
            val confirmar = binding.etConfirmarSenha.text.toString()

            // 1. Validação de campos vazios
            if (atual.isEmpty() || nova.isEmpty() || confirmar.isEmpty()) {
                mostrarToast("Preencha todos os campos")
                return@setOnClickListener
            }

            // 2. Regra do Firebase: Mínimo 6 caracteres
            if (nova.length < 6) {
                mostrarToast("A nova senha deve ter no mínimo 6 caracteres")
                return@setOnClickListener
            }

            // 3. Verificação de igualdade
            if (nova != confirmar) {
                mostrarToast("As senhas novas não coincidem")
                return@setOnClickListener
            }

            val user = Firebase.auth.currentUser
            val email = user?.email
            if (user != null && email != null) {
                binding.btnConfirmar.isEnabled = false
                
                // 4. Criando a credencial com a senha PURA (sem trim)
                val credential = EmailAuthProvider.getCredential(email, atual)
                user.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            user.updatePassword(nova)
                                .addOnCompleteListener { task ->
                                    binding.btnConfirmar.isEnabled = true
                                    if (task.isSuccessful) {
                                        mostrarToast("Senha alterada com sucesso!")
                                        finish()
                                    } else {
                                        val msg = task.exception?.localizedMessage ?: "Erro ao alterar a senha."
                                        mostrarAviso(msg)
                                    }
                                }
                        } else {
                            binding.btnConfirmar.isEnabled = true
                            
                            // Traduzindo a mensagem do Firebase para português
                            val exception = reauthTask.exception
                            val msg = when {
                                exception?.message?.contains("incorrect", ignoreCase = true) == true ||
                                exception?.message?.contains("invalid", ignoreCase = true) == true -> {
                                    "A senha atual informada está incorreta."
                                }
                                exception?.message?.contains("too-many-requests", ignoreCase = true) == true ||
                                exception?.message?.contains("blocked", ignoreCase = true) == true -> {
                                    "Muitas tentativas incorretas. Acesso bloqueado temporariamente por segurança."
                                }
                                else -> exception?.localizedMessage ?: "Senha atual incorreta."
                            }
                            mostrarAviso(msg)
                        }
                    }
            } else {
                mostrarToast("Usuário não autenticado.")
            }
        }

        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, R.id.navigation_perfil_aluno)
    }
}
