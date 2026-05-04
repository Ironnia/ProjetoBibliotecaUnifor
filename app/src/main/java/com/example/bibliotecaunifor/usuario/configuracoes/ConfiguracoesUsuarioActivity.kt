package com.example.bibliotecaunifor.usuario.configuracoes

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaConfiguracoesUsuarioBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils

class ConfiguracoesUsuarioActivity : AppCompatActivity() {
    private lateinit var binding: TelaConfiguracoesUsuarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaConfiguracoesUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnAlterarSenha.setOnClickListener {
            startActivity(android.content.Intent(this, TrocarSenhaActivity::class.java))
        }

        binding.btnChat.setOnClickListener {
            startActivity(android.content.Intent(this, ChatSuporteActivity::class.java))
        }

        binding.btnFAQ.setOnClickListener {
            Toast.makeText(this, "Redirecionando para FAQ...", Toast.LENGTH_SHORT).show()
        }

        binding.switchVoz.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "ativado" else "desativado"
            Toast.makeText(this, "Comando por voz $status", Toast.LENGTH_SHORT).show()
        }

        binding.switchContraste.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "ativado" else "desativado"
            Toast.makeText(this, "Alto contraste $status", Toast.LENGTH_SHORT).show()
        }

        binding.switchVLibras.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "ativado" else "desativado"
            Toast.makeText(this, "vLibras $status", Toast.LENGTH_SHORT).show()
        }

        NavigationUtils.setupBottomNavigation(this, binding.bottomNavigation, R.id.navigation_perfil)
    }
}
