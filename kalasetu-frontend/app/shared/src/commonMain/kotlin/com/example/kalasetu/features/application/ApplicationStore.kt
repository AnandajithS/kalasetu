package com.example.kalasetu.features.application

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ApplicationStore {
    private val _applications = MutableStateFlow<List<Application>>(emptyList())
    val applications: StateFlow<List<Application>> = _applications.asStateFlow()

    fun addApplication(app: Application) {
        _applications.value = _applications.value + app
    }

    fun applicationsForEvent(eventId: String): List<Application> =
        _applications.value.filter { it.eventId == eventId }

    fun applicationById(id: String): Application? =
        _applications.value.firstOrNull { it.id == id }

    // ✅ The missing function
    fun updateStatus(id: String, status: ApplicationStatus) {
        _applications.value = _applications.value.map {
            if (it.id == id) it.copy(status = status) else it
        }
    }
}