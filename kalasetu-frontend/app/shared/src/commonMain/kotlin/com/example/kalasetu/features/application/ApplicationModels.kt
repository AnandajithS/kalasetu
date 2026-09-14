package com.example.kalasetu.features.application

import kotlinx.datetime.LocalDate

enum class ApplicationStatus(val label: String) {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected")
}

data class Application(
    val id: String,
    val eventId: String,

    // ─── Event snapshot (so the list doesn't have to look up the event each time) ───
    val eventTitle: String = "",
    val eventCoverBytes: ByteArray? = null,

    // ─── Applicant form data ───
    val applicantName: String = "",
    val description: String = "",
    val coverImageBytes: ByteArray? = null,
    val applicantAvatarBytes: ByteArray? = null,
    val portfolioFileName: String = "",
    val galleryBytes: List<ByteArray> = emptyList(),
    val email: String = "",
    val phone: String = "",

    // ─── Application meta ───
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val submittedAt: LocalDate? = null,
) {
    override fun equals(other: Any?): Boolean =
        this === other || (
                other is Application &&
                        id == other.id &&
                        status == other.status        // ← THE FIX: include status
                )

    override fun hashCode(): Int =
        31 * id.hashCode() + status.hashCode()   // ← THE FIX: include status
}