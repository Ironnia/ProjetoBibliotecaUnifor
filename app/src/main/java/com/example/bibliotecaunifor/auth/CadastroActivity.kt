package com.example.bibliotecaunifor.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.databinding.CadastroBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CadastroActivity : AppCompatActivity() {
    private lateinit var binding: CadastroBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonCadastrar.setOnClickListener {
            val nome = binding.editTextNome.text.toString()
            val email = binding.editTextEmail.text.toString()
            val senha = binding.editTextSenha.text.toString()
            val repetir = binding.editTextRepetirSenha.text.toString()

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || repetir.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (senha != repetir) {
                binding.layoutRepetirSenha.error = "As senhas não coincidem!"
                return@setOnClickListener
            }

            binding.buttonCadastrar.isEnabled = false

            auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener { resultado ->
                    val uid = resultado.user!!.uid
                    val usuarioMap = hashMapOf(
                        "nome" to nome,
                        "email" to email,
                        "tipo" to "usuario"
                    )

                    db.collection("usuario").document(uid).set(usuarioMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Cadastro feito com sucesso!", Toast.LENGTH_LONG).show()
                            binding.buttonCadastrar.isEnabled = true
                            finish()
                        }
                        .addOnFailureListener {
                            Snackbar.make(binding.root, "Erro ao salvar os dados no banco.", Snackbar.LENGTH_SHORT).show()
                            binding.buttonCadastrar.isEnabled = true
                        }
                }
                .addOnFailureListener { exception ->
                    binding.buttonCadastrar.isEnabled = true
                    when (exception) {
                        is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> {
                            binding.layoutSenha.error = "A senha deve conter no mínimo 6 caracteres"
                            binding.editTextSenha.requestFocus()
                        }
                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                            binding.layoutEmail.error = "Por favor, digite um e-mail válido"
                            binding.editTextEmail.requestFocus()
                        }
                        is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                            binding.layoutEmail.error = "Este e-mail já está cadastrado"
                            binding.editTextEmail.requestFocus()
                        }
                        else -> {
                            Snackbar.make(binding.root, "Erro ao criar conta. Verifique os dados.", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
        }

        binding.textViewFazerLogin.setOnClickListener {
            finish()
        }
    }
}
