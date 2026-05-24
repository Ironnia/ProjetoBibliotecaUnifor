package com.example.bibliotecaunifor.usuario.perfil

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaPerfilUsuarioBinding
import com.example.bibliotecaunifor.usuario.catalogo.CatalogoActivity

import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.example.bibliotecaunifor.usuario.configuracoes.ConfiguracoesUsuarioActivity
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.edit
import com.example.bibliotecaunifor.pegarEmailUsuario
import com.example.bibliotecaunifor.pegarNomeUsuario

class PerfilUsuarioActivity : AppCompatActivity() {
    private lateinit var binding: TelaPerfilUsuarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaPerfilUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nome do aluno:
        pegarNomeUsuario { nome ->
            // como é só o nome do usuário colocar "$nome" dava erro.
            binding.tvName.text = nome
        }
        binding.tvMatricula.text = "Email: ${pegarEmailUsuario()}"


        NavigationUtils.setupBottomNavigation(this, binding.bottomNavigation, com.example.bibliotecaunifor.R.id.navigation_perfil)

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
}
