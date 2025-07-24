package com.example.examen3.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen3.data.PositiveRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HostListViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    /* ----------------  Hosts  ---------------- */
    private val _hosts = MutableStateFlow<List<String>>(emptyList())
    val hosts = _hosts.asStateFlow()

    /* ----------------  Positivos ------------- */
    private val _positives = MutableStateFlow<Set<String>>(emptySet())
    val positives = _positives.asStateFlow()

    init {
        /** 1) flujo de hosts */
        viewModelScope.launch {
            hostsFlow()
                .catch { Log.e("HostListVM", "Error hosts", it) }
                .collect { _hosts.value = it }
        }

        /** 2) flujo de positivos */
        viewModelScope.launch {
            positivesFlow().collect { _positives.value = it }
        }
    }

    /* ---------- HOSTS: únicos pid_a en encuentros ---------- */
    private fun hostsFlow(): Flow<List<String>> = callbackFlow {
        val listener = firestore.collection("encounters")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val list = snap?.documents
                    ?.mapNotNull { it.getString("pid_a") }
                    ?.distinct()
                    ?: emptyList()
                trySend(list).isSuccess
            }
        awaitClose { listener.remove() }
    }

    /* ---------- POSITIVOS: colección "positives" ---------- */
    private fun positivesFlow(): Flow<Set<String>> = callbackFlow {
        val l = firestore.collection("positives")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    close(e); return@addSnapshotListener
                }
                val set = snap?.documents
                    ?.mapNotNull { it.getString("pid") }
                    ?.toSet() ?: emptySet()
                trySend(set).isSuccess
            }
        awaitClose { l.remove() }
    }

    /* ---------- Acción: marcar host como positivo ---------- */
    fun markPositive(pid: String) = viewModelScope.launch {
        PositiveRepository(firestore).markPositive(pid)
    }
}
