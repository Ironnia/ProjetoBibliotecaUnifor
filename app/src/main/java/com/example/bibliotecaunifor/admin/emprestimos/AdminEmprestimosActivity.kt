package com.example.bibliotecaunifor.admin.emprestimos

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaAdminEmprestimosBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils

class AdminEmprestimosActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminEmprestimosBinding
    private lateinit var adapter: AdminEmprestimoAdapter
    private var allItems = listOf<AdminEmprestimo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminEmprestimosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includeToolbar.btnBack.setOnClickListener {
            finish()
        }

        NavigationUtils.setupAdminNavigation(this, binding.bottomNavigation, R.id.navigation_home)

        setupRecyclerView()
        setupFilters()
    }

    private fun setupRecyclerView() {
        allItems = listOf(
            AdminEmprestimo("A Metamorfose", "Franz Kafka", "1234567", "15/04/2026 | 9:45", true),
            AdminEmprestimo("O Espelho", "Machado de Assis", "1213145", "15/04/2026 | 9:45", true),
            AdminEmprestimo("O Espelho", "Machado de Assis", "7465343", "15/04/2026 | 9:45", false),
            AdminEmprestimo("Código da Vinci", "Dan Brown", "111213140", "15/04/2026 | 9:45", false)
        )

        adapter = AdminEmprestimoAdapter(listOf()) // Start empty
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupFilters() {
        binding.etSearch.addTextChangedListener {
            filterList()
        }

        binding.chipGroupFiltros.setOnCheckedStateChangeListener { _, _ ->
            filterList()
        }
    }

    private fun filterList() {
        val query = binding.etSearch.text.toString().lowercase()
        val isDevolucaoChecked = binding.chipDevolucao.isChecked
        val isRetirarChecked = binding.chipRetirar.isChecked

        if (query.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            adapter.updateList(listOf())
            return
        }

        binding.recyclerView.visibility = View.VISIBLE

        val filteredItems = allItems.filter { item ->
            val matchesQuery = item.titulo.lowercase().contains(query) ||
                    item.autor.lowercase().contains(query) ||
                    item.matricula.lowercase().contains(query)

            val matchesChip = when {
                isDevolucaoChecked && isRetirarChecked -> true // Both selected, show both (though group is singleSelection=true in XML, usually)
                isDevolucaoChecked -> item.isParaDevolucao
                isRetirarChecked -> !item.isParaDevolucao
                else -> true // None selected, show both
            }

            matchesQuery && matchesChip
        }

        adapter.updateList(filteredItems)
    }
}
