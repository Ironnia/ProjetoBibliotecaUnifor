package com.example.bibliotecaunifor.usuario.perfil

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaPerfilUsuarioBinding
import com.example.bibliotecaunifor.pegarEmailUsuario
import com.example.bibliotecaunifor.pegarNomeUsuario
import com.example.bibliotecaunifor.usuario.configuracoes.ConfiguracoesUsuarioActivity
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class PerfilUsuarioActivity : AppCompatActivity() {
    private lateinit var binding: TelaPerfilUsuarioBinding
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private var userListener: ListenerRegistration? = null
    private var rankingListener: ListenerRegistration? = null
    private var loansListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaPerfilUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Preenche Nome e Email do usuário
        pegarNomeUsuario { nome ->
            binding.tvName.text = nome
        }
        binding.tvMatricula.text = "Email: ${pegarEmailUsuario()}"

        // Configura a navegação inferior
        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, com.example.bibliotecaunifor.R.id.navigation_perfil_aluno)

        setupListeners()

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, ConfiguracoesUsuarioActivity::class.java))
        }

        // Ícone estático (sem clique de câmera/galeria) conforme solicitado

        binding.btnHistorico.setOnClickListener {
            startActivity(Intent(this, com.example.bibliotecaunifor.usuario.historico.HistoricoActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sair")
                .setMessage("Deseja realmente sair da sua conta?")
                .setPositiveButton("Sair") { _, _ ->
                    val prefs = getSharedPreferences("login", Context.MODE_PRIVATE)
                    prefs.edit { putBoolean("lembrar_me", false) }

                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, com.example.bibliotecaunifor.auth.LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }


    }

    private fun setupListeners() {
        val uid = auth.currentUser?.uid ?: return

        // Escuta dados do usuário (pontos, medalha ouro e foto cadastrada)
        userListener = db.collection("usuario").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val pontosRaw = snapshot.get("pontos")
                    val pontos = when (pontosRaw) {
                        is Number -> pontosRaw.toInt()
                        is String -> pontosRaw.toIntOrNull() ?: 0
                        else -> 0
                    }
                    binding.tvCountPontos.text = String.format("%,d", pontos)
                    binding.llMedalOuro.visibility = if (pontos >= 100) View.VISIBLE else View.GONE

                    // Carrega de forma passiva a fotoUrl se houver no banco, sem poder editar no app
                    val fotoUrl = snapshot.getString("fotoUrl")
                    if (!fotoUrl.isNullOrEmpty()) {
                        Glide.with(this@PerfilUsuarioActivity)
                            .load(fotoUrl)
                            .circleCrop()
                            .into(binding.ivProfile)
                    }
                }
            }

        // Escuta a classificação do ranking e medalha Top 10 (Troféu)
        rankingListener = db.collection("usuario")
            .orderBy("pontos", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val usersList = snapshot.documents
                    val posicao = usersList.indexOfFirst { it.id == uid }
                    if (posicao != -1) {
                        val pos = posicao + 1
                        binding.tvCountRanking.text = "${pos}º"
                        binding.llMedalTop5.visibility = if (pos <= 10) View.VISIBLE else View.GONE
                    } else {
                        binding.tvCountRanking.text = "-"
                        binding.llMedalTop5.visibility = View.GONE
                    }
                }
            }

        // Escuta contagem de empréstimos
        loansListener = db.collection("emprestimos")
            .whereEqualTo("idUsuario", uid)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val totalLivros = snapshot.size()
                    binding.tvCountLivros.text = totalLivros.toString()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        userListener?.remove()
        rankingListener?.remove()
        loansListener?.remove()
    }
}
