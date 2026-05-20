package com.example.bibliotecaunifor.usuario.salas

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.databinding.TelaAgendarSalaBinding

class AgendarSalaActivity : AppCompatActivity() {
    private lateinit var binding: TelaAgendarSalaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAgendarSalaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includeToolbar.btnBack.setOnClickListener {
            finish()
        }

        // Setup Spinners
        val salas = listOf("Sala 01 - Bloco T", "Sala 02 - Bloco T", "Sala 03 - Bloco T", "Sala 04 - Bloco Z")
        val horarios = listOf("08:00 - 09:30", "10:00 - 11:30", "14:00 - 15:30", "16:00 - 17:30")

        binding.spinnerSalas.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, salas)
        binding.spinnerHorarios.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, horarios)

        binding.btnConfirmar.setOnClickListener {
            Toast.makeText(this, "Agendamento realizado com sucesso!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
