package com.example.examen3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProximityViewModel : ViewModel() {

    // Estado con la lista de todos los eventos (mantén si lo necesitas para otra cosa)
    private val _proximityEvents = MutableStateFlow<List<ProximityEvent>>(emptyList())
    val proximityEvents: StateFlow<List<ProximityEvent>> = _proximityEvents.asStateFlow()

    // Conjunto interno para llevar la cuenta de IDs únicos
    private val uniqueDevices = mutableSetOf<String>()

    // Estado con el conteo de dispositivos únicos
    private val _uniqueDeviceCount = MutableStateFlow(0)
    val uniqueDeviceCount: StateFlow<Int> = _uniqueDeviceCount.asStateFlow()

    init {
        viewModelScope.launch {
            ProximityEventBus.events.collect { event ->
                // 1) Sigues acumulando eventos si lo necesitas…
                _proximityEvents.update { old ->
                    (old + event).takeLast(50)
                }
                // 2) …pero sólo incrementas el contador cuando aparezca un nuevo deviceId
                if (uniqueDevices.add(event.deviceId)) {
                    _uniqueDeviceCount.value = uniqueDevices.size
                }
            }
        }
    }

    fun clearEvents() {
        _proximityEvents.value = emptyList()
        uniqueDevices.clear()
        _uniqueDeviceCount.value = 0
    }
}
