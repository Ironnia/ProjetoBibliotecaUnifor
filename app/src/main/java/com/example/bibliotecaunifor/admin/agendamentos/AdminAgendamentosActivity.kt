package com.example.bibliotecaunifor.admin.agendamentos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaAdminAgendamentosBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils

class AdminAgendamentosActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminAgendamentosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminAgendamentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includeToolbar.btnBack.setOnClickListener {
            finish()
        }

        NavigationUtils.setupAdminNavigation(this, binding.bottomNavigation, R.id.navigation_salas)
    }
}
