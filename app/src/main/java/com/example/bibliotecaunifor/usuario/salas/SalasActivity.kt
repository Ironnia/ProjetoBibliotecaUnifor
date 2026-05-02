package com.example.bibliotecaunifor.usuario.salas

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.databinding.TelaSalasBinding
import com.example.bibliotecaunifor.usuario.catalogo.CatalogoActivity
import com.example.bibliotecaunifor.usuario.perfil.PerfilUsuarioActivity

class SalasActivity : AppCompatActivity() {
    private lateinit var binding: TelaSalasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaSalasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.selectedItemId = com.example.bibliotecaunifor.R.id.navigation_salas
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
                com.example.bibliotecaunifor.R.id.navigation_perfil -> {
                    startActivity(Intent(this, PerfilUsuarioActivity::class.java))
                    finish()
                    true
                }
                com.example.bibliotecaunifor.R.id.navigation_salas -> true
                else -> false
            }
        }
    }
}
