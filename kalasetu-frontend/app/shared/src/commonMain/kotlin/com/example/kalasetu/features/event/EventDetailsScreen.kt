package com.example.kalasetu.features.event

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage



@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    viewModel: EventListViewModel,
    onBack: () -> Unit,
    onApply: () -> Unit
) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val event = events.firstOrNull { it.id == eventId }

    // ─── Gallery overlay state ───
    var showGallery by remember { mutableStateOf(false) }
    var galleryStartIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        if (event == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Event not found", color = TextGray)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ─── Cover ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)    // 16:9 to match create form
            ) {
                if (event.coverImageBytes != null) {
                    AsyncImage(
                        model = event.coverImageBytes,
                        contentDescription = event.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LightPurpleBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(event.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(Modifier.height(6.dp))
                Text("By ${event.organizerName}", fontSize = 14.sp, color = TextGray)

                Spacer(Modifier.height(16.dp))

                DetailRow(Icons.Default.CalendarToday, "Duration", formatEventDuration(event.startDate, event.endDate))
                DetailRow(Icons.Default.LocationOn, "Location", event.location.ifBlank { "Not provided" })
                DetailRow(Icons.Default.Email, "Email", event.email.ifBlank { "Not provided" })
                DetailRow(Icons.Default.Phone, "Phone", event.phone.ifBlank { "Not provided" })

                Spacer(Modifier.height(20.dp))

                Text("About this Event", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(Modifier.height(8.dp))
                Text(event.description, fontSize = 14.sp, color = TextGray, lineHeight = 20.sp)

                // ─── Categories ───
                if (event.categories.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Text("Looking for", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        event.categories.forEach { category ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(LightPurpleBg)
                                    .border(1.dp, PurplePrimary, RoundedCornerShape(50))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(category, fontSize = 14.sp, color = PurplePrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // ─── Gallery (tappable thumbnails) ───
                if (event.galleryBytes.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Gallery", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(
                            "${event.galleryBytes.size} photo${if (event.galleryBytes.size > 1) "s" else ""}",
                            fontSize = 12.sp,
                            color = TextGray,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(event.galleryBytes.size) { index ->
                            val bytes = event.galleryBytes[index]
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        galleryStartIndex = index
                                        showGallery = true
                                    }
                            ) {
                                AsyncImage(
                                    model = bytes,
                                    contentDescription = "Gallery image ${index + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onApply,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Apply Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ─── Full-screen gallery overlay ───
    if (showGallery && event != null) {
        FullScreenGallery(
            images = event.galleryBytes,
            startIndex = galleryStartIndex,
            onClose = { showGallery = false }
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LightPurpleBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 13.sp, color = TextGray, modifier = Modifier.width(72.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDark, modifier = Modifier.weight(1f))
    }
}

// ─── Full-screen swipeable gallery ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenGallery(
    images: List<ByteArray>,
    startIndex: Int,
    onClose: () -> Unit
) {
    if (images.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(0, images.size - 1),
        pageCount = { images.size }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Swipeable pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = images[page],
                    contentDescription = "Gallery image ${page + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Close button (top-right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        // Page counter (top-left)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 16.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "${pagerState.currentPage + 1} / ${images.size}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        // Dot indicators (bottom)
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(images.size) { index ->
                    val isActive = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Color.White else Color.White.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}