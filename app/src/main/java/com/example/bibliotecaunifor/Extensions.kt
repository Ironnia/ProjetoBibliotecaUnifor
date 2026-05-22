package com.example.bibliotecaunifor

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Fazendo uma forma de deixar mais fácil de escrever os código de notificação do Snackbar que vão ter vários.

// Agora para ter a notificação podemos usar só "mostrarAviso(mensagem)"
// Vai funcionar para todas as classes que forem activity.
fun AppCompatActivity.mostrarAviso(mensagem: String) {
    Snackbar.make(findViewById(android.R.id.content), mensagem, Snackbar.LENGTH_SHORT).show()
}

// Mesma ideia do de cima
// fica: mostrarToast("mensagem")
fun AppCompatActivity.mostrarToast(mensagem: String, duracao: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, mensagem, duracao).show()
}


// Usar esse para mostrar o nome do usuário pegando do banco, isso depois de rastrear com a id unica.
// Exemplo de uso:
//pegarNomeUsuario { nome ->
//    binding.tvGreeting.text = "Olá $nome, \no que você quer fazer hoje?"
//}
fun pegarNomeUsuario(callback: (String) -> Unit) {
    // ?: return, vai verificar se o usuário está logado. Se não, isso cancela.
    val uid = Firebase.auth.currentUser?.uid ?: return
    Firebase.firestore.collection("usuario").document(uid).get()
        .addOnSuccessListener {
            // Se o campo nome for vazio vai sair o "PLACEHOLDER! CORRIGIR!";
            val nome = it.getString("nome") ?: "PLACEHOLDER! CORRIGIR!"
            callback(nome)
        }



}