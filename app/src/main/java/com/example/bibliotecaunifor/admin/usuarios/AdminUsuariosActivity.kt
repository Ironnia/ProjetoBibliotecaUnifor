package com.example.bibliotecaunifor.admin.usuarios

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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

        setupRecyclerView()

        // FAB: Atribuir livro a aluno (admin power)
        binding.fabAdd.setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Atribuir Livro")
                .setMessage("Informe a matrícula do aluno e o livro que deseja atribuir diretamente.")
                .setPositiveButton("Confirmar") { _, _ ->
                    com.google.android.material.snackbar.Snackbar.make(
                        binding.root, "Livro atribuído com sucesso!", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        NavigationUtils.setupAdminNavigation(this, binding.bottomNavigation, R.id.navigation_perfil)
    }

    private fun setupRecyclerView() {
        val examples = listOf(
            AdminUsuarioLivro("O Espelho", "Machado de Assis", "Exemplar 2 alugado"),
            AdminUsuarioLivro("A Metamorfose", "Franz Kafka", "Exemplar 1 alugado"),
            AdminUsuarioLivro("1984", "George Orwell", "Exemplar 3 reservado")
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = AdminUsuarioLivroAdapter(examples)
    }
}
