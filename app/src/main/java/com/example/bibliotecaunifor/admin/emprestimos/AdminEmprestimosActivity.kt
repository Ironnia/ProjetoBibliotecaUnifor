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
    private var emprestimosListener: com.google.firebase.firestore.ListenerRegistration? = null

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
        binding.chipRetirar.isChecked = true // Força o primeiro filtro a iniciar ativo
    }

    private fun setupRecyclerView() {
        adapter = AdminEmprestimoAdapter(listOf())
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupRealtimeListener() {
        // Escuta real-time somente os empréstimos de LIVROS (não inclui jogos)
        emprestimosListener = db.collection("emprestimos")
            .whereEqualTo("tipoItem", "livro")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // Filtra localmente para excluir os já concluídos (evita índice composto)
                    allLoans = snapshot.toObjects(Emprestimo::class.java)
                        .filter { it.status != "devolvido" && it.status != "cancelado" }
                    filterList()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        emprestimosListener?.remove()
    }

    private fun setupFilters() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener { _, _ ->
            filterList()
        }
    }

    private fun filterList() {
        val isDevolucaoChecked = binding.chipDevolucao.isChecked // Ativos / Para Devolução
        val isRetirarChecked = binding.chipRetirar.isChecked // Pendentes / A retirar

        // Se o usuário tentar desmarcar ambos (caso não use selectionRequired), 
        // nós forçamos uma lista vazia ou mostramos um aviso
        if (!isDevolucaoChecked && !isRetirarChecked) {
            adapter.updateList(emptyList())
            binding.tvEmptyState.text = "Selecione um filtro acima para gerenciar."
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            return
        }

        val finalList = allLoans.filter { item ->
            when {
                isRetirarChecked -> item.status.equals("pendente", ignoreCase = true)
                isDevolucaoChecked -> item.status.equals("ativo", ignoreCase = true) || item.status.equals("atrasado", ignoreCase = true)
                else -> false // Se cair aqui por qualquer outro status, não mostra (evita itens fantasmas)
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
