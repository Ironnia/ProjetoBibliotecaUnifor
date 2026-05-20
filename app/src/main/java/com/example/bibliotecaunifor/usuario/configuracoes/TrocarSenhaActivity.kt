package com.example.bibliotecaunifor.usuario.configuracoes

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaTrocarSenhaBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils

class TrocarSenhaActivity : AppCompatActivity() {
    private lateinit var binding: TelaTrocarSenhaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaTrocarSenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnConfirmar.setOnClickListener {
            val atual = binding.etSenhaAtual.text.toString()
            val nova = binding.etNovaSenha.text.toString()
            val confirmar = binding.etConfirmarSenha.text.toString()

            if (atual.isEmpty() || nova.isEmpty() || confirmar.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else if (nova != confirmar) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        NavigationUtils.setupBottomNavigation(this, binding.bottomNavigation, R.id.navigation_perfil)
    }
}
