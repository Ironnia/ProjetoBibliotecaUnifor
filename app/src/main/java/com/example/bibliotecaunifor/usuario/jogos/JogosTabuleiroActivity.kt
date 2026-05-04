package com.example.bibliotecaunifor.usuario.jogos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaJogosTabuleiroBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.android.material.button.MaterialButton

class JogosTabuleiroActivity : AppCompatActivity() {
    private lateinit var binding: TelaJogosTabuleiroBinding
    private var showingMeusJogos = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaJogosTabuleiroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.chipDisponivelJogos.setOnClickListener {
            showingMeusJogos = false
            setupRecyclerView()
        }

        binding.chipMeusJogos.setOnClickListener {
            showingMeusJogos = true
            setupRecyclerView()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        NavigationUtils.setupBottomNavigation(this, binding.bottomNavigation, R.id.navigation_home)
    }

    private fun setupRecyclerView() {
        val items = if (showingMeusJogos) {
            listOf(
                JogoItem("Dixit", "3-6 Jogadores | 30 min", "Devolver em: 06/05", "Devolver")
            )
        } else {
            listOf(
                JogoItem("Catan", "3-4 Jogadores | 90 min", "Disponível", "Reservar"),
                JogoItem("Ticket to Ride", "2-5 Jogadores | 60 min", "Disponível", "Reservar"),
                JogoItem("Exploding Kittens", "2-5 Jogadores | 15 min", "Disponível", "Reservar")
            )
        }

        binding.rvJogos.layoutManager = LinearLayoutManager(this)
        binding.rvJogos.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            inner class JogoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val nome: TextView = view.findViewById(R.id.tvJogoNome)
                val desc: TextView = view.findViewById(R.id.tvJogoDesc)
                val btn: MaterialButton = view.findViewById(R.id.btnAcaoJogo)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_jogo, parent, false)
                return JogoViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val item = items[position]
                (holder as JogoViewHolder).nome.text = item.nome
                holder.desc.text = item.descricao
                holder.btn.text = item.acao
                
                holder.btn.setOnClickListener {
                    if (showingMeusJogos) {
                        showDevolucaoDialog(item)
                    } else {
                        showReservaDialog(item)
                    }
                }
            }

            override fun getItemCount() = items.size
        }
    }

    private fun showReservaDialog(item: JogoItem) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Reserva")
            .setMessage("Deseja reservar o jogo \"${item.nome}\" por 2 horas?")
            .setPositiveButton("Confirmar") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Reserva Realizada")
                    .setMessage("O jogo foi reservado. Retire no balcão em até 15 minutos.")
                    .setPositiveButton("Ok", null)
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDevolucaoDialog(item: JogoItem) {
        AlertDialog.Builder(this)
            .setTitle("Devolver Jogo")
            .setMessage("Apresente o jogo no balcão para confirmar a devolução.")
            .setPositiveButton("Ok", null)
            .show()
    }

    data class JogoItem(val nome: String, val descricao: String, val status: String, val acao: String)
}
