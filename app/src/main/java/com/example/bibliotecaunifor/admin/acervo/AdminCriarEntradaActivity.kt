package com.example.bibliotecaunifor.admin.acervo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.crud.Entrada
import com.example.bibliotecaunifor.crud.Exemplar
import com.example.bibliotecaunifor.crud.adicionarEntrada
import com.example.bibliotecaunifor.crud.buscarEntradaPorId
import com.example.bibliotecaunifor.crud.editarEntrada
import com.example.bibliotecaunifor.databinding.TelaAdminEditarLivroBinding
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AdminCriarEntradaActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminEditarLivroBinding
    private var entradaId: String? = null
    private lateinit var exemplarAdapter: AdminExemplarEditAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminEditarLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        val isEdit = intent.getBooleanExtra("isEdit", false)
        entradaId = intent.getStringExtra("entrada_id")
        if (isEdit && entradaId != null) {
            val titulo = intent.getStringExtra("titulo") ?: ""
            val autor = intent.getStringExtra("autor") ?: ""
            val isbn = intent.getStringExtra("isbn") ?: ""
            val edicao = intent.getStringExtra("edicao") ?: ""
            val publicacao = intent.getStringExtra("publicacao") ?: ""
            val cdu = intent.getStringExtra("cdu") ?: ""
            val cutter = intent.getStringExtra("cutter") ?: ""
            val assuntos = intent.getStringArrayListExtra("assuntos") ?: arrayListOf<String>()

            binding.etTitulo.setText(titulo)
            binding.etAutor.setText(autor)
            binding.etIsbn.setText(isbn)
            binding.etEdicao.setText(edicao)
            binding.etPublicacao.setText(publicacao)
            binding.etCdu.setText(cdu)
            binding.etCutter.setText(cutter)
            assuntos.forEach { adicionarChipAssunto(it) }

            lifecycleScope.launch {
                val entrada = buscarEntradaPorId(entradaId!!)
                val exemplares = entrada?.exemplares ?: emptyList()
                exemplarAdapter.setExemplares(exemplares)
            }
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancelar.setOnClickListener { finish() }

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
        val titulo = binding.etTitulo.text.toString()
        val autor = binding.etAutor.text.toString()
        val isbn = binding.etIsbn.text.toString()
        val edicao = binding.etEdicao.text.toString()
        val publicacao = binding.etPublicacao.text.toString()
        val cdu = binding.etCdu.text.toString()
        val cutter = binding.etCutter.text.toString()
        
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
            exemplares = exemplares
        )

        lifecycleScope.launch {
            if (isEdit && entradaId != null) {
                editarEntrada(novaEntrada, entradaId!!)
                Snackbar.make(binding.root, "Livro atualizado com sucesso!", Snackbar.LENGTH_SHORT).show()
            } else {
                adicionarEntrada(novaEntrada)
                Snackbar.make(binding.root, "Livro cadastrado com sucesso!", Snackbar.LENGTH_SHORT).show()
            }
            binding.btnConcluir.postDelayed({ 
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
