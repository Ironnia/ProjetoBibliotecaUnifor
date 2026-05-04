package com.example.bibliotecaunifor.usuario.utils

import android.app.Activity
import android.content.Intent
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.usuario.catalogo.CatalogoActivity
import com.example.bibliotecaunifor.usuario.perfil.PerfilUsuarioActivity
import com.example.bibliotecaunifor.usuario.salas.SalasActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavigationUtils {
    fun setupBottomNavigation(activity: Activity, bottomNavigationView: BottomNavigationView, currentItemId: Int) {
        bottomNavigationView.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
        
        bottomNavigationView.selectedItemId = currentItemId

        bottomNavigationView.setOnItemSelectedListener { item ->
            val isCurrentActivityTab = when (item.itemId) {
                R.id.navigation_home -> activity is MainActivity
                R.id.navigation_catalogo -> activity is CatalogoActivity
                R.id.navigation_salas -> activity is SalasActivity
                R.id.navigation_perfil -> activity is PerfilUsuarioActivity
                else -> false
            }

            if (!isCurrentActivityTab) {
                val intent = when (item.itemId) {
                    R.id.navigation_home -> Intent(activity, MainActivity::class.java)
                    R.id.navigation_catalogo -> Intent(activity, CatalogoActivity::class.java)
                    R.id.navigation_salas -> Intent(activity, SalasActivity::class.java)
                    R.id.navigation_perfil -> Intent(activity, PerfilUsuarioActivity::class.java)
                    else -> null
                }
                
                intent?.let {
                    it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    activity.startActivity(it)
                }
            }
            true
        }
    }
    fun setupAdminNavigation(activity: Activity, bottomNavigationView: BottomNavigationView, currentItemId: Int) {
        bottomNavigationView.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
        bottomNavigationView.selectedItemId = currentItemId

        bottomNavigationView.setOnItemSelectedListener { item ->
            val isCurrentActivityTab = when (item.itemId) {
                R.id.navigation_home -> activity is com.example.bibliotecaunifor.admin.AdminHomeActivity
                R.id.navigation_catalogo -> activity is com.example.bibliotecaunifor.admin.acervo.AdminAcervoActivity
                R.id.navigation_salas -> activity is com.example.bibliotecaunifor.admin.agendamentos.AdminAgendamentosActivity
                R.id.navigation_perfil -> activity is com.example.bibliotecaunifor.admin.usuarios.AdminUsuariosActivity
                else -> false
            }

            if (!isCurrentActivityTab) {
                val intent = when (item.itemId) {
                    R.id.navigation_home -> Intent(activity, com.example.bibliotecaunifor.admin.AdminHomeActivity::class.java)
                    R.id.navigation_catalogo -> Intent(activity, com.example.bibliotecaunifor.admin.acervo.AdminAcervoActivity::class.java)
                    R.id.navigation_salas -> Intent(activity, com.example.bibliotecaunifor.admin.agendamentos.AdminAgendamentosActivity::class.java)
                    R.id.navigation_perfil -> Intent(activity, com.example.bibliotecaunifor.admin.usuarios.AdminUsuariosActivity::class.java)
                    else -> null
                }

                intent?.let {
                    it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    activity.startActivity(it)
                }
            }
            true
        }
    }
}
