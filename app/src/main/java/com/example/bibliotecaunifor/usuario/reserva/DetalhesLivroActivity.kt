package com.example.bibliotecaunifor.usuario.reserva

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.databinding.TelaDetalhesLivroBinding

class DetalhesLivroActivity : AppCompatActivity() {
    private lateinit var binding: TelaDetalhesLivroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaDetalhesLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("title") ?: "Sem título"
        val author = intent.getStringExtra("author") ?: "Desconhecido"
        val available = intent.getIntExtra("available", 0)

        binding.tvTitle.text = title
        binding.tvAuthor.text = author

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnDetails.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Detalhes do Livro")
                .setMessage("Título: $title\nAutor: $author\nISBN: 978-3-16-148410-0\n\nDescrição: Uma das obras-primas da literatura brasileira, explorando temas de ciúme e dúvida através da vida de Bento Santiago.")
                .setPositiveButton("Ok", null)
                .show()
        }

        binding.btnReservar.setOnClickListener {
            if (available > 0) {
                val isbn = "978-3-16-148410-0" // Mock ISBN
                val calendar = java.util.Calendar.getInstance()
                
                // Retirada: Amanhã às 21:00
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                val retiradaDate = java.text.SimpleDateFormat("dd/MM/yyyy").format(calendar.time)
                
                // Devolução: 14 dias após a retirada (Total 15 dias de hoje)
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 14)
                val devolucaoDate = java.text.SimpleDateFormat("dd/MM/yyyy").format(calendar.time)

                AlertDialog.Builder(this)
                    .setTitle("Confirmar Reserva")
                    .setMessage("Título: $title\nAutor: $author\nISBN: $isbn\n\nDeseja confirmar a reserva deste item?")
                    .setPositiveButton("Confirmar") { _, _ ->
                        AlertDialog.Builder(this)
                            .setTitle("Reserva Realizada!")
                            .setMessage("O livro \"$title\" foi reservado com sucesso.\n\nRetirar até: $retiradaDate às 21:00\nDevolver em: $devolucaoDate\n\nApresente seu QR Code no balcão para retirada.")
                            .setPositiveButton("Ver QR Code") { _, _ ->
                                // Simular QR Code com texto abaixo
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

                                val qrDialog = AlertDialog.Builder(this)
                                    .setTitle("Seu QR Code de Retirada")
                                    .setMessage("O livro $title foi reservado.\nRetirar até $retiradaDate às 21:00\nDevolver em $devolucaoDate")
                                    .setView(dialogView)
                                    .setPositiveButton("Fechar", null)
                                    .create()
                                qrDialog.show()
                            }
                            .setNegativeButton("Fechar", null)
                            .show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Indisponível")
                    .setMessage("No momento este livro não está disponível!")
                    .setPositiveButton("Voltar", null)
                    .show()
            }
        }

        com.example.bibliotecaunifor.usuario.utils.NavigationUtils.setupBottomNavigation(this, binding.bottomNavigation, com.example.bibliotecaunifor.R.id.navigation_catalogo)
    }
}
