package com.example.bibliotecaunifor.crud

import android.annotation.SuppressLint
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@SuppressLint("StaticFieldLeak")
val db: FirebaseFirestore = Firebase.firestore


suspend fun adicionarEntrada(entrada: Entrada) {
    try {
        db.collection("Acervo")
            .add(entrada)
            .await()
    } catch (e: Exception) {
        println("Erro ao adicionar entrada: ${e.message}")
    }
}

suspend fun editarEntrada(entrada: Entrada, id: String) {
    try {
        db.collection("Acervo").document(id)
            .set(entrada)
            .await()
    } catch (e: Exception) {
        println("Erro ao editar entrada: ${e.message}")
    }
}

suspend fun excluirEntrada(id: String) {
    try {
        db.collection("Acervo").document(id)
            .delete()
            .await()
    } catch (e: Exception) {
        println("Erro ao excluir entrada: ${e.message}")
    }
}

fun String.removerAcentos(): String {
    val temp = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
    return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(temp, "").lowercase(java.util.Locale.getDefault())
}

suspend fun buscarEntrada(pesquisa: String): List<Entrada> {
    try {
        val todas = listarEntradas()
        if (pesquisa.trim().isEmpty()) return todas
        val termo = pesquisa.trim().removerAcentos()
        return todas.filter { entrada ->
            entrada.titulo.removerAcentos().contains(termo) ||
                    entrada.autor.removerAcentos().contains(termo) ||
                    entrada.isbn.removerAcentos().contains(termo)
        }
    } catch (e: Exception) {
        println("Erro ao pesquisar entradas: ${e.message}")
        return emptyList()
    }
}

suspend fun buscarEntradaPorId(id: String): Entrada? {
    try {
        val snapshot = db.collection("Acervo")
            .document(id)
            .get()
            .await()
        return snapshot.toObject(Entrada::class.java)
    } catch (e: Exception) {
        println("Erro ao buscar entrada por ID: ${e.message}")
        return null
    }
}

suspend fun listarEntradas(): List<Entrada> {
    try {
        val snapshot = db.collection("Acervo")
            .get()
            .await()
        return snapshot.toObjects(Entrada::class.java)
    } catch (e: Exception) {
        println("Erro ao listar entradas: ${e.message}")
        return emptyList()
    }
}
