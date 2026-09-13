package com.example.kalasetu

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kalasetu.features.auth.*
import com.example.kalasetu.features.feed.FeedScreen
import com.example.kalasetu.features.feed.KalaTopBar
import com.example.kalasetu.features.onboarding.*
import com.example.kalasetu.features.profile.*
import com.example.kalasetu.navigation.BackHandler
import com.example.kalasetu.navigation.Screen
import com.example.kalasetu.theme.KalasetuTheme

@Composable
fun App() {
    var screen by remember { mutableStateOf<Screen>(Screen.OnboardingWelcome) }
    var selectedRole by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userLocation by remember { mutableStateOf("") }
    var currentProfile by remember { mutableStateOf<Profile?>(null) }

    KalasetuTheme {
        when (val currentScreen = screen) {

            Screen.Feed -> FeedScreen(
                userAvatarUrl = currentProfile?.avatarUrl,
                userAvatarBytes = currentProfile?.avatarBytes,
                userName = currentProfile?.name ?: userName,
                userEmail = currentProfile?.email ?: userEmail,
                onNavigateToProfile = { screen = Screen.Profile(userId = "123") },
                onNavigateToStore = { screen = Screen.Store },
                onNavigateToHome = { screen = Screen.Feed }
            )

            Screen.Store -> {
                BackHandler { screen = Screen.Feed }
                Scaffold(
                    topBar = {
                        KalaTopBar(
                            avatarUrl = currentProfile?.avatarUrl,
                            avatarBytes = currentProfile?.avatarBytes,
                            userName = currentProfile?.name ?: userName,
                            onProfileClick = { screen = Screen.Profile(userId = "123") },
                            onMenuClick = { /* Handle menu in store if needed */ }
                        )
                    }
                ) { innerPadding ->
                    Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        Text("Store Screen Placeholder")
                        Button(onClick = { screen = Screen.Feed }, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
                            Text("Back to Feed")
                        }
                    }
                }
            }

            Screen.OnboardingWelcome -> OnboardingWelcomeScreen(
                onNext = { screen = Screen.AuthSignup }
            )

            Screen.AuthSignup -> {
                BackHandler { screen = Screen.OnboardingWelcome }
                AuthSignupScreen(
                    onSignUp = { email ->
                        userEmail = email
                        screen = Screen.AuthOtp
                    },
                    onLogin = { screen = Screen.AuthLogin },
                    onBack = { screen = Screen.OnboardingWelcome }
                )
            }

            Screen.AuthOtp -> {
                BackHandler { screen = Screen.AuthSignup }
                AuthOtpScreen(
                    onVerify = { screen = Screen.OnboardingBasicInfo },
                    onLogin = { screen = Screen.AuthLogin },
                    onBack = { screen = Screen.AuthSignup }
                )
            }

            Screen.AuthLogin -> {
                BackHandler { screen = Screen.AuthSignup }
                AuthLoginScreen(
                    onLogin = { email ->
                        userEmail = email
                        screen = Screen.OnboardingBasicInfo
                    },
                    onSignUp = { screen = Screen.AuthSignup },
                    onBack = { screen = Screen.AuthSignup }
                )
            }

            Screen.OnboardingBasicInfo -> {
                BackHandler { screen = Screen.OnboardingWelcome }
                OnboardingBasicInfoScreen(
                    onNext = { name, role ->
                        userName = name
                        selectedRole = role
                        screen = Screen.OnboardingLocation
                    },
                ) { screen = Screen.OnboardingWelcome }
            }

            Screen.OnboardingLocation -> {
                BackHandler { screen = Screen.OnboardingBasicInfo }
                OnboardingLocationScreen(
                    onNext = { location ->
                        userLocation = location
                        screen = when (selectedRole) {
                            "Artist"          -> Screen.ArtistExperience
                            "Event Organizer" -> Screen.OrganizerType
                            else              -> Screen.AudienceInterests
                        }
                    },
                ) { screen = Screen.OnboardingBasicInfo }
            }

            Screen.ArtistExperience -> {
                BackHandler { screen = Screen.OnboardingLocation }
                ExperienceScreen(
                    onNext = { screen = Screen.OnboardingDone },
                ) { screen = Screen.OnboardingLocation }
            }

            Screen.OrganizerType -> {
                BackHandler { screen = Screen.OnboardingLocation }
                OrganizerTypeScreen(
                    onNext = { screen = Screen.OrganizerIntent },
                ) { screen = Screen.OnboardingLocation }
            }

            Screen.OrganizerIntent -> {
                BackHandler { screen = Screen.OrganizerType }
                OrganizerIntentScreen(
                    onNext = { screen = Screen.OnboardingDone },
                ) { screen = Screen.OrganizerType }
            }

            Screen.AudienceInterests -> {
                BackHandler { screen = Screen.OnboardingLocation }
                InterestsScreen(
                    onNext = { screen = Screen.OnboardingDone },
                ) { screen = Screen.OnboardingLocation }
            }

            Screen.OnboardingDone -> {
                BackHandler { screen = Screen.Feed }
                OnboardingDoneScreen {
                    // Temporary mock user ID for Profile UI development.
                    // Replace with the authenticated user ID when registration/auth is integrated.
                    screen = Screen.Profile(userId = "123")
                }
            }

            is Screen.Profile -> {
                BackHandler { screen = Screen.Feed }
                val presenter = remember(currentScreen.userId, currentProfile) {
                    ProfilePresenter(
                        repository = ProfileRepository(
                            initialProfile = currentProfile ?: Profile(
                                name = userName,
                                location = userLocation
                            )
                        )
                    )
                }
                ProfileScreen(
                    presenter = presenter,
                    userId = currentScreen.userId,
                    onEditProfile = { screen = Screen.EditProfile(currentScreen.userId) },
                    onShare = { /* Handle share */ },
                    onBack = { screen = Screen.Feed }
                )
            }

            is Screen.EditProfile -> {
                BackHandler { screen = Screen.Profile(userId = currentScreen.userId) }
                val profileToEdit = currentProfile ?: Profile(
                    id       = currentScreen.userId,
                    name     = userName,
                    location = userLocation,
                    username = "",
                    email    = userEmail,
                )
                EditProfileScreen(
                    profile = profileToEdit,
                    onBack  = { screen = Screen.Profile(userId = currentScreen.userId) },
                    onSave  = { updated ->
                        currentProfile = updated
                        userName       = updated.name
                        userEmail      = updated.email
                        screen         = Screen.Profile(userId = currentScreen.userId)
                    },
                )
            }
        }
    }
}
