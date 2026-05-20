package com.example.bibliotecaunifor.admin.agendamentos

import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.DialogAdminGerenciarHorariosBinding
import com.example.bibliotecaunifor.databinding.TelaAdminAgendamentosBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar

class AdminAgendamentosActivity : AppCompatActivity() {
    private lateinit var binding: TelaAdminAgendamentosBinding

    // Dados mock das mesas
    private val mesas = listOf(
        AdminMesa("Mesa 15", "2º andar", "Livre até 10:40", false),
        AdminMesa("Mesa 16", "2º andar", "Livre até 21:00", false),
        AdminMesa("Mesa 17", "2º andar", "Livre até 19:10", false),
        AdminMesa("Mesa 4", "1º andar", "Livre até 9:10", false),
        AdminMesa("Mesa 13", "2º andar", "Ocupada até 11:10", true, "1213145"),
        AdminMesa("Mesa 2", "1º andar", "Ocupada até 14:00", true, "7654321")
    )

    // Dados mock dos horários
    private val horarios = listOf(
        AdminHorario("7:10 - 8:00", true, "1213145"),
        AdminHorario("8:00 - 9:50", false),
        AdminHorario("9:50 - 10:40", true, "7654321"),
        AdminHorario("10:40 - 11:30", false),
        AdminHorario("11:30 - 12:20", false),
        AdminHorario("12:20 - 13:10", true, "9876543"),
        AdminHorario("13:10 - 14:00", false),
        AdminHorario("14:00 - 14:50", false),
        AdminHorario("14:50 - 15:40", true, "1122334"),
        AdminHorario("15:40 - 16:30", false),
        AdminHorario("16:30 - 17:20", false),
        AdminHorario("17:20 - 18:10", false)
    )

    // Padrão de dias do calendário (true = ocupado, false = livre)
    private val diasCalendario = listOf(
        true, false, true, true, true, true,
        true, true, false, true, true, true,
        false, true, true, false, true, true,
        true, false, true, true, false, true,
        true, true, true, false, true, false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaAdminAgendamentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includeToolbar.btnBack.setOnClickListener { finish() }

        NavigationUtils.setupAdminNavigation(this, binding.bottomNavigation, R.id.navigation_salas)

        setupEstacoes()
        setupCalendario()

        // Chip switching
        binding.chipEstacoes.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.viewFlipper.displayedChild = 0
        }

        binding.chipCordelteca.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.viewFlipper.displayedChild = 1
        }
    }

    private fun setupEstacoes() {
        binding.recyclerMesas.layoutManager = LinearLayoutManager(this)
        binding.recyclerMesas.adapter = AdminMesaAdapter(mesas) { mesa ->
            showGerenciarHorariosDialog(mesa)
        }
    }

    private fun showGerenciarHorariosDialog(mesa: AdminMesa) {
        val dialogBinding = DialogAdminGerenciarHorariosBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvTituloDialog.text = "Gerenciar Horários - ${mesa.nome}"

        val slots = listOf(
            "07:10 - 08:00", "08:00 - 08:50", "08:50 - 09:40", "09:40 - 10:30",
            "10:40 - 11:30", "11:30 - 12:20", "12:20 - 13:10", "13:10 - 14:00",
            "14:00 - 14:50", "14:50 - 15:40", "15:40 - 16:30", "16:30 - 17:20",
            "17:20 - 18:10", "18:10 - 19:00", "19:00 - 19:50", "19:50 - 20:40",
            "20:40 - 21:30"
        )
        val horariosPopup = slots.mapIndexed { index, s ->
            AdminHorario(s, index % 4 == 0)
        }

        dialogBinding.recyclerHorariosPopup.layoutManager = LinearLayoutManager(this)
        dialogBinding.recyclerHorariosPopup.adapter = AdminHorarioPopupAdapter(horariosPopup)

        dialog.show()
    }

    private fun setupCalendario() {
        val grid = binding.gridCalendario
        grid.removeAllViews()

        val diasSemana = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
        for (dia in diasSemana) {
            val header = android.widget.TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = dpToPx(36)
            params.height = dpToPx(20)
            params.setMargins(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(4))
            header.layoutParams = params
            header.text = dia
            header.textSize = 10f
            header.gravity = android.view.Gravity.CENTER
            header.setTextColor(getColor(R.color.unifor_marinho_dark))
            header.setTypeface(null, android.graphics.Typeface.BOLD)
            grid.addView(header)
        }

        for (i in diasCalendario.indices) {
            val isOcupado = diasCalendario[i]
            val diaNumero = i + 1

            val circulo = android.widget.TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = dpToPx(36)
            params.height = dpToPx(36)
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            circulo.layoutParams = params
            circulo.text = diaNumero.toString()
            circulo.textSize = 13f
            circulo.gravity = android.view.Gravity.CENTER
            circulo.setTextColor(getColor(R.color.white))
            circulo.setTypeface(null, android.graphics.Typeface.BOLD)

            circulo.setBackgroundResource(
                if (isOcupado) R.drawable.bg_circle_red else R.drawable.bg_circle_green
            )

            circulo.setOnClickListener {
                mostrarHorarios(diaNumero)
            }

            grid.addView(circulo)
        }
    }

    private fun mostrarHorarios(dia: Int = 0) {
        binding.tvHorariosTitulo.visibility = View.VISIBLE
        binding.tvHorariosTitulo.text = if (dia > 0) "Horários — Dia $dia" else "Horários do dia"
        binding.recyclerHorarios.visibility = View.VISIBLE
        binding.recyclerHorarios.layoutManager = LinearLayoutManager(this)
        binding.recyclerHorarios.adapter = AdminHorarioAdapter(horarios)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
