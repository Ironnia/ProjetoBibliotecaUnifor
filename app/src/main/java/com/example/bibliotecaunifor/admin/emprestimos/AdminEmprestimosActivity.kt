package com.example.bibliotecaunifor.admin.emprestimos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaAdminEmprestimosBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils

class AdminEmprestimosActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminEmprestimosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminEmprestimosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includeToolbar.btnBack.setOnClickListener {
            finish()
        }

        NavigationUtils.setupAdminNavigation(this, binding.bottomNavigation, R.id.navigation_home) // Not a main tab, but part of admin flow

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val examples = listOf(
            AdminEmprestimo("A Metamorfose", "Franz Kafka", "1234567", "15/04/2026 | 9:45", true),
            AdminEmprestimo("O Espelho", "Machado de Assis", "1213145", "15/04/2026 | 9:45", true),
            AdminEmprestimo("O Espelho", "Machado de Assis", "7465343", "15/04/2026 | 9:45", false),
            AdminEmprestimo("Código da Vinci", "Dan Brown", "111213140", "15/04/2026 | 9:45", false)
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = AdminEmprestimoAdapter(examples)
    }
}
