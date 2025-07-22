package com.example.examen3

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Event bus para notificar en tiempo real los eventos de proximidad.
 * Se configura con replay = 1 para que el último evento
 * se reemita automáticamente a nuevos collectors,
 * y un buffer extra para prevenir pérdidas bajo alta demanda.
 */
object ProximityEventBus {
    private val _events = MutableSharedFlow<ProximityEvent>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<ProximityEvent> = _events.asSharedFlow()

    fun postEvent(event: ProximityEvent) {
        _events.tryEmit(event)
    }
}
