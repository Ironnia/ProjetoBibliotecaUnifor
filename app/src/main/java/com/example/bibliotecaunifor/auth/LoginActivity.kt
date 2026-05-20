package com.example.bibliotecaunifor.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.admin.AdminHomeActivity
import com.example.bibliotecaunifor.databinding.Login1Binding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: Login1Binding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = Login1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Se já estava logado, não precisa logar de novo
        if (auth.currentUser != null) {
            buscarTipoENavegar(auth.currentUser!!.uid)
            return
        }

        binding.buttonEntrar.setOnClickListener {
            val email = binding.editTextMatricula.text.toString().trim()
            val senha = binding.editTextSenha.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Snackbar.make(binding.root, "Preencha e-mail e senha.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener { resultado ->
                    buscarTipoENavegar(resultado.user!!.uid)
                }
                .addOnFailureListener {
                    Snackbar.make(binding.root, "E-mail ou senha incorretos.", Snackbar.LENGTH_SHORT).show()
                }
        }

        binding.textViewCadastrarAqui.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }

        binding.textViewEsqueciSenha.setOnClickListener {
            startActivity(Intent(this, RecuperarSenhaActivity::class.java))
        }
    }

    private fun buscarTipoENavegar(uid: String) {
        db.collection("usuario").document(uid).get()
            .addOnSuccessListener { documento ->
                val tipo = documento.getString("tipo") ?: "usuario"
                val destino = if (tipo == "admin") AdminHomeActivity::class.java else MainActivity::class.java
                startActivity(Intent(this, destino))
                finish()
            }
            .addOnFailureListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }
}