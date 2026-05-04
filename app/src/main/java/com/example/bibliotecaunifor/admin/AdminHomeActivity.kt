package com.example.bibliotecaunifor.admin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaAdminHomeBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils

class AdminHomeActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.cardAcervo.setOnClickListener {
            startActivity(android.content.Intent(this, com.example.bibliotecaunifor.admin.acervo.AdminAcervoActivity::class.java))
        }

        binding.cardEmprestimos.setOnClickListener {
            startActivity(android.content.Intent(this, com.example.bibliotecaunifor.admin.emprestimos.AdminEmprestimosActivity::class.java))
        }

        binding.cardUsuarios.setOnClickListener {
            startActivity(android.content.Intent(this, com.example.bibliotecaunifor.admin.usuarios.AdminUsuariosActivity::class.java))
        }

        binding.cardSalas.setOnClickListener {
            startActivity(android.content.Intent(this, com.example.bibliotecaunifor.admin.agendamentos.AdminAgendamentosActivity::class.java))
        }

        // A barra de pesquisa agora é apenas um EditText para digitação, sem redirecionamento ao clicar.

        // Próximas Devoluções - Atalho rápido para Empréstimos
        binding.cardDevolucoes.setOnClickListener {
            startActivity(android.content.Intent(this, com.example.bibliotecaunifor.admin.emprestimos.AdminEmprestimosActivity::class.java))
        }

        binding.ivProfile.setOnClickListener {
            // Em vez de fechar, vamos para a tela de Usuários (que é o "Perfil" do Admin)
            startActivity(android.content.Intent(this, com.example.bibliotecaunifor.admin.usuarios.AdminUsuariosActivity::class.java))
        }

        NavigationUtils.setupAdminNavigation(this, binding.bottomNavigation, R.id.navigation_home)
    }
}
