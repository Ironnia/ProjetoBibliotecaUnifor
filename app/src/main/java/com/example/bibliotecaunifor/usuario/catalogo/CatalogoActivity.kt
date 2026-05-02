package com.example.bibliotecaunifor.usuario.catalogo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.databinding.TelaCatalogoBinding

class CatalogoActivity : AppCompatActivity() {
    private lateinit var binding: TelaCatalogoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaCatalogoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val books = listOf(
            Book("O código da Vinci", "Dan Brown", 3),
            Book("1984", "George Orwell", 2),
            Book("Dom Casmurro", "Machado de Assis", 0),
            Book("A metamorfose", "Franz Kafka", 3),
            Book("O espelho", "Machado de Assis", 1)
        )

        val adapter = BookAdapter(
            books = books,
            onBookClicked = { book ->
                val intent = Intent(this, com.example.bibliotecaunifor.usuario.reserva.DetalhesLivroActivity::class.java).apply {
                    putExtra("title", book.title)
                    putExtra("author", book.author)
                    putExtra("isbn", book.isbn)
                    putExtra("available", book.availableCopies)
                }
                startActivity(intent)
            },
            onReserveClicked = { book ->
                if (book.availableCopies > 0) {
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Reservar ${book.title}?")
                        .setMessage("Prazo para retirada: 23:59:59")
                        .setPositiveButton("Confirmar") { _, _ ->
                            androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("O livro \"${book.title}\" foi reservado")
                                .setMessage("Retirar até amanhã às 09:00\nDevolver em 7 dias.")
                                .setPositiveButton("Fechar", null)
                                .show()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                } else {
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Indisponível")
                        .setMessage("No momento este livro não está disponível!")
                        .setPositiveButton("Voltar", null)
                        .show()
                }
            }
        )

        binding.rvBooks.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvBooks.adapter = adapter

        binding.bottomNavigation.selectedItemId = com.example.bibliotecaunifor.R.id.navigation_catalogo
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.bibliotecaunifor.R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                com.example.bibliotecaunifor.R.id.navigation_catalogo -> true
                // TODO: Salas and Perfil
                else -> false
            }
        }
    }
}
