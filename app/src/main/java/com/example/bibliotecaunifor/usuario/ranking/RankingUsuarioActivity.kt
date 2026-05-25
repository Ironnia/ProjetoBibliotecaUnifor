package com.example.bibliotecaunifor.usuario.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.Usuario
import com.example.bibliotecaunifor.databinding.TelaRankingUsuarioBinding
import com.example.bibliotecaunifor.databinding.ItemRankingUsuarioBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration

class RankingUsuarioActivity : AppCompatActivity() {
    private lateinit var binding: TelaRankingUsuarioBinding
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private var rankingListener: ListenerRegistration? = null
    private lateinit var rankAdapter: RankingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaRankingUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Destaca a aba de perfil/ranking na navegação inferior do aluno
        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, R.id.navigation_perfil_aluno)

        setupRecyclerView()
        observeRanking()
        carregarMinhaPosicao()

        binding.chipGeral.setOnClickListener { observeRanking() }
        binding.chipMensal.setOnClickListener { observeRanking() }
    }

    private fun setupRecyclerView() {
        val email = com.example.bibliotecaunifor.pegarEmailUsuario()
        rankAdapter = RankingAdapter(listOf(), email)
        binding.rvRanking.layoutManager = LinearLayoutManager(this)
        binding.rvRanking.adapter = rankAdapter
    }

    private fun observeRanking() {
        rankingListener?.remove()
        
        // Escuta em tempo real os 10 usuários com maior pontuação
        rankingListener = db.collection("usuario")
            .orderBy("pontos", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.toObjects(Usuario::class.java)
                    rankAdapter.updateList(list)
                }
            }
    }

    private fun carregarMinhaPosicao() {
        val emailLogado = com.example.bibliotecaunifor.pegarEmailUsuario()
        val uid = auth.currentUser?.uid ?: return

        // 1. Pega meus pontos em tempo real
        db.collection("usuario").document(uid).addSnapshotListener { doc, err ->
            if (err == null && doc != null) {
                val meusPontos = doc.getLong("pontos") ?: 0L
                val meuNome = doc.getString("nome") ?: "Eu"
                
                binding.tvMeusPontos.text = "${meusPontos} pts"
                binding.tvMeuNome.text = "$meuNome (Você)"

                // 2. Conta quantos usuários têm MAIS pontos que eu para saber minha posição
                db.collection("usuario")
                    .whereGreaterThan("pontos", meusPontos)
                    .get()
                    .addOnSuccessListener { result ->
                        val posicao = result.size() + 1
                        binding.tvMinhaPosicao.text = "${posicao}º"
                    }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rankingListener?.remove()
    }

    class RankingAdapter(
        private var users: List<Usuario>,
        private val emailLogado: String
    ) : RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemRankingUsuarioBinding) : RecyclerView.ViewHolder(binding.root)

        fun updateList(newList: List<Usuario>) {
            users = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemRankingUsuarioBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = users[position]
            with(holder.binding) {
                // 1. Lógica do Pódio (Top 3) com emojis e cores personalizadas
                val emoji = when (position) {
                    0 -> "🥇 "
                    1 -> "🥈 "
                    2 -> "🥉 "
                    else -> ""
                }
                tvPosicao.text = "$emoji${position + 1}º"
                
                when (position) {
                    0 -> tvPosicao.setTextColor(android.graphics.Color.parseColor("#FFD700"))
                    1 -> tvPosicao.setTextColor(android.graphics.Color.parseColor("#C0C0C0"))
                    2 -> tvPosicao.setTextColor(android.graphics.Color.parseColor("#CD7F32"))
                    else -> tvPosicao.setTextColor(android.graphics.Color.BLACK)
                }

                // 2. Destaque do usuário logado na lista
                if (user.email == emailLogado) {
                    tvNomeAluno.text = "${user.nome} (Você)"
                    root.setCardBackgroundColor(android.graphics.Color.parseColor("#E3F2FD")) // Azul bem clarinho
                    root.strokeWidth = 4
                    root.strokeColor = android.graphics.Color.parseColor("#004AF7")
                } else {
                    tvNomeAluno.text = user.nome
                    root.setCardBackgroundColor(android.graphics.Color.WHITE)
                    root.strokeWidth = 2
                    root.strokeColor = android.graphics.Color.BLACK
                }
                
                tvPontuacao.text = String.format("%,d pts", user.pontos)
            }
        }

        override fun getItemCount() = users.size
    }
}
