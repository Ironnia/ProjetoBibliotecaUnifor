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

suspend fun buscarEntrada(pesquisa: String): List<Entrada> {
    try {
        val snapshot = db.collection("Acervo")
            .orderBy("titulo")
            .startAt(pesquisa)
            .endAt(pesquisa+ "\uf8ff")
            .get()
            .await()
        return snapshot.toObjects(Entrada::class.java)
    } catch (e: Exception) {
        println("Erro ao pesquisar entradas: ${e.message}")
        return emptyList()
    }
}

suspend fun listarEntradas(): List<Entrada> {
    try {
        val snapshot = db.collection("Acervo")
            .get().await()
        return snapshot.toObjects(Entrada::class.java)
    } catch (e: Exception) {
        println("Erro ao listar entradas: ${e.message}")
        return emptyList()
    }
}

suspend fun buscarEntradaPorId(id: String): Entrada? {
    try {
        val snapshot = db.collection("Acervo").document(id).get().await()
        return snapshot.toObject(Entrada::class.java)
    } catch (e: Exception) {
        println("Erro ao buscar entrada por ID: ${e.message}")
        return null
    }
}
