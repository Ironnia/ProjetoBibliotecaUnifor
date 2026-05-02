package com.example.bibliotecaunifor

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.databinding.TelaHomeUsuarioBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: TelaHomeUsuarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaHomeUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.selectedItemId = com.example.bibliotecaunifor.R.id.navigation_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.bibliotecaunifor.R.id.navigation_home -> true
                com.example.bibliotecaunifor.R.id.navigation_catalogo -> {
                    startActivity(android.content.Intent(this, com.example.bibliotecaunifor.usuario.catalogo.CatalogoActivity::class.java))
                    true
                }
                com.example.bibliotecaunifor.R.id.navigation_salas -> {
                    startActivity(android.content.Intent(this, com.example.bibliotecaunifor.usuario.salas.SalasActivity::class.java))
                    true
                }
                com.example.bibliotecaunifor.R.id.navigation_perfil -> {
                    startActivity(android.content.Intent(this, com.example.bibliotecaunifor.usuario.perfil.PerfilUsuarioActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}