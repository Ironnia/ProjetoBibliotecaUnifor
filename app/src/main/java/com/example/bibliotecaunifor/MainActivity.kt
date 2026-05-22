package com.example.bibliotecaunifor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.databinding.TelaHomeUsuarioBinding
import com.example.bibliotecaunifor.usuario.catalogo.CatalogoActivity
import com.example.bibliotecaunifor.usuario.perfil.PerfilUsuarioActivity
import com.example.bibliotecaunifor.usuario.salas.SalasActivity

import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.example.bibliotecaunifor.usuario.ranking.RankingUsuarioActivity
import com.example.bibliotecaunifor.usuario.alugueis.AlugueisActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// A lógica da Home do usuário está aqui
class MainActivity : AppCompatActivity() {

    private lateinit var binding: TelaHomeUsuarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        // teste do crashlytics:
//        // https://firebase.google.com/docs/crashlytics/android/get-started?hl=pt-br#force-test-crash
//        throw RuntimeException("Test Crash") // Force a crash

        // enableEdgeToEdge()
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
            binding.tvGreeting.text = "Olá $nome, \no que você quer fazer hoje?"
        }

        // Setup Navigation
        NavigationUtils.setupBottomNavigation(this, binding.bottomNavigation, R.id.navigation_home)

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
            startActivity(Intent(this, com.example.bibliotecaunifor.usuario.emprestimos.MeusLivrosActivity::class.java))
        }
        binding.cardHistorico.setOnClickListener {
            startActivity(Intent(this, com.example.bibliotecaunifor.usuario.historico.HistoricoActivity::class.java))
        }
        binding.cardJogos.setOnClickListener {
            startActivity(Intent(this, com.example.bibliotecaunifor.usuario.jogos.JogosTabuleiroActivity::class.java))
        }

        // Search Bar fake enter
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            startActivity(Intent(this, CatalogoActivity::class.java))
            true
        }

        // Devolucoes List fake click - leva para os detalhes do livro
        binding.tvDevolucoesList.setOnClickListener {
            val intent = Intent(this, com.example.bibliotecaunifor.usuario.reserva.DetalhesLivroActivity::class.java).apply {
                putExtra("title", "Dom Casmurro")
                putExtra("author", "Machado de Assis")
                putExtra("available", 0)
            }
            startActivity(intent)
        }
    }
}