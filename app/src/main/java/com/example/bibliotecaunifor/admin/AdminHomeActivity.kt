package com.example.bibliotecaunifor.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.admin.acervo.AdminAcervoActivity
import com.example.bibliotecaunifor.admin.agendamentos.AdminAgendamentosActivity
import com.example.bibliotecaunifor.admin.emprestimos.AdminEmprestimosActivity
import com.example.bibliotecaunifor.admin.jogos.AdminJogosActivity
import com.example.bibliotecaunifor.databinding.TelaAdminHomeBinding
import com.example.bibliotecaunifor.pegarNomeUsuario
import com.example.bibliotecaunifor.usuario.utils.FirestoreSeedData
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminHomeActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminHomeBinding

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RODE UMA VEZ E APAGUE DEPOIS PARA POPULAR O FIRESTORE:
      // FirestoreSeedData.popularTudo()

        pegarNomeUsuario { nome ->
            binding.tvGreeting.text = "Olá $nome,\no que você quer fazer hoje?"
        }

        binding.ivProfile.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Sair da Conta")
                .setMessage("Deseja realmente sair da conta administrativa?")
                .setPositiveButton("Sim") { _, _ ->
                    auth.signOut()
                    startActivity(Intent(this, com.example.bibliotecaunifor.auth.LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Não", null)
                .show()
        }

        binding.cardAcervo.setOnClickListener {
            startActivity(Intent(this, AdminAcervoActivity::class.java))
        }

        binding.cardEmprestimos.setOnClickListener {
            startActivity(Intent(this, AdminEmprestimosActivity::class.java))
        }

        binding.cardJogos.setOnClickListener {
            startActivity(Intent(this, AdminJogosActivity::class.java))
        }

        binding.cardSalas.setOnClickListener {
            startActivity(Intent(this, AdminAgendamentosActivity::class.java))
        }

        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val busca = binding.etSearch.text.toString().trim()
            if (busca.isNotEmpty()) {
                val intent = Intent(this, AdminAcervoActivity::class.java)
                intent.putExtra("BUSCA", busca)
                startActivity(intent)
            }
            true
        }

        carregarPainelResumo()
        NavigationUtils.navegacaoAdmin(this, binding.bottomNavigation, R.id.navigation_home_admin)
    }

    private fun carregarPainelResumo() {
        val hoje = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())
        
        // Contagem de Empréstimos Ativos e Atrasados
        db.collection("emprestimos")
            .whereIn("status", listOf("ativo", "atrasado"))
            .get()
            .addOnSuccessListener { result ->
                val ativos = result.documents.count { it.getString("status") == "ativo" }
                val atrasados = result.documents.count { it.getString("status") == "atrasado" }

                binding.tvResumoEmprestimos.text = "$ativos alugados | $atrasados atrasados"

                if (atrasados > 0) {
                    binding.tvResumoEmprestimos.setTextColor(android.graphics.Color.parseColor("#FF0000"))
                } else {
                    binding.tvResumoEmprestimos.setTextColor(getColor(R.color.unifor_marinho_dark))
                }
            }
            .addOnFailureListener { e ->
                FirebaseCrashlytics.getInstance().recordException(e)
            }

        // Salas Reservadas e Ocupadas Hoje
        db.collection("agendamentos")
            .whereEqualTo("data", hoje)
            .get()
            .addOnSuccessListener { result ->
                val reservadas = result.documents.count { it.getString("status") == "reservado" }
                val ocupadas = result.documents.count { it.getString("status") == "ocupada" }

                binding.tvResumoSalas.text = "$reservadas reservadas | $ocupadas ocupadas"
            }
            .addOnFailureListener { e ->
                FirebaseCrashlytics.getInstance().recordException(e)
            }

        // livros mais reservados.
        db.collection("Acervo")
            .orderBy("reservaCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { result ->
                val livros = result.toObjects(com.example.bibliotecaunifor.crud.Entrada::class.java)

                if (livros.size >= 1) binding.tvTop1.text = "1. ${livros[0].titulo} - ${livros[0].reservaCount} vezes"
                if (livros.size >= 2) binding.tvTop2.text = "2. ${livros[1].titulo} - ${livros[1].reservaCount} vezes"
                if (livros.size >= 3) binding.tvTop3.text = "3. ${livros[2].titulo} - ${livros[2].reservaCount} vezes"
            }
    }
}
