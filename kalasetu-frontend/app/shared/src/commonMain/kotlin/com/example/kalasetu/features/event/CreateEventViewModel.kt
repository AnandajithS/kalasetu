package com.example.kalasetu.features.event

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CreateEventState(
    val eventName: String = "",
    val description: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val allowApplications: Boolean = true,
    val coverImageName: String? = null,
    val galleryImageNames: List<String> = emptyList(),
)

class CreateEventViewModel : ViewModel() {
    private val _state = MutableStateFlow(CreateEventState())
    val state: StateFlow<CreateEventState> = _state.asStateFlow()

    fun onEventNameChange(name: String) = _state.update { it.copy(eventName = name) }
    fun onDescriptionChange(desc: String) = _state.update { it.copy(description = desc) }
    fun onEmailChange(email: String) = _state.update { it.copy(email = email) }
    fun onContactChange(contact: String) = _state.update { it.copy(contactNumber = contact) }
    fun onAllowApplicationsChange(allow: Boolean) = _state.update { it.copy(allowApplications = allow) }

    fun onCoverImageChange(name: String?) = _state.update {
        it.copy(coverImageName = name)
    }

    fun onGalleryImagesChange(names: List<String>) = _state.update {
        it.copy(galleryImageNames = names)
    }

    fun syncFromDraft(
        title: String,
        description: String,
        email: String,
        phone: String,
        coverImageName: String?,
    ) {
        _state.update {
            it.copy(
                eventName = title,
                description = description,
                email = email,
                contactNumber = phone,
                coverImageName = coverImageName ?: it.coverImageName,
            )
        }
    }

    fun submitEvent() {
        // Handle API submission here
    }
}