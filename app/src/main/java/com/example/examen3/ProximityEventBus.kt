package com.example.examen3

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object ProximityEventBus {
    private val _events = MutableSharedFlow<ProximityEvent>()
    val events: SharedFlow<ProximityEvent> = _events.asSharedFlow()

    fun postEvent(event: ProximityEvent) {
        _events.tryEmit(event)
    }
}