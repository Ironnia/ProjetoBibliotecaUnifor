package com.example.bibliotecaunifor.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.bibliotecaunifor.databinding.CadastroBinding
import com.example.bibliotecaunifor.mostrarAviso
import com.example.bibliotecaunifor.mostrarToast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CadastroActivity : AppCompatActivity() {
    private lateinit var binding: CadastroBinding
    // https://firebase.google.com/docs/auth/android/start?hl=pt-br#check_current_auth_state
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editTextNome.doOnTextChanged { _, _, _, _ ->
            binding.layoutNome.error = null
            binding.layoutNome.isErrorEnabled = false
        }
        binding.editTextEmail.doOnTextChanged { _, _, _, _ ->
            binding.layoutEmail.error = null
            binding.layoutEmail.isErrorEnabled = false
        }
        binding.editTextSenha.doOnTextChanged { _, _, _, _ ->
            binding.layoutSenha.error = null
            binding.layoutSenha.isErrorEnabled = false
        }
        binding.editTextRepetirSenha.doOnTextChanged { _, _, _, _ ->
            binding.layoutRepetirSenha.error = null
            binding.layoutRepetirSenha.isErrorEnabled = false
        }

        binding.buttonCadastrar.setOnClickListener {
            val nome = binding.editTextNome.text.toString()
            val email = binding.editTextEmail.text.toString()
            val senha = binding.editTextSenha.text.toString()
            val repetir = binding.editTextRepetirSenha.text.toString()

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || repetir.isEmpty()) {
                mostrarToast("Preencha todos os campos!")
                return@setOnClickListener
            }
            
            if (senha != repetir) {
                binding.layoutRepetirSenha.error = "As senhas não coincidem!"
                return@setOnClickListener
            }

            // Para bloquear vários clicks de requisição ao sistema Auth com feedback visual.
            binding.buttonCadastrar.visibility = android.view.View.INVISIBLE
            binding.progressBar.visibility = android.view.View.VISIBLE

            // https://firebase.google.com/docs/auth/android/start?hl=pt-br#sign_up_new_users
            //
            auth.createUserWithEmailAndPassword(email, senha)
                // usando o .add inve´s de if e else fica mais fácil de ler.
                .addOnSuccessListener { resultado ->
                    val uid = resultado.user!!.uid
                    val usuarioMap = hashMapOf(
                        "nome" to nome,
                        "email" to email,
                        "tipo" to "usuario"
                    )

                    // https://firebase.google.com/docs/firestore/manage-data/add-data?hl=pt-br#add_a_document
                    // "No back-end, .add(...) e .doc().set(...) são equivalentes, então você pode usar qualquer uma das opções." - documentação
                    db.collection("usuario").document(uid).set(usuarioMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Cadastro feito com sucesso!", Toast.LENGTH_LONG).show()
                            binding.buttonCadastrar.visibility = android.view.View.VISIBLE
                            binding.progressBar.visibility = android.view.View.GONE
                            binding.buttonCadastrar.isEnabled = true
                            finish()
                        }
                        .addOnFailureListener {
                            Snackbar.make(binding.root, "Erro ao salvar os dados no banco.", Snackbar.LENGTH_SHORT).show()
                            binding.buttonCadastrar.visibility = android.view.View.VISIBLE
                            binding.progressBar.visibility = android.view.View.GONE
                            binding.buttonCadastrar.isEnabled = true
                        }
                }
                .addOnFailureListener { exception ->
                    binding.apply {
                        buttonCadastrar.visibility = android.view.View.VISIBLE
                        progressBar.visibility = android.view.View.GONE
                        buttonCadastrar.isEnabled = true

                        when (exception) {
                            is FirebaseAuthWeakPasswordException -> {
                                layoutSenha.isErrorEnabled = true
                                layoutSenha.error = "A senha deve conter no mínimo 6 caracteres"
                                editTextSenha.requestFocus()
                            }

                            is FirebaseAuthInvalidCredentialsException -> {
                                layoutEmail.isErrorEnabled = true
                                layoutEmail.error = "Por favor, digite um e-mail válido"
                                editTextEmail.requestFocus()
                            }

                            is FirebaseAuthUserCollisionException -> {
                                layoutEmail.isErrorEnabled = true
                                layoutEmail.error = "Este e-mail já está cadastrado"
                                editTextEmail.requestFocus()
                            }

                            else -> {
                                mostrarAviso("Erro ao criar conta. Verifique os dados.")
                            }
                        }
                    }
                }
        }

        binding.textViewFazerLogin.setOnClickListener {
            finish()
        }
    }
}
