package com.example.bibliotecaunifor.usuario.utils

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.admin.AdminHomeActivity
import com.example.bibliotecaunifor.admin.acervo.AdminAcervoActivity
import com.example.bibliotecaunifor.admin.agendamentos.AdminAgendamentosActivity
import com.example.bibliotecaunifor.admin.jogos.AdminJogosActivity
import com.example.bibliotecaunifor.usuario.catalogo.CatalogoActivity
import com.example.bibliotecaunifor.usuario.perfil.PerfilUsuarioActivity
import com.example.bibliotecaunifor.usuario.salas.SalasActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavigationUtils {
    fun navegacaoAluno(activity: Activity, bottomNavigationView: BottomNavigationView, currentItemId: Int) {
       // Deixando mais fácil de escrever.
        bottomNavigationView.apply {
            labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
            selectedItemId = currentItemId

            setOnItemSelectedListener { item ->
                // as telas "secundárias" estavam com o home desativado.
                val estouNaTelaAtual = when (item.itemId) {
                    R.id.navigation_home_aluno -> activity is MainActivity
                    R.id.navigation_catalogo_aluno -> activity is CatalogoActivity
                    R.id.navigation_salas_aluno -> activity is SalasActivity
                    R.id.navigation_perfil_aluno -> activity is PerfilUsuarioActivity
                    else -> false
                }

                if (estouNaTelaAtual) return@setOnItemSelectedListener true

                val destino = when (item.itemId) {
                    R.id.navigation_home_aluno -> MainActivity::class.java
                    R.id.navigation_catalogo_aluno -> CatalogoActivity::class.java
                    R.id.navigation_salas_aluno -> SalasActivity::class.java
                    R.id.navigation_perfil_aluno -> PerfilUsuarioActivity::class.java
                    else -> null
                }

                destino?.let { telaProxima ->
                    // checar de segurança
                    val telaAtual = telaProxima.isInstance(activity)

                    if (!telaAtual) {
                        // agora temos a tal da fluidez e memória.
                        val intent = Intent(activity, telaProxima).apply {
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        }
                        // Animação com o a recomendação da google para evitar glitches visuais.
                        val options = ActivityOptions.makeCustomAnimation(activity, 0, 0)

                        activity.startActivity(intent, options.toBundle())
                        // resolve o problema dos icones com realce nas telas erradas:
                        return@setOnItemSelectedListener false
                    }
                }
                true
            }
        }
    }
    fun navegacaoAdmin(activity: Activity, bottomNavigationView: BottomNavigationView, currentItemId: Int) {
        bottomNavigationView.apply {
            labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
            selectedItemId = currentItemId

            setOnItemSelectedListener { item ->
                //if (item.itemId == currentItemId) return@setOnItemSelectedListener true
                val estouNaTelaAtual = when (item.itemId) {
                    R.id.navigation_home_admin -> activity is AdminHomeActivity
                    R.id.navigation_catalogo_admin -> activity is AdminAcervoActivity
                    R.id.navigation_salas_admin -> activity is AdminAgendamentosActivity
                    R.id.navigation_jogos_admin -> activity is AdminJogosActivity
                    else -> false
                }

                if (estouNaTelaAtual) return@setOnItemSelectedListener true

                val destino = when (item.itemId) {
                    R.id.navigation_home_admin -> AdminHomeActivity::class.java
                    R.id.navigation_catalogo_admin -> AdminAcervoActivity::class.java
                    R.id.navigation_salas_admin -> AdminAgendamentosActivity::class.java
                    R.id.navigation_jogos_admin -> AdminJogosActivity::class.java
                    else -> null
                }

                // Mais ktx esse takeIf é para filtrar se a tela é diferente da atual.
                destino?.takeIf { !it.isInstance(activity) }?.let { classe ->
                    val intent = Intent(activity, classe).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }
                    val options = ActivityOptions.makeCustomAnimation(activity, 0, 0)
                    activity.startActivity(intent, options.toBundle())
                    return@setOnItemSelectedListener false
                }
                true
            }
        }
    }
}
