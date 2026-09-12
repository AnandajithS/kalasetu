package com.example.kalasetu.features.event

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate

data class EventDraft(
    val title: String = "",
    val description: String = "",
    val coverImageBytes: ByteArray? = null,
    val galleryBytes: List<ByteArray> = emptyList(),
    val email: String = "",
    val phone: String = "",
    val categories: List<String> = emptyList(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val location: String = "",
)