package com.example.bibliotecaunifor.usuario.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaRankingUsuarioBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils

class RankingUsuarioActivity : AppCompatActivity() {
    private lateinit var binding: TelaRankingUsuarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaRankingUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.chipGeral.setOnClickListener { setupRecyclerView() }
        binding.chipMensal.setOnClickListener { setupRecyclerView() }

        binding.btnBack.setOnClickListener {
            finish()
        }

        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, R.id.navigation_home)
    }

    private fun setupRecyclerView() {
        val rankList = listOf(
            RankItem(1, "João Victor", 45),
            RankItem(2, "Maria Silva", 38),
            RankItem(3, "Pedro Santos", 32),
            RankItem(4, "Ana Lima", 29),
            RankItem(5, "Lucas Gomes", 25)
        )

        binding.rvRanking.layoutManager = LinearLayoutManager(this)
        binding.rvRanking.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            inner class RankViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val pos: TextView = view.findViewById(R.id.tv_posicao)
                val nome: TextView = view.findViewById(R.id.tv_nome_aluno)
                val livros: TextView = view.findViewById(R.id.tv_pontuacao)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking_usuario, parent, false)
                return RankViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val item = rankList[position]
                (holder as RankViewHolder).pos.text = "${item.pos}º"
                holder.nome.text = item.nome
                holder.livros.text = "${item.livros}"
            }

            override fun getItemCount() = rankList.size
        }
    }

    data class RankItem(val pos: Int, val nome: String, val livros: Int)
}
