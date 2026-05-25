package com.example.bibliotecaunifor.usuario.configuracoes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
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
                try {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/558592466625"))
                    startActivity(intent)
                } catch (e: Exception) {
                    mostrarToast("Não foi possível abrir o link do suporte.")
                }
            }

            btnFAQ.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://unifor.br/fale-conosco"))
                    startActivity(intent)
                } catch (e: Exception) {
                    mostrarToast("Não foi possível abrir a Central de Ajuda.")
                }
            }

            // Navegação Inferior
            NavigationUtils.navegacaoAluno(this@ConfiguracoesUsuarioActivity, bottomNavigation, R.id.navigation_perfil_aluno)
        }
    }
}
