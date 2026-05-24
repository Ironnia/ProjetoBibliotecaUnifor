package com.example.bibliotecaunifor.usuario.configuracoes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaConfiguracoesUsuarioBinding
import com.example.bibliotecaunifor.mostrarToast
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils

class ConfiguracoesUsuarioActivity : AppCompatActivity() {
    private lateinit var binding: TelaConfiguracoesUsuarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaConfiguracoesUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Pra salvar local.
        val prefs = getSharedPreferences("configuracoes", Context.MODE_PRIVATE)

        // Carregar as escolhas salvas
        with(binding) {
            switchVoz.isChecked = prefs.getBoolean("acessibilidade_voz", false)
            switchContraste.isChecked = prefs.getBoolean("acessibilidade_contraste", false)
            switchVLibras.isChecked = prefs.getBoolean("acessibilidade_vlibras", false)

            toolbar.setNavigationOnClickListener { finish() }

            btnAlterarSenha.setOnClickListener {
                startActivity(
                    Intent(
                        this@ConfiguracoesUsuarioActivity,
                        TrocarSenhaActivity::class.java
                    )
                )
            }

            btnChat.setOnClickListener {
                startActivity(Intent(this@ConfiguracoesUsuarioActivity, ChatSuporteActivity::class.java))
            }

            btnFAQ.setOnClickListener {
                mostrarToast("Redirecionando para FAQ...")
                //Toast.makeText(this, "Redirecionando para FAQ...", Toast.LENGTH_SHORT).show()
            }

            // Usar KTX agora.
            // tudo está sendo salvo local e checando como está.
            switchVoz.setOnCheckedChangeListener { _, isChecked ->

                prefs.edit { putBoolean("acessibilidade_voz", isChecked) }

                val status = if (isChecked) "ativado" else "desativado"
                mostrarToast("Comando por voz $status")
            }

            switchContraste.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit { putBoolean("acessibilidade_contraste", isChecked) }

                val status = if (isChecked) "ativado" else "desativado"
                mostrarToast("Alto contraste $status")
            }

            switchVLibras.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit { putBoolean("acessibilidade_vlibras", isChecked) }

                val status = if (isChecked) "ativado" else "desativado"
                mostrarToast("vLibras $status")
            }

            // Navegação Inferior
            NavigationUtils.setupBottomNavigation(this@ConfiguracoesUsuarioActivity, bottomNavigation, R.id.navigation_perfil)
        }
    }
}
