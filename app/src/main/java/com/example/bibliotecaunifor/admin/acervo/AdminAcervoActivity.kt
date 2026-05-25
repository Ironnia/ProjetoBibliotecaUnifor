package com.example.bibliotecaunifor.admin.acervo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.crud.Entrada
import com.example.bibliotecaunifor.databinding.TelaAdminAcervoBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

import com.example.bibliotecaunifor.crud.removerAcentos

class AdminAcervoActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminAcervoBinding
    private lateinit var adapter: AdminEntradaAdapter

    /** Cache da lista completa carregada via SnapshotListener */
    private var todasEntradas = listOf<Entrada>()
    private var acervoListener: com.google.firebase.firestore.ListenerRegistration? = null

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminAcervoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recupera busca inicial da Home (se houver)
        val termoBuscaInicial = intent.getStringExtra("BUSCA")
        if (!termoBuscaInicial.isNullOrEmpty()) {
            binding.etSearch.setText(termoBuscaInicial)
        }

        binding.includeToolbar.btnBack.setOnClickListener { finish() }

        NavigationUtils.navegacaoAdmin(this, binding.bottomNavigation, R.id.navigation_catalogo_admin)

        setupRecyclerView()
        setupRealtimeListener()
        setupSearch()

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AdminCriarEntradaActivity::class.java)
            intent.putExtra("isEdit", false)
            startActivity(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val busca = intent.getStringExtra("BUSCA")
        if (!busca.isNullOrEmpty()) {
            binding.etSearch.setText(busca)
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminEntradaAdapter(emptyList()) {
            // Callback ao deletar: o SnapshotListener vai atualizar automaticamente
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    /**
     * Escuta em tempo real o acervo. Qualquer CRUD feito pelo admin
     * reflete instantaneamente na lista sem precisar sair e voltar.
     */
    private fun setupRealtimeListener() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE

        acervoListener = db.collection("Acervo")
            .addSnapshotListener { snapshot, error ->
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE

                if (error != null || snapshot == null) return@addSnapshotListener

                todasEntradas = snapshot.toObjects(Entrada::class.java)
                filtrarLocalmente()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        acervoListener?.remove()
    }

    /**
     * Configura busca local em tempo real por título, autor ou ISBN
     * (case-insensitive e sem necessidade de índice no Firestore).
     */
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLocalmente()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    /** Filtra a lista em memória por título, autor ou ISBN (case-insensitive e tolerante a acentos). */
    private fun filtrarLocalmente() {
        val termo = binding.etSearch.text.toString().trim().removerAcentos()

        val resultado = if (termo.isEmpty()) {
            todasEntradas
        } else {
            todasEntradas.filter { entrada ->
                entrada.titulo.removerAcentos().contains(termo) ||
                        entrada.autor.removerAcentos().contains(termo) ||
                        entrada.isbn.removerAcentos().contains(termo)
            }
        }

        adapter.updateData(resultado)
    }
}
