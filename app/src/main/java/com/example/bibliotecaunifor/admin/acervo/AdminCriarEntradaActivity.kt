package com.example.bibliotecaunifor.admin.acervo

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.crud.Entrada
import com.example.bibliotecaunifor.crud.Exemplar
import com.example.bibliotecaunifor.crud.adicionarEntrada
import com.example.bibliotecaunifor.crud.buscarEntradaPorId
import com.example.bibliotecaunifor.crud.editarEntrada
import com.example.bibliotecaunifor.databinding.TelaAdminEditarLivroBinding
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.UUID

class AdminCriarEntradaActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminEditarLivroBinding
    private var entradaId: String? = null
    private lateinit var exemplarAdapter: AdminExemplarEditAdapter
    private var reservaCount: Int = 0
    private var selectedImageUri: Uri? = null
    private var currentImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminEditarLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                Glide.with(this).load(uri).into(binding.ivCapaPreview)
            }
        }

        binding.btnSelecionarFoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        val isEdit = intent.getBooleanExtra("isEdit", false)
        entradaId = intent.getStringExtra("entrada_id")
        
        if (isEdit && entradaId != null) {
            lifecycleScope.launch {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnConcluir.isEnabled = false
                
                val entrada = buscarEntradaPorId(entradaId!!)
                if (entrada != null) {
                    binding.etTitulo.setText(entrada.titulo)
                    binding.etAutor.setText(entrada.autor)
                    binding.etIsbn.setText(entrada.isbn)
                    binding.etEdicao.setText(entrada.edicao)
                    binding.etPublicacao.setText(entrada.publicacao)
                    binding.etCdu.setText(entrada.cdu)
                    binding.etCutter.setText(entrada.cutter)
                    
                    binding.chipGroupAssuntos.removeAllViews()
                    entrada.assuntos.forEach { adicionarChipAssunto(it) }
                    
                    reservaCount = entrada.reservaCount
                    exemplarAdapter.setExemplares(entrada.exemplares)
                    
                    currentImageUrl = entrada.imageUrl
                    if (currentImageUrl.isNotEmpty()) {
                        Glide.with(this@AdminCriarEntradaActivity)
                            .load(currentImageUrl)
                            .into(binding.ivCapaPreview)
                    }
                }
                
                binding.progressBar.visibility = View.GONE
                binding.btnConcluir.isEnabled = true
            }
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancelar.setOnClickListener { finish() }

        // Adiciona tag ao apertar Enter/Search no teclado no campo Novo Assunto
        binding.etNovoAssunto.setOnKeyListener { _, keyCode, event ->
            if (event.action == android.view.KeyEvent.ACTION_DOWN && keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                val texto = binding.etNovoAssunto.text.toString().trim()
                if (texto.isNotEmpty()) {
                    adicionarChipAssunto(texto)
                    binding.etNovoAssunto.setText("")
                }
                true
            } else {
                false
            }
        }

        binding.btnAddAssunto.setOnClickListener {
            val texto = binding.etNovoAssunto.text.toString().trim()
            if (texto.isNotEmpty()) {
                adicionarChipAssunto(texto)
                binding.etNovoAssunto.setText("")
            }
        }

        binding.btnAddExemplar.setOnClickListener {
            exemplarAdapter.adicionarExemplar()
        }

        if (!isEdit) {
            exemplarAdapter.adicionarExemplar()
        }

        binding.btnConcluir.setOnClickListener {
            salvarEntrada(isEdit)
        }
    }

    private fun setupRecyclerView() {
        exemplarAdapter = AdminExemplarEditAdapter()
        binding.rvExemplares.apply {
            adapter = exemplarAdapter
            layoutManager = LinearLayoutManager(this@AdminCriarEntradaActivity)
        }
    }

    private fun salvarEntrada(isEdit: Boolean) {
        // Limpa erros anteriores
        binding.tilTitulo.error = null
        binding.tilAutor.error = null
        binding.tilIsbn.error = null

        val titulo = binding.etTitulo.text.toString().trim()
        val autor = binding.etAutor.text.toString().trim()
        val isbn = binding.etIsbn.text.toString().trim()
        val edicao = binding.etEdicao.text.toString().trim()
        val publicacao = binding.etPublicacao.text.toString().trim()
        val cdu = binding.etCdu.text.toString().trim()
        val cutter = binding.etCutter.text.toString().trim()

        var hasError = false

        if (titulo.isEmpty()) {
            binding.tilTitulo.error = "Título é obrigatório"
            hasError = true
        }
        if (autor.isEmpty()) {
            binding.tilAutor.error = "Autor é obrigatório"
            hasError = true
        }
        if (isbn.isEmpty()) {
            binding.tilIsbn.error = "ISBN é obrigatório"
            hasError = true
        }

        if (hasError) return

        val assuntos = mutableListOf<String>()
        for (i in 0 until binding.chipGroupAssuntos.childCount) {
            val chip = binding.chipGroupAssuntos.getChildAt(i) as Chip
            assuntos.add(chip.text.toString())
        }

        val exemplares = exemplarAdapter.getExemplares()

        val novaEntrada = Entrada(
            id = entradaId ?: "",
            isbn = isbn,
            titulo = titulo,
            autor = autor,
            edicao = edicao,
            publicacao = publicacao,
            cdu = cdu,
            cutter = cutter,
            assuntos = assuntos,
            exemplares = exemplares,
            reservaCount = reservaCount,
            imageUrl = currentImageUrl
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnConcluir.isEnabled = false

        if (selectedImageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val imageRef = storageRef.child("capas_livros/$fileName")
            
            imageRef.putFile(selectedImageUri!!).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    salvarNoFirestore(uri.toString(), isEdit, novaEntrada)
                }
            }.addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.btnConcluir.isEnabled = true
                Snackbar.make(binding.root, "Erro ao enviar imagem", Snackbar.LENGTH_SHORT).show()
            }
        } else {
            salvarNoFirestore(currentImageUrl, isEdit, novaEntrada)
        }
    }

    private fun salvarNoFirestore(url: String, isEdit: Boolean, entradaBase: Entrada) {
        val novaEntrada = entradaBase.copy(imageUrl = url)
        lifecycleScope.launch {
            if (isEdit && entradaId != null) {
                editarEntrada(novaEntrada, entradaId!!)
                Snackbar.make(binding.root, "Livro atualizado com sucesso!", Snackbar.LENGTH_SHORT).show()
            } else {
                adicionarEntrada(novaEntrada)
                Snackbar.make(binding.root, "Livro cadastrado com sucesso!", Snackbar.LENGTH_SHORT).show()
            }

            binding.btnConcluir.postDelayed({
                binding.progressBar.visibility = View.GONE
                binding.btnConcluir.isEnabled = true
                setResult(RESULT_OK)
                finish()
            }, 1000)
        }
    }

    private fun adicionarChipAssunto(texto: String) {
        val chip = Chip(this)
        chip.text = texto
        chip.isCloseIconVisible = true
        chip.setChipBackgroundColorResource(android.R.color.white)
        chip.chipStrokeWidth = 2f
        chip.setOnCloseIconClickListener {
            binding.chipGroupAssuntos.removeView(chip)
        }
        binding.chipGroupAssuntos.addView(chip)
    }
}
