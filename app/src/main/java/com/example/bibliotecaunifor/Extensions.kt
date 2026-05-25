package com.example.bibliotecaunifor

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

// global desse carinha Alert que precisa me muito canto.
fun AppCompatActivity.mostrarDialogoSimples(
    titulo: String,
    mensagem: String,
    textoBotao: String = "Ok",
    aoConfirmar: (() -> Unit)? = null
) {
    AlertDialog.Builder(this)
        .setTitle(titulo)
        .setMessage(mensagem)
        .setPositiveButton(textoBotao) { _, _ ->
            aoConfirmar?.invoke()
        }
        .show()
}
// Versão modificada com ajuda do chat de documentação: (Teoricamente é para funcionar com quase tudo.
// colocar as coisas opcionais com um ?= null
fun AppCompatActivity.mostrarDialogo(
    titulo: String,
    mensagem: String,
    textoPositivo: String = "Ok",
    textoNegativo: String? = null,
    layoutCustomizado: android.view.View? = null, //nosso QRCODE!
    aoConfirmar: (() -> Unit)? = null
) {
    AlertDialog.Builder(this).apply {
        setTitle(titulo)
        setMessage(mensagem)
        layoutCustomizado?.let { setView(it) } // Se tiver layout, ele coloca
        setPositiveButton(textoPositivo) { _, _ -> aoConfirmar?.invoke() }
        textoNegativo?.let { setNegativeButton(it, null) }
        show()
    }
}



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

// o email já está na autenticação estão é só chamar, não precisa esperar o Firestore E verificar se está vazio ou não.
fun pegarEmailUsuario(): String {
    return Firebase.auth.currentUser?.email ?: "Email não disponível"
}