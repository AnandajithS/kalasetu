package com.example.kalasetu.features.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.kalasetu.features.profile.toInitials
import com.example.kalasetu.theme.KalasetuTheme
import com.example.kalasetu.theme.SelectedPurple
import com.example.kalasetu.theme.SubtitleGray
import com.example.kalasetu.theme.UnselectedBorder
import kotlinx.coroutines.launch

data class ArtistPost(
    val id: Int,
    val artistName: String,
    val craft: String,
    val location: String,
    val timeAgo: String,
    val avatarUrl: String,
    val images: List<String>,
    val caption: String,
    val likes: Int,
    val comments: Int,
    val avatarBackground: Color = Color(0xFFEDE7F6)
)

//Hardcoded stuff
private val dummyPosts = listOf(
    ArtistPost(
        id = 1,
        artistName = "Praharsha",
        craft = "Artist",
        location = "India",
        timeAgo = "2h ago",
        avatarUrl = "https://i.pravatar.cc/150?img=12",
        images = listOf(
            "https://picsum.photos/seed/kasavu1/800/600",
            "https://picsum.photos/seed/kasavu2/800/600",
            "https://picsum.photos/seed/kasavu3/800/600"
        ),
        caption = "Love making portraits and also do enjoy acrylic painting...",
        likes = 10006,
        comments = 45
    ),
    ArtistPost(
        id = 2,
        artistName = "nkart826",
        craft = "Charcoal Painting",
        location = "India",
        timeAgo = "4w ago",
        avatarUrl = "https://i.pravatar.cc/150?img=33",
        images = listOf(
            "https://picsum.photos/seed/horse1/800/900"
        ),
        caption = "Studies in charcoal — capturing motion and light in monochrome.",
        likes = 512,
        comments = 87
    )
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    userAvatarUrl: String? = null,
    userAvatarBytes: ByteArray? = null,
    userName: String? = null,
    userEmail: String? = null,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToStore: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("FEED", "DISCOVER", "NEW", "HYPED")
    
    var showComments by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarContent(
                userName = userName,
                userEmail = userEmail,
                userAvatarUrl = userAvatarUrl,
                userAvatarBytes = userAvatarBytes,
                onClose = { scope.launch { drawerState.close() } },
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    when (route) {
                        "Profile" -> onNavigateToProfile()
                        "Store" -> onNavigateToStore()
                        "Dashboard" -> onNavigateToHome()
                    }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                KalaTopBar(
                    avatarUrl = userAvatarUrl,
                    avatarBytes = userAvatarBytes,
                    userName = userName,
                    onProfileClick = onNavigateToProfile,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = {
                KalaBottomNav(
                    onHomeClick = onNavigateToHome,
                    onStoreClick = onNavigateToStore,
                    onProfileClick = onNavigateToProfile
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                KalaTabRow(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                when (selectedTab) {
                    0 -> FeedContent(onCommentClick = { showComments = true })
                    1 -> DiscoverContent()
                    else -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Content for ${tabs[selectedTab]}")
                        }
                    }
                }
            }

            if (showComments) {
                CommentsBottomSheet(
                    onDismissRequest = { showComments = false },
                    sheetState = sheetState
                )
            }
        }
    }
}

@Composable
internal fun FeedContent(onCommentClick: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(dummyPosts) { post ->
            PostCard(post = post, onCommentClick = onCommentClick)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KalaTopBar(
    avatarUrl: String?,
    avatarBytes: ByteArray?,
    userName: String?,
    onProfileClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "KalaSetu",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            val model = avatarBytes ?: avatarUrl
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                if (model != null) {
                    AsyncImage(
                        model = model,
                        contentDescription = "Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val initials = userName?.toInitials() ?: ""
                    if (initials.isNotEmpty()) {
                        Text(
                            text = initials,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
internal fun KalaTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = SelectedPurple,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                color = SelectedPurple
            )
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedIndex == index) SelectedPurple else SubtitleGray,
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                        softWrap = false
                    )
                }
            )
        }
    }
}

@Composable
private fun PostCard(post: ArtistPost, onCommentClick: () -> Unit) {
    var saved by remember { mutableStateOf(false) }
    var liked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(post.likes) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, UnselectedBorder.copy(alpha = 0.4f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.avatarUrl,
                    contentDescription = post.artistName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(post.avatarBackground)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.artistName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = "${post.craft} • ${post.location} • ${post.timeAgo}",
                        fontSize = 12.sp,
                        color = SubtitleGray
                    )
                }
                IconButton(onClick = {  }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
            }

            ImageCarousel(images = post.images)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    liked = !liked
                    likeCount += if (liked) 1 else -1
                }) {
                    Icon(
                        imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (liked) Color.Red else Color.Black
                    )
                }
                Text(text = "$likeCount", fontSize = 13.sp)

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = onCommentClick) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comments")
                }
                Text(
                    text = "${post.comments}",
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onCommentClick() }
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { saved = !saved }) {
                    Icon(
                        imageVector = if (saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save"
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                var expanded by remember { mutableStateOf(false) }
                Text(
                    buildString {
                        append(post.artistName)
                        append(" · ")
                        append(post.caption)
                    },
                    fontSize = 13.sp,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!expanded) {
                    Text(
                        text = "see more",
                        fontSize = 12.sp,
                        color = SubtitleGray,
                        modifier = Modifier.clickable { expanded = true }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun ImageCarousel(images: List<String>) {
    var currentIndex by remember { mutableIntStateOf(0) }

    Box {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            items(images) { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .fillMaxHeight()
                )
            }
        }

        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                images.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex) SelectedPurple
                                else Color.White.copy(alpha = 0.6f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
internal fun KalaBottomNav(
    onStoreClick: () -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    var selected by remember { mutableIntStateOf(1) }
    val items = listOf(
        Icons.Filled.Storefront,
        Icons.Filled.Home,
        Icons.Outlined.Person
    )

    NavigationBar(containerColor = Color.White) {
        items.forEachIndexed { index, icon ->
            NavigationBarItem(
                selected = selected == index,
                onClick = {
                    selected = index
                    when (index) {
                        0 -> onStoreClick()
                        1 -> onHomeClick()
                        2 -> onProfileClick()
                    }
                },
                icon = { Icon(icon, contentDescription = null) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    indicatorColor = SelectedPurple,
                    unselectedIconColor = SubtitleGray
                )
            )
        }
    }
}
