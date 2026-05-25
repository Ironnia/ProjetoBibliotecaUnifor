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
    private var jogosListener: com.google.firebase.firestore.ListenerRegistration? = null

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
        // setupFilters()
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
        jogosListener = db.collection("alugueis")
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

    override fun onDestroy() {
        super.onDestroy()
        jogosListener?.remove()
    }



    private fun filterList() {

        val finalList = allLeases

        // 3. Renderização final e gerenciamento de Empty States Contextualizados
        if (finalList.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            

            binding.tvEmptyState.text = "Nenhum aluguel de jogo registrado."
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
            adapter.updateList(finalList)
        }
    }
}
