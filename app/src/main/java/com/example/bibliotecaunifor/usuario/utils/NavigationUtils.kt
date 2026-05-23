package com.example.bibliotecaunifor.usuario.utils

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.admin.AdminHomeActivity
import com.example.bibliotecaunifor.admin.acervo.AdminAcervoActivity
import com.example.bibliotecaunifor.admin.agendamentos.AdminAgendamentosActivity
import com.example.bibliotecaunifor.admin.emprestimos.AdminEmprestimosActivity
import com.example.bibliotecaunifor.usuario.catalogo.CatalogoActivity
import com.example.bibliotecaunifor.usuario.perfil.PerfilUsuarioActivity
import com.example.bibliotecaunifor.usuario.salas.SalasActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavigationUtils {
    fun setupBottomNavigation(activity: Activity, bottomNavigationView: BottomNavigationView, currentItemId: Int) {
       // Deixando mais fácil de escrever.
        bottomNavigationView.apply {
            labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
            selectedItemId = currentItemId

            setOnItemSelectedListener { item ->
//                val isCurrentActivityTab = when (item.itemId) {
//                    R.id.navigation_home -> activity is MainActivity
//                    R.id.navigation_catalogo -> activity is CatalogoActivity
//                    R.id.navigation_salas -> activity is SalasActivity
//                    R.id.navigation_perfil -> activity is PerfilUsuarioActivity
//                    else -> false
//                }
            // Para evitar a tela de "piscarr, clicando pra mesma. Evita ficar criando várias da mesma tela.
            // faz o mesmo do de ciam.
                if (item.itemId == currentItemId) return@setOnItemSelectedListener true

                val destino = when (item.itemId) {
                    R.id.navigation_home -> MainActivity::class.java
                    R.id.navigation_catalogo -> CatalogoActivity::class.java
                    R.id.navigation_salas -> SalasActivity::class.java
                    R.id.navigation_perfil -> PerfilUsuarioActivity::class.java
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
//                        // O antigo destruia a tela. Agora vai trazer a tela que foi usada, assim fica meio que "salvo" o estado da tela. Não recria ela.
//                        it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
//                        activity.startActivity(it)

                }
                true
            }
        }
    }
    fun setupAdminNavigation(activity: Activity, bottomNavigationView: BottomNavigationView, currentItemId: Int) {
        bottomNavigationView.apply {
            labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
            selectedItemId = currentItemId

            setOnItemSelectedListener { item ->
                if (item.itemId == currentItemId) return@setOnItemSelectedListener true

                val destino = when (item.itemId) {
                    R.id.navigation_home -> AdminHomeActivity::class.java
                    R.id.navigation_catalogo -> AdminAcervoActivity::class.java
                    R.id.navigation_salas -> AdminAgendamentosActivity::class.java
                    R.id.navigation_perfil -> AdminEmprestimosActivity::class.java
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
