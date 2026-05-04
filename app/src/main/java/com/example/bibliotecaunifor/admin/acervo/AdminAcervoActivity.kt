package com.example.bibliotecaunifor.admin.acervo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaAdminAcervoBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils

class AdminAcervoActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminAcervoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminAcervoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includeToolbar.btnBack.setOnClickListener {
            finish()
        }

        NavigationUtils.setupAdminNavigation(this, binding.bottomNavigation, R.id.navigation_catalogo)

        setupRecyclerView()

        binding.fabAdd.setOnClickListener {
            startActivity(android.content.Intent(this, AdminCriarLivroActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        val examples = listOf(
            AdminLivro("A Metamorfose", "Franz Kafka", 3, 2, true),
            AdminLivro("1984", "George Orwell", 3, 2, false),
            AdminLivro("O Espelho", "Machado de Assis", 3, 2, false),
            AdminLivro("Dom Casmurro", "Machado de Assis", 3, 2, false),
            AdminLivro("Código da Vinci", "Dan Brown", 3, 2, false)
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = AdminLivroAdapter(examples)
    }
}
