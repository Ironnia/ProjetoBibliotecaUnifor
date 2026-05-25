package com.example.bibliotecaunifor.usuario.catalogo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.bibliotecaunifor.crud.Entrada
import com.example.bibliotecaunifor.databinding.TelaCatalogoBinding
import com.example.bibliotecaunifor.crud.listarEntradas
import com.example.bibliotecaunifor.crud.buscarEntrada
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.example.bibliotecaunifor.mostrarAviso
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import com.example.bibliotecaunifor.crud.removerAcentos
import com.example.bibliotecaunifor.pegarNomeUsuario

class CatalogoActivity : AppCompatActivity() {
    private lateinit var binding: TelaCatalogoBinding
    private lateinit var adapter: BookAdapter
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private var acervoCompleto = listOf<Entrada>()
    private var idsReservados = setOf<String>()
    private var acervoListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var emprestimosListener: com.google.firebase.firestore.ListenerRegistration? = null

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
        binding.etSearch.doOnTextChanged { _, _, _, _ ->

            filtrarEAtualizarAdapter()
        }

        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, com.example.bibliotecaunifor.R.id.navigation_catalogo_aluno)
        
        setupRealtimeListeners()

        val initialQuery = intent.getStringExtra("QUERY")
        if (!initialQuery.isNullOrEmpty()) {
            binding.etSearch.setText(initialQuery)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val query = intent.getStringExtra("QUERY")
        if (!query.isNullOrEmpty()) {
            binding.etSearch.setText(query)
        }
    }

    private fun setupRealtimeListeners() {
        val uid = auth.currentUser?.uid ?: return

        acervoListener?.remove()
        acervoListener = db.collection("Acervo")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    acervoCompleto = snapshot.toObjects(Entrada::class.java)
                    filtrarEAtualizarAdapter()
                }
            }

        emprestimosListener?.remove()
        emprestimosListener = db.collection("emprestimos")
            .whereEqualTo("idUsuario", uid)
            .whereEqualTo("tipoItem", "livro")
            .whereIn("status", listOf("pendente", "ativo"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    idsReservados = snapshot.documents.mapNotNull { it.getString("idItem") }.toSet()
                    filtrarEAtualizarAdapter()
                }
            }
    }

    private fun filtrarEAtualizarAdapter() {
        val query = binding.etSearch.text.toString().trim().removerAcentos()
        val resultado = if (query.isEmpty()) {
            acervoCompleto
        } else {
            acervoCompleto.filter { entrada ->
                entrada.titulo.removerAcentos().contains(query) ||
                        entrada.autor.removerAcentos().contains(query) ||
                        entrada.isbn.removerAcentos().contains(query)
            }
        }
        adapter.updateData(resultado, idsReservados)
    }



    private fun confirmarReserva(entry: Entrada) {
        val uid = auth.currentUser?.uid ?: return

        // Verifica o limite de 5 livros ativos (pendente + ativo) antes de prosseguir
        db.collection("emprestimos")
            .whereEqualTo("idUsuario", uid)
            .whereEqualTo("tipoItem", "livro")
            .whereIn("status", listOf("pendente", "ativo"))
            .get()
            .addOnSuccessListener { result ->
                if (result.size() >= 5) {
                    com.google.android.material.snackbar.Snackbar.make(
                        binding.root,
                        "Você atingiu o limite de 5 livros. Devolva um antes de reservar outro.",
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    abrirDialogoReserva(entry)
                }
            }
            .addOnFailureListener {
                // Em caso de falha na verificação, permite prosseguir (a transação ainda protege)
                abrirDialogoReserva(entry)
            }
    }

    private fun abrirDialogoReserva(entry: Entrada) {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        val retiradaDate = java.text.SimpleDateFormat("dd/MM/yyyy").format(calendar.time)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 14)
        val devolucaoDate = java.text.SimpleDateFormat("dd/MM/yyyy").format(calendar.time)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reservar Livro?")
            .setMessage("Título: ${entry.titulo}\nAutor: ${entry.autor}\nISBN: ${entry.isbn}\n\nPrazo para retirada: $retiradaDate às 21:00")
            .setPositiveButton("Confirmar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                button.isEnabled = false // Evita cliques múltiplos
                val uid = auth.currentUser?.uid ?: run {
                    dialog.dismiss()
                    return@setOnClickListener
                }
                
                pegarNomeUsuario { nomeUsuario ->
                    val calendarDevolucao = java.util.Calendar.getInstance()
                    calendarDevolucao.add(java.util.Calendar.DAY_OF_YEAR, 15)
                    val devolucaoMillis = calendarDevolucao.timeInMillis



                    db.runTransaction { transaction ->
                        val livroRef = db.collection("Acervo").document(entry.id)
                        val snapshot = transaction.get(livroRef)
                        val entradaDb = snapshot.toObject(Entrada::class.java) ?: return@runTransaction null

                        // Busca o primeiro exemplar disponível
                        val exemplar = entradaDb.exemplares.firstOrNull { it.situacao == "Disponivel" }

                        if (exemplar != null) {
                            val novosExemplares = entradaDb.exemplares.map { ex ->
                                if (ex.registro == exemplar.registro) {
                                    ex.copy(situacao = "Reservado")
                                } else {
                                    ex
                                }
                            }

                            val aluguelRef = db.collection("emprestimos").document()
                            val novoAluguel = hashMapOf(
                                "idUsuario" to uid,
                                "nomeUsuario" to nomeUsuario,
                                "idLivro" to entry.id,
                                "tituloLivro" to entry.titulo,
                                "autorLivro" to entry.autor,
                                "idExemplar" to exemplar.registro,
                                "dataEmprestimo" to System.currentTimeMillis(),
                                "dataDevolucaoPrevista" to java.util.Date(devolucaoMillis),
                                "status" to "pendente",
                                "tipoItem" to "livro"
                            )

                            transaction.set(aluguelRef, novoAluguel)
                            transaction.update(livroRef, "exemplares", novosExemplares)
                            transaction.update(livroRef, "reservaCount", entradaDb.reservaCount + 1)
                            true
                        } else {
                            false
                        }
                    }.addOnSuccessListener { sucesso ->
                        dialog.dismiss()
                        if (sucesso == true) {
                            exibirSucessoReserva(entry, retiradaDate, devolucaoDate)
                            // setupRealtimeListeners já vai atualizar reativamente
                        } else {
                            mostrarAviso("Infelizmente o livro acabou de ficar indisponível.")
                        }
                    }.addOnFailureListener {
                        button.isEnabled = true
                        mostrarAviso("Erro ao processar reserva. Tente novamente.")
                    }
                }
            }
        }
        dialog.show()
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

    override fun onDestroy() {
        super.onDestroy()
        acervoListener?.remove()
        emprestimosListener?.remove()
    }
}
