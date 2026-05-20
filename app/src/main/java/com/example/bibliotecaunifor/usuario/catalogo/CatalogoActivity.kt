package com.example.bibliotecaunifor.usuario.catalogo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.databinding.TelaCatalogoBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils

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

        binding.ivBack.setOnClickListener {
            finish()
        }

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
                    val calendar = java.util.Calendar.getInstance()
                    // Retirada: Amanhã às 21:00
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                    val retiradaDate = java.text.SimpleDateFormat("dd/MM/yyyy").format(calendar.time)
                    // Devolução: 14 dias após a retirada
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, 14)
                    val devolucaoDate = java.text.SimpleDateFormat("dd/MM/yyyy").format(calendar.time)

                    val imageView = android.widget.ImageView(this).apply {
                        setImageResource(com.example.bibliotecaunifor.R.drawable.ic_qrcode)
                        setPadding(0, 32, 0, 32)
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            500
                        )
                    }

                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Reservar Livro?")
                        .setMessage("Título: ${book.title}\nAutor: ${book.author}\nISBN: ${book.isbn}\n\nPrazo para retirada: $retiradaDate às 21:00")
                        .setPositiveButton("Confirmar") { _, _ ->
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
                                .setMessage("O livro ${book.title} foi reservado!\n\nRetirar até $retiradaDate às 21:00\nDevolver em $devolucaoDate.")
                                .setView(dialogView)
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

        NavigationUtils.setupBottomNavigation(this, binding.bottomNavigation, com.example.bibliotecaunifor.R.id.navigation_catalogo)
    }
}
