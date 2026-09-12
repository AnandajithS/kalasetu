package com.example.kalasetu.features.event

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class EventListViewModel : ViewModel() {
    val events: StateFlow<List<Event>> = EventStore.events

    fun addEvent(event: Event) = EventStore.addEvent(event)

    fun getEventById(id: String): Event? =
        EventStore.events.value.firstOrNull { it.id == id }
}