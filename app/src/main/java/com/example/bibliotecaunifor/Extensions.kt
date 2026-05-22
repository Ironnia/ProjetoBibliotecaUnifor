package com.example.bibliotecaunifor

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

// Fazendo uma forma de deixar mais fácil de escrever os código de notificação do Snackbar que vão ter vários.

// Agora para ter a notificação podemos usar só "mostrarAviso(mensagem)"
// Vai funcionar para todas as classes que forem activity.
fun AppCompatActivity.mostrarAviso(mensagem: String) {
    Snackbar.make(findViewById(android.R.id.content), mensagem, Snackbar.LENGTH_SHORT).show()
}