package com.example.bibliotecaunifor.usuario.perfil

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaPerfilUsuarioBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.example.bibliotecaunifor.usuario.configuracoes.ConfiguracoesUsuarioActivity
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.edit
import com.example.bibliotecaunifor.pegarEmailUsuario
import com.example.bibliotecaunifor.pegarNomeUsuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration

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

        // Nome do aluno:
        pegarNomeUsuario { nome ->
            binding.tvName.text = nome
        }
        binding.tvMatricula.text = "Email: ${pegarEmailUsuario()}"

        // Corrigido para navigation_perfil_aluno conforme menu do aluno
        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, com.example.bibliotecaunifor.R.id.navigation_perfil_aluno)

        setupListeners()

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, ConfiguracoesUsuarioActivity::class.java))
        }

        binding.ivProfile.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Alterar Foto")
                .setMessage("Deseja escolher uma nova foto da galeria ou tirar uma foto?")
                .setPositiveButton("Galeria", null)
                .setNegativeButton("Câmera", null)
                .setNeutralButton("Cancelar", null)
                .show()
        }

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

        binding.btnPendencias.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Pendências Financeiras")
                .setMessage("Você não possui multas ou pendências no momento. Parabéns!")
                .setPositiveButton("Ok", null)
                .show()
        }

        binding.btnTags.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_tags_preferencia, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            dialogView.findViewById<MaterialButton>(R.id.btn_salvar_tags).setOnClickListener {
                dialog.dismiss()
                Snackbar.make(binding.root, "Preferências atualizadas!", Snackbar.LENGTH_SHORT).show()
            }

            dialog.show()
        }
    }

    private fun setupListeners() {
        val uid = auth.currentUser?.uid ?: return

        // 1. Escuta em tempo real os dados do usuário para preencher pontos
        userListener = db.collection("usuario").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val pontos = snapshot.getLong("pontos")?.toInt() ?: 0
                    binding.tvCountPontos.text = String.format("%,d", pontos)
                }
            }

        // 2. Escuta em tempo real todos os usuários ordenados para calcular a posição no ranking
        rankingListener = db.collection("usuario")
            .orderBy("pontos", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val usersList = snapshot.documents
                    val posicao = usersList.indexOfFirst { it.id == uid }
                    if (posicao != -1) {
                        binding.tvCountRanking.text = "${posicao + 1}º"
                    } else {
                        binding.tvCountRanking.text = "-"
                    }
                }
            }

        // 3. Escuta em tempo real os empréstimos ativos/devolvidos do usuário para contar livros lidos
        loansListener = db.collection("emprestimos")
            .whereEqualTo("idUsuario", uid)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    // Contamos a quantidade total de registros de empréstimos do aluno
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
