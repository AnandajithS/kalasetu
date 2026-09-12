package com.example.kalasetu.features.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ApplicationViewModel : ViewModel() {
    private val _formState = MutableStateFlow(ApplicationFormState())
    val formState: StateFlow<ApplicationFormState> = _formState.asStateFlow()
    val attachedFileSize: Long = 0L

    fun onCoverLetterChange(value: String) = _formState.update { it.copy(coverLetter = value) }
    fun onPortfolioLinkChange(value: String) = _formState.update { it.copy(portfolioLink = value) }
    fun onExpectedFeeChange(value: String) = _formState.update { it.copy(expectedFee = value) }

    fun onFileRemoved() = _formState.update {
        it.copy(
            //attachedFileName = null,
            //attachedFilePath = null,
            attachedFileSize = 0L
        )
    }
    fun onFileAttached(file: PlatformFile) {
        _formState.update { it.copy(attachedFile = file, attachedBytes = null) }
        viewModelScope.launch {
            try {
                val bytes = file.readBytes()
                _formState.update {
                    it.copy(
                        attachedBytes = bytes,
                        attachedFileSize = bytes.size.toLong()
                    )
                }
            } catch (e: Exception) {
                println("Failed to read file: ${e.message}")
            }
        }
    }
    fun submitApplication(eventId: String) {
        // TODO: Send application to backend API
    }
}