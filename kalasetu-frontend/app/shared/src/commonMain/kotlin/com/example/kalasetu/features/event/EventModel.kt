package com.example.kalasetu.features.event

import kotlinx.datetime.LocalDate

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val organizerName: String = "",
    val email: String = "",
    val phone: String = "",
    val coverImageBytes: ByteArray? = null,
    val galleryBytes: List<ByteArray> = emptyList(),
    val categories: List<String> = emptyList()
)

// ─── Shared Duration Formatter ───
fun formatEventDuration(start: LocalDate?, end: LocalDate?): String {
    if (start == null) return "Date TBA"
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val s = "${start.dayOfMonth} ${months[start.monthNumber - 1]} ${start.year}"
    if (end == null || end == start) return s
    if (end.monthNumber == start.monthNumber && end.year == start.year) {
        return "${start.dayOfMonth} to ${end.dayOfMonth} ${months[start.monthNumber - 1]} ${start.year}"
    }
    return "$s to ${end.dayOfMonth} ${months[end.monthNumber - 1]} ${end.year}"
}

// ─── Converts an organizer's draft into a published Event ───
fun EventDraft.toEvent(id: String): Event = Event(
    id = id,
    title = title,
    description = description,
    location = location,
    startDate = startDate,
    endDate = endDate,
    email = email,
    phone = phone,
    coverImageBytes = coverImageBytes,
    galleryBytes = galleryBytes,
    categories = categories
)

// ─── Application form state (for the artist flow) ───
data class ApplicationFormState(
    val coverLetter: String = "",
    val portfolioLink: String = "",
    val expectedFee: String = "",
    val attachedFile: io.github.vinceglb.filekit.core.PlatformFile? = null,
    val attachedBytes: ByteArray? = null,
    val attachedFileSize: Long = 0L
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ApplicationFormState) return false
        return coverLetter == other.coverLetter &&
                portfolioLink == other.portfolioLink &&
                expectedFee == other.expectedFee &&
                attachedFile == other.attachedFile &&
                attachedFileSize == other.attachedFileSize
    }

    override fun hashCode(): Int {
        var result = coverLetter.hashCode()
        result = 31 * result + portfolioLink.hashCode()
        result = 31 * result + expectedFee.hashCode()
        result = 31 * result + (attachedFile?.hashCode() ?: 0)
        result = 31 * result + attachedFileSize.hashCode()
        return result
    }
}