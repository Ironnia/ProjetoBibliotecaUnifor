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
            // val atual = binding.etSenhaAtual.text.toString()
            // val nova = binding.etNovaSenha.text.toString()
            // val confirmar = binding.etConfirmarSenha.text.toString()
            val atual = binding.etSenhaAtual.text.toString().trim()
            val nova = binding.etNovaSenha.text.toString().trim()
            val confirmar = binding.etConfirmarSenha.text.toString().trim()

            if (atual.isEmpty() || nova.isEmpty() || confirmar.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (nova != confirmar) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = Firebase.auth.currentUser
            val email = user?.email
            if (user != null && email != null) {
                binding.btnConfirmar.isEnabled = false
                val credential = EmailAuthProvider.getCredential(email, atual)
                user.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            user.updatePassword(nova)
                                .addOnCompleteListener { task ->
                                    binding.btnConfirmar.isEnabled = true
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        val msg = task.exception?.localizedMessage ?: "Erro ao alterar a senha."
                                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            binding.btnConfirmar.isEnabled = true
                            val msg = reauthTask.exception?.localizedMessage ?: "Senha atual incorreta."
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            }
        }

        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, R.id.navigation_perfil_aluno)
    }
}
