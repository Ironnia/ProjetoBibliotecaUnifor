package com.example.bibliotecaunifor.usuario.salas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.Sala
import com.example.bibliotecaunifor.databinding.TelaSalasBinding
import com.example.bibliotecaunifor.databinding.DialogAdminGerenciarHorariosBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.bibliotecaunifor.admin.agendamentos.AdminHorario
import com.example.bibliotecaunifor.mostrarAviso
import com.example.bibliotecaunifor.pegarNomeUsuario
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Date
import java.util.Locale

class SalasActivity : AppCompatActivity() {
    private lateinit var binding: TelaSalasBinding
    private var showingMeusAgendamentos = false

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private lateinit var salaAdapter: SalaAdapter
    private lateinit var agendamentoAdapter: AgendamentoAdapter
    private var salasListener: ListenerRegistration? = null
    private var agendamentosListener: ListenerRegistration? = null

    // NOVAS VARIÁVEIS PARA FILTROS EM TEMPO REAL
    private var todasAsSalas = listOf<Sala>()
    private var todosOsAgendamentos = listOf<AgendamentoDb>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = TelaSalasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAdapters()
        carregarSalas()
        setupFiltros()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnMeusAgendamentos.setOnClickListener {
            showingMeusAgendamentos = !showingMeusAgendamentos
            if (showingMeusAgendamentos) {
                binding.tvTitle.text = "Meus Agendamentos"
                binding.btnMeusAgendamentos.text = "Agendar Sala"
                binding.chipGroupFiltros.visibility = View.GONE
                carregarMeusAgendamentos()
            } else {
                binding.tvTitle.text = "Agendar sala de estudos"
                binding.btnMeusAgendamentos.text = "Meus Agendamentos"
                binding.chipGroupFiltros.visibility = View.VISIBLE
                carregarSalas()
            }
        }

        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, R.id.navigation_salas_aluno)
    }

    private fun setupAdapters() {
        salaAdapter = SalaAdapter(emptyList()) { sala -> mostrarHorariosSala(sala) }
        agendamentoAdapter = AgendamentoAdapter(
                emptyList(),
        { ag -> mostrarQrCodeDialog(ag) }, // Abre o QR Code
        { ag -> mostrarSalaCancela(ag) }     // Cancela
        )

        binding.rvMesas.layoutManager = LinearLayoutManager(this)
        binding.rvMesas.adapter = salaAdapter
    }

    private fun carregarSalas() {
        // CÓDIGO ANTIGO COMENTADO CONFORME PEDIDO
        /*
        agendamentosListener?.remove()
        agendamentosListener = null

        salasListener?.remove()
        salasListener = db.collection("salas").addSnapshotListener { snapshot, error ->
            if (error != null) {
                FirebaseCrashlytics.getInstance().recordException(error)
                return@addSnapshotListener
            }
            val lista = snapshot?.toObjects<Sala>() ?: emptyList()
            salaAdapter.atualizarLista(lista)
            binding.rvMesas.adapter = salaAdapter

            if (lista.isEmpty()) {
                binding.tvEmptyStateMesas.text = "Nenhuma sala disponível no momento."
                binding.tvEmptyStateMesas.visibility = View.VISIBLE
                binding.rvMesas.visibility = View.GONE
            } else {
                binding.tvEmptyStateMesas.visibility = View.GONE
                binding.rvMesas.visibility = View.VISIBLE
            }
        }
        */

        // NOVA LÓGICA REATIVA DE SALAS + AGENDAMENTOS
        agendamentosListener?.remove()
        val dataHoje = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())
        
        agendamentosListener = db.collection("agendamentos")
            .whereEqualTo("data", dataHoje)
            .addSnapshotListener { snapAg, errorAg ->
                if (errorAg != null) {
                    FirebaseCrashlytics.getInstance().recordException(errorAg)
                    return@addSnapshotListener
                }
                val ags = snapAg?.toObjects<AgendamentoDb>() ?: emptyList()
                
                salasListener?.remove()
                salasListener = db.collection("salas").addSnapshotListener { snapSalas, errorSalas ->
                    if (errorSalas != null) {
                        FirebaseCrashlytics.getInstance().recordException(errorSalas)
                        return@addSnapshotListener
                    }
                    // CÓDIGO ANTIGO COMENTADO CONFORME PEDIDO
                    /*
                    val listaSalas = snapSalas?.toObjects<Sala>() ?: emptyList()
                    
                    // Passamos a lista de agendamentos para o adapter
                    salaAdapter.atualizarLista(listaSalas, ags)
                    binding.rvMesas.adapter = salaAdapter

                    if (listaSalas.isEmpty()) {
                        binding.tvEmptyStateMesas.text = "Nenhuma sala disponível no momento."
                        binding.tvEmptyStateMesas.visibility = View.VISIBLE
                        binding.rvMesas.visibility = View.GONE
                    } else {
                        binding.tvEmptyStateMesas.visibility = View.GONE
                        binding.rvMesas.visibility = View.VISIBLE
                    }
                    */

                    // NOVA LÓGICA DE FILTRAGEM DINÂMICA
                    todasAsSalas = snapSalas?.toObjects<Sala>() ?: emptyList()
                    todosOsAgendamentos = ags
                    aplicarFiltros()
                }
            }
    }

    private fun carregarMeusAgendamentos() {
        val uid = auth.currentUser?.uid ?: return

        salasListener?.remove()
        salasListener = null

        agendamentosListener?.remove()
        agendamentosListener = db.collection("agendamentos")
            .whereEqualTo("idUsuario", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    FirebaseCrashlytics.getInstance().recordException(error)
                    return@addSnapshotListener
                }
                val listaTotal = snapshot?.toObjects<AgendamentoDb>() ?: emptyList()
                val dataHoje = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())
                val agora = LocalTime.now()

                listaTotal.forEach { ag ->
                    if (ag.status == "pendente" && ag.data == dataHoje) {
                        try {
                            val horaInicio = LocalTime.parse(ag.horario.split(" - ")[0])
                            if (agora.isAfter(horaInicio.plusMinutes(15))) {
                                // Se achou alguém atrasado, cancela no banco silenciosamente
                                db.collection("agendamentos").document(ag.id).update("status", "expirado")
                            }
                        } catch (e: Exception) {
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }
                    }
                }

                // Filtra para não mostrar expirados na lista ativa do aluno
                val listaFiltrada = listaTotal.filter { it.status == "pendente" || it.status == "reservado" }
                agendamentoAdapter.atualizarLista(listaFiltrada)
                binding.rvMesas.adapter = agendamentoAdapter

                if (listaFiltrada.isEmpty()) {
                    binding.tvEmptyStateMesas.text = "Você não possui agendamentos no momento."
                    binding.tvEmptyStateMesas.visibility = View.VISIBLE
                    binding.rvMesas.visibility = View.GONE
                } else {
                    binding.tvEmptyStateMesas.visibility = View.GONE
                    binding.rvMesas.visibility = View.VISIBLE
                }
            }
    }

    private fun mostrarHorariosSala(sala: Sala) {
        val dialogBinding = DialogAdminGerenciarHorariosBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvTituloDialog.text = "Selecionar horário - ${sala.nome}"

        val dataHoje = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())
        val slots = listOf(
            "07:10 - 08:00", "08:00 - 08:50", "08:50 - 09:40", "09:40 - 10:30",
            "10:40 - 11:30", "11:30 - 12:20", "12:20 - 13:10", "13:10 - 14:00",
            "14:00 - 14:50", "14:50 - 15:40", "15:40 - 16:30", "16:30 - 17:20",
            "17:20 - 18:10", "18:10 - 19:00", "19:00 - 19:50", "19:50 - 20:40",
            "20:40 - 21:30"
        )

        db.collection("agendamentos")
            .whereEqualTo("idSala", sala.id)
            .whereEqualTo("data", dataHoje)
            .whereIn("status", listOf("pendente", "reservado"))
            .get()
            .addOnSuccessListener { result ->
                val agendados = result.toObjects(AgendamentoDb::class.java)
                val horariosPopup = slots.map { slot ->
                    val ag = agendados.firstOrNull { it.horario == slot }
                    val isOcupado = ag != null
                    
                    AdminHorario(
                        horario = slot,
                        isOcupado = isOcupado,
                        email = ag?.nomeUsuario ?: "",
                        idAgendamento = ag?.id,
                        idSala = sala.id,
                        nomeSala = sala.nome,
                        data = dataHoje
                    )
                }

                dialogBinding.recyclerHorariosPopup.layoutManager = LinearLayoutManager(this)
                dialogBinding.recyclerHorariosPopup.adapter = StudentHorarioPopupAdapter(horariosPopup) { selectedHorario ->
                    dialog.dismiss()
                    verificarDisponibilidadeEReservar(sala, dataHoje, selectedHorario.horario)
                }
                dialog.show()
            }
            .addOnFailureListener {
                mostrarAviso("Erro ao carregar horários. Tente novamente.")
            }
    }

    private fun verificarDisponibilidadeEReservar(sala: Sala, data: String, horario: String) {
        val uid = auth.currentUser?.uid ?: return
        
        // 1. Verifica se o aluno já tem uma reserva ativa (pendente ou reservada)
        db.collection("agendamentos")
            .whereEqualTo("idUsuario", uid)
            .whereIn("status", listOf("pendente", "reservado"))
            .get()
            .addOnSuccessListener { userReservations ->
                // CÓDIGO ANTIGO COMENTADO CONFORME PEDIDO
                /*
                if (!userReservations.isEmpty) {
                    mostrarAviso("Você já possui uma reserva ativa. Cancele em Meus Agendamentos para agendar outro.")
                } else {
                */

                // NOVA LÓGICA DE AVISO COM TERMO DE ACORDO COM O NOME
                if (!userReservations.isEmpty) {
                    val termo = obterTermo(sala.nome)
                    mostrarAviso("Você já possui uma reserva de $termo ativa. Cancele em Meus Agendamentos para agendar outro.")
                } else {
                    // 2. Verifica se o slot está livre
                    db.collection("agendamentos")
                        .whereEqualTo("idSala", sala.id)
                        .whereEqualTo("data", data)
                        .whereEqualTo("horario", horario)
                        .whereIn("status", listOf("pendente", "reservado"))
                        .get()
                        .addOnSuccessListener { result ->
                            if (!result.isEmpty) {
                                mostrarAviso("Este horário já foi reservado por outro aluno.")
                            } else {
                                salvarAgendamento(sala, data, horario)
                            }
                        }
                }
            }
            .addOnFailureListener {
                mostrarAviso("Erro ao validar agendamentos. Tente novamente.")
            }
    }

    private fun salvarAgendamento(sala: Sala, data: String, horario: String) {
        val uid = auth.currentUser?.uid ?: return
        pegarNomeUsuario { nome ->
            val novoAgendamento = hashMapOf(
                "idUsuario" to uid,
                "nomeUsuario" to nome,
                "idSala" to sala.id,
                "nomeSala" to sala.nome,
                "data" to data,
                "horario" to horario,
                "status" to "pendente"
            )
            db.collection("agendamentos").add(novoAgendamento).addOnSuccessListener {
                mostrarSalaSucesso(sala.nome, data, horario)
                if (showingMeusAgendamentos) carregarMeusAgendamentos() else carregarSalas()
            }
        }
    }

    private fun mostrarSalaSucesso(salaNome: String, data: String, horario: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sala_sucesso, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        
        // CÓDIGO ANTIGO COMENTADO CONFORME PEDIDO
        /*
        dialogView.findViewById<TextView>(R.id.tv_success_data).text = "Data: $data"
        dialogView.findViewById<TextView>(R.id.tv_success_horario).text = "Horário: $horario"
        */

        // NOVA LÓGICA DE TÍTULO DINÂMICO
        val termo = obterTermo(salaNome)
        val titulo = if (termo == "sala") "Sala reservada com sucesso!" else "Mesa reservada com sucesso!"
        dialogView.findViewById<TextView>(R.id.tv_success_title)?.text = titulo
        
        dialogView.findViewById<TextView>(R.id.tv_success_data).text = "Data: $data"
        dialogView.findViewById<TextView>(R.id.tv_success_horario).text = "Horário: $horario"
        
        dialogView.findViewById<MaterialButton>(R.id.btn_success_voltar).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun mostrarSalaCancela(ag: AgendamentoDb) {
        // CÓDIGO ANTIGO COMENTADO CONFORME PEDIDO
        /*
        AlertDialog.Builder(this)
            .setTitle("ATENÇÃO!")
            .setMessage("Deseja cancelar a reserva?\nSala: ${ag.nomeSala}\nData: ${ag.data}")
            .setPositiveButton("Sim, Cancelar") { _, _ ->
                // db.collection("agendamentos").document(ag.id).delete().addOnSuccessListener {
                db.collection("agendamentos").document(ag.id).update("status", "cancelado").addOnSuccessListener {
                    mostrarAviso("Cancelado com sucesso.")
                    carregarMeusAgendamentos()
                }
            }
            .setNegativeButton("Voltar", null).show()
        */

        val termo = obterTermo(ag.nomeSala).replaceFirstChar { it.uppercase() }
        AlertDialog.Builder(this)
            .setTitle("ATENÇÃO!")
            .setMessage("Deseja cancelar a reserva?\n$termo: ${ag.nomeSala}\nData: ${ag.data}")
            .setPositiveButton("Sim, Cancelar") { _, _ ->
                db.collection("agendamentos").document(ag.id).update("status", "cancelado").addOnSuccessListener {
                    mostrarAviso("Cancelado com sucesso.")
                    carregarMeusAgendamentos()
                }
            }
            .setNegativeButton("Voltar", null).show()
    }

    private fun mostrarQrCodeDialog(ag: AgendamentoDb) {
        try {
            val dataHoje = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())

            // ver o dia certo
            if (ag.data == dataHoje) {
                val horarioInicioStr = ag.horario.split(" - ")[0] // Pega o "14:00" de "14:00 - 15:00"
                val horaInicio = LocalTime.parse(horarioInicioStr)
                val agora = LocalTime.now()

                // ver a hora atual
                if (agora.isAfter(horaInicio.plusMinutes(15))) {
                    mostrarAviso("Sua tolerância de 15 min expirou. O agendamento foi cancelado.")
                    cancelarAgendamentoPorAtraso(ag)
                    return
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            e.printStackTrace()
        }
        // normal
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qrcode_reserva, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_qr_title)
        val tvInstructions = dialogView.findViewById<TextView>(R.id.tv_qr_instructions)
        val btnFechar = dialogView.findViewById<MaterialButton>(R.id.btn_fechar_qr)

        tvTitle.text = "Check-in: ${ag.nomeSala}"
        tvInstructions.text = "Apresente este código no balcão para validar sua entrada.\nReserva: ${ag.data} às ${ag.horario}"

        btnFechar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun cancelarAgendamentoPorAtraso(ag: AgendamentoDb) {
        db.collection("agendamentos").document(ag.id)
            .update("status", "expirado") // Marcamos como expirado para histórico
            .addOnSuccessListener {
                carregarMeusAgendamentos()
            }
            .addOnFailureListener { e ->
                FirebaseCrashlytics.getInstance().recordException(e)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        salasListener?.remove()
        agendamentosListener?.remove()
    }

    // NOVAS FUNÇÕES COMPLEMENTARES DE FILTRAGEM E NOMENCLATURA
    private fun obterTermo(nome: String): String {
        return if (nome.contains("Sala Temática", ignoreCase = true)) "sala" else "mesa"
    }

    private fun setupFiltros() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener { _, _ ->
            aplicarFiltros()
        }
    }

    private fun aplicarFiltros() {
        val filtradas = when (binding.chipGroupFiltros.checkedChipId) {
            R.id.chipDisponivel -> {
                todasAsSalas.filter { sala ->
                    val (statusTexto, isLivre) = SalaStatusHelper.calcularStatus(sala.id, todosOsAgendamentos)
                    isLivre && !statusTexto.contains("Lotada")
                }
            }
            R.id.chipOcupadas -> {
                todasAsSalas.filter { sala ->
                    val (_, isLivre) = SalaStatusHelper.calcularStatus(sala.id, todosOsAgendamentos)
                    !isLivre
                }
            }
            else -> todasAsSalas // Todas as salas/mesas
        }
        
        salaAdapter.atualizarLista(filtradas, todosOsAgendamentos)
        
        // Garante que o adapter correto seja reatribuído ao voltar de "Meus Agendamentos"
        if (binding.rvMesas.adapter != salaAdapter) {
            binding.rvMesas.adapter = salaAdapter
        }
        
        if (filtradas.isEmpty()) {
            val termoText = if (todasAsSalas.isEmpty()) "Nenhuma sala disponível no momento." else "Nenhuma mesa disponível com este filtro."
            binding.tvEmptyStateMesas.text = termoText
            binding.tvEmptyStateMesas.visibility = View.VISIBLE
            binding.rvMesas.visibility = View.GONE
        } else {
            binding.tvEmptyStateMesas.visibility = View.GONE
            binding.rvMesas.visibility = View.VISIBLE
        }
    }
}