package com.example.bibliotecaunifor.admin.jogos

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.crud.AluguelJogo
import com.example.bibliotecaunifor.databinding.TelaAdminJogosBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class AdminJogosActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminJogosBinding
    private lateinit var adapter: AdminJogosAdapter
    private var allLeases = listOf<AluguelJogo>()

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminJogosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Configuração da barra de navegação inferior do ADM
        NavigationUtils.navegacaoAdmin(this, binding.bottomNavigation, R.id.navigation_jogos_admin)

        setupRecyclerView()
        setupRealtimeListener()
        setupFilters()
    }

    private fun setupRecyclerView() {
        adapter = AdminJogosAdapter(listOf())
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupRealtimeListener() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE

        // Escuta real-time na coleção de alugueis
        db.collection("alugueis")
            .whereEqualTo("tipoItem", "jogo")
            .addSnapshotListener { snapshot, error ->
                binding.progressBar.visibility = View.GONE
                if (error != null) {
                    binding.tvEmptyState.text = "Erro ao conectar ao banco de dados."
                    binding.tvEmptyState.visibility = View.VISIBLE
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Filtramos localmente para remover itens que já foram devolvidos
                    allLeases = snapshot.toObjects(AluguelJogo::class.java).filter { it.status != "devolvido" }
                    filterList()
                }
            }
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
        val query = binding.etSearch.text.toString().trim().lowercase()
        val isDevolucaoChecked = binding.chipDevolucao.isChecked // Ativos / Em uso
        val isRetirarChecked = binding.chipRetirar.isChecked // Pendentes / A retirar

        // 1. Filtra localmente pela query textual (título do jogo ou email do aluno)
        val filteredQuery = if (query.isEmpty()) {
            allLeases
        } else {
            allLeases.filter { item ->
                item.tituloItem.lowercase().contains(query) ||
                item.emailUsuario.lowercase().contains(query)
            }
        }

        // 2. Filtra localmente pelo chip selecionado
        val finalList = filteredQuery.filter { item ->
            when {
                isRetirarChecked -> item.status.equals("pendente", ignoreCase = true)
                isDevolucaoChecked -> item.status.equals("ativo", ignoreCase = true)
                else -> true
            }
        }

        // 3. Renderização final e gerenciamento de Empty States Contextualizados
        if (finalList.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            
            val emptyText = when {
                isRetirarChecked -> "Nenhuma reserva aguardando retirada."
                isDevolucaoChecked -> "Nenhum jogo em uso no momento."
                else -> "Nenhum aluguel de jogo registrado."
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
