package com.example.kalasetu.features.application

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.kalasetu.features.event.Event
import kotlinx.datetime.daysUntil

private val PurplePrimary = Color(0xFF7466F1)
private val LightPurpleBg = Color(0xFFF4F1FF)
private val TextDark = Color(0xFF1E1E1E)
private val TextGray = Color(0xFF757575)
private val BorderGray = Color(0xFFE0E0E0)

private val StatusPendingBg = Color(0xFFFFF4E5)
private val StatusPendingFg = Color(0xFFE69500)
private val StatusAcceptedBg = Color(0xFFE5F7E5)
private val StatusAcceptedFg = Color(0xFF2E7D32)
private val StatusRejectedBg = Color(0xFFFFE5E5)
private val StatusRejectedFg = Color(0xFFD32F2F)

@Composable
fun EventApplicationsScreen(
    event: Event,
    onBack: () -> Unit,
    onApplicationClick: (String) -> Unit,
) {
    // Live read from the shared store — updates the moment an artist applies
    val allApps by ApplicationStore.applications.collectAsState()
    val eventApps = remember(allApps, event.id) {
        allApps.filter { it.eventId == event.id }
    }

    val tabs = listOf("All", "Pending", "Accepted", "Rejected")
    var selectedTab by remember { mutableStateOf(0) }

    val filtered = remember(eventApps, selectedTab) {
        when (selectedTab) {
            1 -> eventApps.filter { it.status == ApplicationStatus.PENDING }
            2 -> eventApps.filter { it.status == ApplicationStatus.ACCEPTED }
            3 -> eventApps.filter { it.status == ApplicationStatus.REJECTED }
            else -> eventApps
        }
    }

    val counts = listOf(
        eventApps.size,
        eventApps.count { it.status == ApplicationStatus.PENDING },
        eventApps.count { it.status == ApplicationStatus.ACCEPTED },
        eventApps.count { it.status == ApplicationStatus.REJECTED },
    )

    Scaffold(containerColor = Color.White) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ─── Back arrow ───
            IconButton(
                onClick = onBack,
                modifier = Modifier.offset(x = (-12).dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black,
                )
            }

            // ─── Cover image with 3 info pills ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                if (event.coverImageBytes != null) {
                    AsyncImage(
                        model = event.coverImageBytes,
                        contentDescription = event.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LightPurpleBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Image, null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }

                // Overlay: 3 pills at bottom of cover
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    InfoPill(
                        icon = Icons.Default.CalendarToday,
                        label = "Duration",
                        value = formatDurationPill(event),
                        modifier = Modifier.weight(1f),
                    )
                    InfoPill(
                        icon = Icons.Default.Person,
                        label = "Organizer",
                        value = event.organizerName.ifBlank { "KalaSetu" },
                        modifier = Modifier.weight(1f),
                    )
                    InfoPill(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        value = event.location.ifBlank { "—" },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ─── Title + description ───
            Text(
                event.title.ifBlank { "Untitled Event" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                event.description.ifBlank { "No description provided" },
                fontSize = 13.sp,
                color = TextGray,
                lineHeight = 18.sp,
                maxLines = 2,
            )

            Spacer(Modifier.height(16.dp))

            // ─── Tabs ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                tabs.forEachIndexed { i, label ->
                    val isSelected = selectedTab == i
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PurplePrimary else Color.White)
                            .then(
                                if (!isSelected) Modifier.border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                                else Modifier
                            )
                            .clickable { selectedTab = i }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "$label (${counts[i]})",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else PurplePrimary,
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ─── Applications list ───
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "0 Applications",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (eventApps.isEmpty()) {
                                "No artists have applied to this event yet"
                            } else {
                                "No applications in this tab"
                            },
                            fontSize = 13.sp,
                            color = TextGray,
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    items(filtered, key = { it.id }) { app ->
                        OrganizerAppRow(
                            app = app,
                            onClick = { onApplicationClick(app.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = PurplePrimary,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 9.sp,
                color = TextGray,
                maxLines = 1,
            )
            Text(
                value,
                fontSize = 10.sp,
                color = TextDark,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun OrganizerAppRow(app: Application, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightPurpleBg),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar / portfolio photo
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                if (app.applicantAvatarBytes != null) {
                    AsyncImage(
                        model = app.applicantAvatarBytes,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                    )
                } else {
                    Text(
                        app.applicantName.take(1).uppercase().ifBlank { "A" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary,
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    app.applicantName.ifBlank { "Applicant" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    app.description.ifBlank { "No description" },
                    fontSize = 12.sp,
                    color = TextGray,
                    maxLines = 2,
                    lineHeight = 16.sp,
                )
            }

            Spacer(Modifier.width(8.dp))

            StatusBadge(status = app.status)

            Spacer(Modifier.width(6.dp))

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextDark,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun StatusBadge(status: ApplicationStatus) {
    val (bg, fg) = when (status) {
        ApplicationStatus.PENDING -> StatusPendingBg to StatusPendingFg
        ApplicationStatus.ACCEPTED -> StatusAcceptedBg to StatusAcceptedFg
        ApplicationStatus.REJECTED -> StatusRejectedBg to StatusRejectedFg
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            status.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}

private fun formatDurationPill(event: Event): String {
    val start = event.startDate ?: return "TBA"
    val end = event.endDate
    if (end == null || end == start) return "1 day"
    val days = start.daysUntil(end) + 1
    return "$days days"
}