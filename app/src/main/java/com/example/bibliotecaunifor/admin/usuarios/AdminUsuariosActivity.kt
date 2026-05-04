package com.example.bibliotecaunifor.admin.usuarios

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaAdminUsuariosBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils

class AdminUsuariosActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminUsuariosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includeToolbar.btnBack.setOnClickListener {
            finish()
        }

        NavigationUtils.setupAdminNavigation(this, binding.bottomNavigation, R.id.navigation_perfil)
    }
}
