package com.example.bibliotecaunifor.usuario.perfil

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.databinding.TelaPerfilUsuarioBinding
import com.example.bibliotecaunifor.usuario.catalogo.CatalogoActivity

class PerfilUsuarioActivity : AppCompatActivity() {
    private lateinit var binding: TelaPerfilUsuarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaPerfilUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.selectedItemId = com.example.bibliotecaunifor.R.id.navigation_perfil
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.bibliotecaunifor.R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                com.example.bibliotecaunifor.R.id.navigation_catalogo -> {
                    startActivity(Intent(this, CatalogoActivity::class.java))
                    finish()
                    true
                }
                com.example.bibliotecaunifor.R.id.navigation_perfil -> true
                // TODO: Salas
                else -> false
            }
        }
    }
}
