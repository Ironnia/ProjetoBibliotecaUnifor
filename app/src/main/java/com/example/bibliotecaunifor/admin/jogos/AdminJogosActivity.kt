package com.example.bibliotecaunifor.admin.jogos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaAdminJogosBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils

class AdminJogosActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminJogosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminJogosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        NavigationUtils.navegacaoAdmin(this, binding.bottomNavigation, R.id.navigation_perfil)
    }
}
