package com.example.bibliotecaunifor.admin.emprestimos

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.crud.Emprestimo
import com.example.bibliotecaunifor.databinding.TelaAdminEmprestimosBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class AdminEmprestimosActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminEmprestimosBinding
    private lateinit var adapter: AdminEmprestimoAdapter
    private var allLoans = listOf<Emprestimo>()

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminEmprestimosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includeToolbar.btnBack.setOnClickListener {
            finish()
        }

        NavigationUtils.navegacaoAdmin(this, binding.bottomNavigation, R.id.navigation_home_admin)

        setupRecyclerView()
        setupRealtimeListener()
        setupFilters()
    }

    private fun setupRecyclerView() {
        adapter = AdminEmprestimoAdapter(listOf())
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupRealtimeListener() {
        // Escuta real-time na coleção de emprestimos
        db.collection("emprestimos")
            .whereNotEqualTo("status", "devolvido")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    allLoans = snapshot.toObjects(Emprestimo::class.java)
                    filterList()
                }
            }
    }

    private fun setupFilters() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener { _, _ ->
            filterList()
        }
    }

    private fun filterList() {
        val isDevolucaoChecked = binding.chipDevolucao.isChecked // Ativos / Para Devolução
        val isRetirarChecked = binding.chipRetirar.isChecked // Pendentes / A retirar

        val finalList = allLoans.filter { item ->
            when {
                isRetirarChecked -> item.status.equals("pendente", ignoreCase = true)
                isDevolucaoChecked -> item.status.equals("ativo", ignoreCase = true) || item.status.equals("atrasado", ignoreCase = true)
                else -> true
            }
        }

        if (finalList.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            
            val emptyText = when {
                isRetirarChecked -> "Nenhuma reserva aguardando retirada."
                isDevolucaoChecked -> "Nenhum livro fora da biblioteca no momento."
                else -> "Nenhum empréstimo ativo no momento."
            }
            binding.tvEmptyState.text = emptyText
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
            adapter.updateList(finalList)
        }
    }
}
