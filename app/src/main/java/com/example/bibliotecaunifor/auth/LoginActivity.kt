package com.example.bibliotecaunifor.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.Usuario
import com.example.bibliotecaunifor.admin.AdminHomeActivity
import com.example.bibliotecaunifor.databinding.Login1Binding
import com.example.bibliotecaunifor.mostrarAviso
import com.example.bibliotecaunifor.mostrarToast
import com.google.android.material.snackbar.Snackbar

// Usando as sintaxe do KTX para simplificar.
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.annotation.meta.When
import androidx.core.content.edit


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


        val prefs = getSharedPreferences("login", MODE_PRIVATE)
        val manterLogado = prefs.getBoolean("lembrar_me", false)

        // refiz na sintaxe de KTX que entendo melhor. A "?" verifica o if null igalmente.
        // O !! não é uma boa prática pelo que li.

        // FIZ UMA TELA SPLASH para isso: SplashActivity.kt
//        auth.currentUser?.let { user ->
//            if (manterLogado) {
//                buscarTipoENavegar(user.uid)
//                return
//            } else {
//                auth.signOut()
//            }
//        }

        // limpar os erros quando o usuario comoça a usar. Precisa ficar dentro do Oncrete para funcionar!
        binding.editTextEmail.doOnTextChanged { _, _, _, _ ->
            binding.layoutEmail.error = null
            // remove o espaço em branco.
            binding.layoutEmail.isErrorEnabled = false
        }
        binding.editTextSenha.doOnTextChanged { _, _, _, _ ->
            binding.layoutSenha.error = null
            // remove o espaço em branco.
            binding.layoutSenha.isErrorEnabled = false
        }

        binding.buttonEntrar.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val senha = binding.editTextSenha.text.toString().trim()



            // verificação de formulario.
//            if (email.isEmpty() || senha.isEmpty()) {
//                mostrarAviso("Preencha e-mail e senha.")
//                binding.layoutSenha.error = "As senhas não coincidem!"
//                // Snackbar.make(binding.root, "Preencha e-mail e senha.", Snackbar.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
            // limpar os erros
//            binding.layoutEmail.error = null
//            binding.layoutSenha.error = null
            when {
                email.isEmpty() && senha.isEmpty() -> {
                    binding.layoutEmail.error = "Preencha o e-mail!"; binding.layoutSenha.error = "Preencha a senha!"; return@setOnClickListener
                }
                email.isEmpty() -> {
                    binding.layoutEmail.error = "Preencha o e-mail!"; return@setOnClickListener
                }
                senha.isEmpty() -> {
                    binding.layoutSenha.error = "Preencha a senha!"; return@setOnClickListener
                }
            }
            // Bloquear para deixar o servidor carregar.
            binding.buttonEntrar.visibility = android.view.View.INVISIBLE
            binding.progressBar.visibility = android.view.View.VISIBLE

            // https://firebase.google.com/docs/auth/android/start?hl=pt-br#sign_in_existing_users
            // mudar para o ktx também.
            auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener { resultado ->
                    // isso vai fazer o botão funcionar.
                    val lembrar = binding.checkBoxLembrar.isChecked
                    val prefs = getSharedPreferences("login", MODE_PRIVATE)
                    prefs.edit { putBoolean("lembrar_me", lembrar) }
                    //buscarTipoENavegar(resultado.user!!.uid)
                    resultado.user?.uid?.let { uid ->
                        buscarTipoENavegar(uid)
                    }
                }
                .addOnFailureListener {
                    binding.buttonEntrar.visibility = android.view.View.VISIBLE
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.buttonEntrar.isEnabled = true
                    mostrarAviso("Usuário(a) ou senha incorretos")
                // Snackbar.make(binding.root, "E-mail ou senha incorretos.", Snackbar.LENGTH_SHORT).show()
                }

        }

        binding.textViewCadastrarAqui.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }

        binding.textViewEsqueciSenha.setOnClickListener {
            startActivity(Intent(this, RecuperarSenhaActivity::class.java))
        }

        // Dentro do onCreate, logo abaixo das outras configurações do binding
        binding.editTextSenha.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                // Isso simula o clique no botão Entrar
                binding.buttonEntrar.performClick()
                true
            } else {
                false
            }
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
