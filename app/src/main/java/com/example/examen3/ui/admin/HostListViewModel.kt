// In package com.example.examen3.ui.admin

package com.example.examen3.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HostListViewModel : ViewModel() {

    private val _hosts = MutableStateFlow<List<String>>(emptyList())
    val hosts: StateFlow<List<String>> = _hosts.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()

    init {
        viewModelScope.launch {
            hostsFlow()
                .catch { error -> Log.e("HostListViewModel", "Error in flow", error) }
                .collect { _hosts.value = it }
        }
    }

    // Fetches all encounters and extracts the unique "pid_a" values
    private fun hostsFlow(): Flow<List<String>> = callbackFlow {
        val listener = firestore.collection("encounters")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Keep order to show recent hosts first
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error) // Close the flow on error
                    return@addSnapshotListener
                }

                val uniqueHosts = snap?.documents
                    ?.mapNotNull { it.getString("pid_a") } // Get all pid_a values
                    ?.distinct() // Keep only unique ones
                    ?: emptyList()

                Log.d("HostListViewModel", "Found ${uniqueHosts.size} unique hosts")
                trySend(uniqueHosts).isSuccess
            }

        awaitClose { listener.remove() }
    }
}