package com.example.bibliotecaunifor.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.Usuario
import com.example.bibliotecaunifor.admin.AdminHomeActivity
import com.example.bibliotecaunifor.crud.db
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth


// Melhorar a UX tirar a tela de login piscando quando tem "Lembrar-me" ativo
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = Firebase.auth
        val prefs = getSharedPreferences("login", MODE_PRIVATE)
        val manterLogado = prefs.getBoolean("lembrar_me", false)

        val user = auth.currentUser
        if (user != null && manterLogado) {
            // Vai direto para a busca de tipo e navegação
            buscarTipoENavegar(user.uid)
        } else {
            // Vai para a tela de Login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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
