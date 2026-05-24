package com.example.bibliotecaunifor.usuario.catalogo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.crud.Entrada
import com.example.bibliotecaunifor.databinding.TelaCatalogoBinding
import com.example.bibliotecaunifor.crud.listarEntradas
import com.example.bibliotecaunifor.crud.buscarEntrada
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import kotlinx.coroutines.launch

class CatalogoActivity : AppCompatActivity() {
    private lateinit var binding: TelaCatalogoBinding
    private lateinit var adapter: BookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = TelaCatalogoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            finish()
        }

        adapter = BookAdapter(
            entries = emptyList(),
            onBookClicked = { entry ->
                val intent = Intent(this, com.example.bibliotecaunifor.usuario.reserva.DetalhesLivroActivity::class.java).apply {
                    putExtra("entrada_id", entry.id)
                }
                startActivity(intent)
            },
            onReserveClicked = { entry ->
                if (entry.exemplaresDisponiveis > 0) {
                    confirmarReserva(entry)
                } else {
                    exibirIndisponivel()
                }
            }
        )

        binding.rvBooks.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvBooks.adapter = adapter

        // +ktx, Nosso campinho de bucsar vai funcionar assim:
        // "monitora" se tem texto escrito, se tem faz o "filtro, se não mostra tudo (mesmo de antes, mas agora simples de entender né)
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            val query = text.toString()
            if (query.isNotEmpty()) searchEntries(query) else loadEntries()
        }

        // Direto do m3, é o filtro ráido. É legal.
        // Em outro momento precisa meio que aumentar bastante a lógica para procurar por todo tipo de coisa, como autor; (acho que só titulo e Autor, está bom?)
        binding.cgFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                // isso vai identificar se é "livro" ou "artigo", ele vai ler o texto do botão. Po isso o caça pelo botão.
                val chip = group.findViewById<com.google.android.material.chip.Chip>(checkedIds.first())
                searchEntries(chip.text.toString())
            }
        }


        NavigationUtils.setupBottomNavigation(this, binding.bottomNavigation, com.example.bibliotecaunifor.R.id.navigation_catalogo)
        
        loadEntries()
    }

    private fun loadEntries() {
        lifecycleScope.launch {
            val entries = listarEntradas()
            adapter.updateData(entries)
        }
    }

    private fun searchEntries(query: String) {
        lifecycleScope.launch {
            val entries = buscarEntrada(query)
            adapter.updateData(entries)
        }
    }

    private fun confirmarReserva(entry: Entrada) {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        val retiradaDate = java.text.SimpleDateFormat("dd/MM/yyyy").format(calendar.time)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 14)
        val devolucaoDate = java.text.SimpleDateFormat("dd/MM/yyyy").format(calendar.time)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reservar Livro?")
            .setMessage("Título: ${entry.titulo}\nAutor: ${entry.autor}\nISBN: ${entry.isbn}\n\nPrazo para retirada: $retiradaDate às 21:00")
            .setPositiveButton("Confirmar") { _, _ ->
                exibirSucessoReserva(entry, retiradaDate, devolucaoDate)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun exibirSucessoReserva(entry: Entrada, retiradaDate: String, devolucaoDate: String) {
        val dialogView = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setPadding(32, 32, 32, 32)
            val iv = android.widget.ImageView(context).apply {
                setImageResource(com.example.bibliotecaunifor.R.drawable.ic_qrcode)
                layoutParams = android.widget.LinearLayout.LayoutParams(500, 500)
            }
            val tv = android.widget.TextView(context).apply {
                text = "(Apresente no balcão)"
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 16, 0, 0)
            }
            addView(iv)
            addView(tv)
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reserva Efetuada!")
            .setMessage("O livro ${entry.titulo} foi reservado!\n\nRetirar até $retiradaDate às 21:00\nDevolver em $devolucaoDate.")
            .setView(dialogView)
            .setPositiveButton("Fechar", null)
            .show()
    }

    private fun exibirIndisponivel() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Indisponível")
            .setMessage("No momento este livro não está disponível!")
            .setPositiveButton("Voltar", null)
            .show()
    }
}
