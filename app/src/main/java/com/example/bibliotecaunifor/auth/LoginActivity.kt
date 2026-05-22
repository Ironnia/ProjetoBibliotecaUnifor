package com.example.bibliotecaunifor.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.Usuario
import com.example.bibliotecaunifor.admin.AdminHomeActivity
import com.example.bibliotecaunifor.databinding.Login1Binding
import com.example.bibliotecaunifor.mostrarAviso
import com.google.android.material.snackbar.Snackbar

// Usando as sintaxe do KTX para simplificar.
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase



class LoginActivity : AppCompatActivity() {

    private lateinit var binding: Login1Binding
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = Login1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Se já estava logado, não precisa logar de novo

        // https://firebase.google.com/docs/auth/android/start?hl=pt-br#check_current_auth_state
        // currentUser retorna se tem algum usuário logado, na lógica do Firebase SDK. Se não tiver vai retornar null.
//        if (auth.currentUser != null) {
//            // !! É seguro aqui por causa da verificação de null acima.
//            buscarTipoENavegar(auth.currentUser!!.uid)
//            return
//

        // refiz na sintaxe de KTX que entendo melhor. A "?" verifica o if null igalmente.
        // O !! não é uma boa prática pelo que li.
        auth.currentUser?.let {user ->
            buscarTipoENavegar(user.uid)
            return
        }

        binding.buttonEntrar.setOnClickListener {
            val email = binding.editTextMatricula.text.toString().trim()
            val senha = binding.editTextSenha.text.toString().trim()

            // verificação de formulario.
            if (email.isEmpty() || senha.isEmpty()) {
                mostrarAviso("Preencha e-mail e senha.")
                // Snackbar.make(binding.root, "Preencha e-mail e senha.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // https://firebase.google.com/docs/auth/android/start?hl=pt-br#sign_in_existing_users
            // mudar para o ktx também.
            auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener { resultado ->
                    //buscarTipoENavegar(resultado.user!!.uid)
                    resultado.user?.uid?.let { uid ->
                        buscarTipoENavegar(uid)
                    }
                }
                .addOnFailureListener {
                    mostrarAviso("E-mail ou senha incorretos.")
                // Snackbar.make(binding.root, "E-mail ou senha incorretos.", Snackbar.LENGTH_SHORT).show()
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
                // val tipo = documento.getString("tipo") ?: "usuario"

                // usando a classe do usuario.
                val usuario = documento.toObject(Usuario::class.java)
                val tipo = usuario?.tipo ?: "usuario"
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
