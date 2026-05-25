package com.example.bibliotecaunifor.admin.acervo

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaAdminAcervoBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.crud.listarEntradas
import com.example.bibliotecaunifor.crud.buscarEntrada
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import kotlinx.coroutines.launch

class AdminAcervoActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminAcervoBinding
    private lateinit var adapter: AdminEntradaAdapter
    private var termoBuscaInicial: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminAcervoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recupera busca inicial da Home (se houver)
        termoBuscaInicial = intent.getStringExtra("BUSCA")
        if (!termoBuscaInicial.isNullOrEmpty()) {
            binding.etSearch.setText(termoBuscaInicial)
        }

        binding.includeToolbar.btnBack.setOnClickListener {
            finish()
        }

        NavigationUtils.navegacaoAdmin(this, binding.bottomNavigation, R.id.navigation_catalogo_admin)

        setupRecyclerView()

        // Configura campo de busca rápida local
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val texto = binding.etSearch.text.toString().trim()
                realizarBusca(texto)
                true
            } else {
                false
            }
        }

        // Habilita busca em tempo real com mudança de texto
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s.toString().trim()
                if (texto.isEmpty()) {
                    realizarBusca("")
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.fabAdd.setOnClickListener {
            val intent = android.content.Intent(this, AdminCriarEntradaActivity::class.java)
            intent.putExtra("isEdit", false)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Se houver busca inicial, faz a busca. Senão, carrega a lista completa
        val texto = binding.etSearch.text.toString().trim()
        realizarBusca(texto)
    }

    private fun setupRecyclerView() {
        adapter = AdminEntradaAdapter(emptyList()) {
            val texto = binding.etSearch.text.toString().trim()
            realizarBusca(texto)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun realizarBusca(pesquisa: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE

            val items = if (pesquisa.isEmpty()) {
                listarEntradas()
            } else {
                buscarEntrada(pesquisa)
            }

            adapter.updateData(items)
            binding.progressBar.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }
}
