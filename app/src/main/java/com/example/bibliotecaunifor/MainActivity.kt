package com.example.bibliotecaunifor

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.databinding.TelaHomeUsuarioBinding
import com.example.bibliotecaunifor.usuario.catalogo.CatalogoActivity
import com.example.bibliotecaunifor.usuario.perfil.PerfilUsuarioActivity
import com.example.bibliotecaunifor.usuario.salas.SalasActivity

import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.example.bibliotecaunifor.usuario.ranking.RankingUsuarioActivity
import com.example.bibliotecaunifor.usuario.emprestimos.MeusLivrosActivity
import com.example.bibliotecaunifor.usuario.historico.HistoricoActivity
import com.example.bibliotecaunifor.usuario.jogos.JogosTabuleiroActivity
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.usuario.utils.FirestoreSeedData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// A lógica da Home do usuário está aqui
class MainActivity : AppCompatActivity() {

    private lateinit var binding: TelaHomeUsuarioBinding

    private val db = Firebase.firestore
    private var devolucoesListener: com.google.firebase.firestore.ListenerRegistration? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Firebase.auth.currentUser == null) {
            val intent = Intent(this, com.example.bibliotecaunifor.auth.LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // enableEdgeToEdge()
        // FirestoreSeedData.popularTudo()
        binding = TelaHomeUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // https://firebase.google.com/docs/auth/android/start?hl=pt-br#access_user_information
        //
//        val usuarioAtual = Firebase.auth.currentUser
//        usuarioAtual?.let {
//            val nomeParaExibir = it.displayName ?: "nome"
//            binding.tvGreeting.text = "Olá $nomeParaExibir, \no que você quer fazer hoje?"
//        }

        pegarNomeUsuario { nome ->
            binding.tvGreeting.apply {
                text = "Olá $nome, \no que você quer fazer hoje?"
            // animação;  Não dá nem pra ver kk; Talvez retirar
                alpha = 0f
                animate().alpha(1f).setDuration(500).start()
            }
        }

        // Setup Navigation
        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, R.id.navigation_home_aluno)

        // Carrega a foto de perfil do usuário em tempo real
        val userUid = Firebase.auth.currentUser?.uid
        if (userUid != null) {
            db.collection("usuario").document(userUid)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        val fotoUrl = snapshot.getString("fotoUrl")
                        if (!fotoUrl.isNullOrEmpty()) {
                            Glide.with(this@MainActivity)
                                .load(fotoUrl)
                                .circleCrop()
                                .into(binding.ivProfile)
                        }
                    }
                }
        }

        configurarSecaoDevolucoes()

        // Header

        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, PerfilUsuarioActivity::class.java))
        }
        binding.btnRanking.setOnClickListener {
            startActivity(Intent(this, RankingUsuarioActivity::class.java))
        }

        // Shortcuts
        binding.cardAgendamentos.setOnClickListener {
            startActivity(Intent(this, SalasActivity::class.java))
        }
        binding.cardEmprestimos.setOnClickListener {
            startActivity(Intent(this, MeusLivrosActivity::class.java))
        }
        binding.cardHistorico.setOnClickListener {
            startActivity(Intent(this, HistoricoActivity::class.java))
        }
        binding.cardJogos.setOnClickListener {
            startActivity(Intent(this, JogosTabuleiroActivity::class.java))
        }

        // Search Bar enter
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val query = binding.etSearch.text.toString().trim()
            val intent = Intent(this, CatalogoActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                if (query.isNotEmpty()) {
                    putExtra("QUERY", query)
                }
            }
            startActivity(intent)
            true
        }


        // Isso que é uma lógica para fazer lá na prente. Deixar assim para sinalizar.
//        binding.tvDevolucoesList.text = "PLACEHOLDER CORRIGIR!"


        // Devolucoes List fake click - leva para os detalhes do livro
//        binding.tvDevolucoesList.setOnClickListener {
//            val intent = Intent(this, com.example.bibliotecaunifor.usuario.reserva.DetalhesLivroActivity::class.java).apply {
//                putExtra("title", "Dom Casmurro")
//                putExtra("author", "Machado de Assis")
//                putExtra("available", 0)
//            }
//            startActivity(intent)
//        }
    }
    // Fazer um mock para depois entender como conectar no firestro. Acho que seria algo assim, baseado na documentação:
    // https://firebase.google.com/docs/firestore/query-data/get-data?hl=pt-br#custom_objects
    // Estruturado com auxilio do chat da documentação:
    // private para não ser chamado por outras telas sem querer no biding.
    private fun configurarSecaoDevolucoes() {
        val uid = Firebase.auth.currentUser?.uid ?: return

        binding.tvDevolucoesList.text = "Buscando suas devoluções..."

        devolucoesListener?.remove()

        devolucoesListener = db.collection("emprestimos")
            .whereEqualTo("idUsuario", uid)
            .whereEqualTo("status", "ativo")
            .whereEqualTo("tipoItem", "livro")
            .addSnapshotListener { result, error ->
                if (error != null) {
                    binding.tvDevolucoesList.text = "Erro ao carregar devoluções."
                    return@addSnapshotListener
                }

                if (result == null || result.isEmpty) {
                    binding.tvDevolucoesList.text =
                        "Tudo em dia! \nVocê não tem devoluções para esta semana."
                } else {
                    val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
                    val livros = result.mapNotNull { doc ->
                        val titulo = doc.getString("tituloLivro") ?: "Livro"
                        val dataTimestamp = doc.getTimestamp("dataDevolucaoPrevista")

                        if (dataTimestamp != null) {
                            val dataDate = dataTimestamp.toDate()
                            val dataStr = sdf.format(dataDate)
                            val hoje = System.currentTimeMillis()

                            if (hoje > dataDate.time) {
                                "• $titulo (Atrasado: $dataStr) ⚠️"
                            } else {
                                "• $titulo (Devolver até: $dataStr)"
                            }
                        } else {
                            "• $titulo (Data não definida)"
                        }
                    }
                    binding.tvDevolucoesList.text = livros.joinToString("\n")
                }
            }

        binding.cardAlertaDevolucoes.setOnClickListener {
            val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
            val intent = Intent(this, MeusLivrosActivity::class.java)
            startActivity(intent, options.toBundle())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        devolucoesListener?.remove()
    }
}