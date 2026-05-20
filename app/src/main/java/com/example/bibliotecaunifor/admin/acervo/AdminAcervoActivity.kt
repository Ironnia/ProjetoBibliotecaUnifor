package com.example.bibliotecaunifor.admin.acervo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaAdminAcervoBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.crud.listarEntradas
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import kotlinx.coroutines.launch

class AdminAcervoActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminAcervoBinding
    private lateinit var adapter: AdminEntradaAdapter

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
            val intent = android.content.Intent(this, AdminCriarLivroActivity::class.java)
            intent.putExtra("isEdit", false)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = AdminEntradaAdapter(emptyList()) {
            loadData()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadData() {
        lifecycleScope.launch {
            val items = listarEntradas()
            adapter.updateData(items)
        }
    }
}
