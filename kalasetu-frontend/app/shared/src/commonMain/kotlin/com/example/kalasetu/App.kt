package com.example.kalasetu

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kalasetu.features.application.*
import com.example.kalasetu.features.auth.*
import com.example.kalasetu.features.event.*
import com.example.kalasetu.features.onboarding.*
import com.example.kalasetu.features.profile.*
import com.example.kalasetu.navigation.Screen
import com.example.kalasetu.theme.KalasetuTheme
import kotlinx.datetime.LocalDate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kalasetu.repository.EventRepository
import kotlinx.coroutines.launch
import com.example.kalasetu.repository.AuthRepository



@Composable
fun App() {
    var screen by remember { mutableStateOf<Screen>(Screen.OnboardingWelcome) }
    var selectedRole by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var userLocation by remember { mutableStateOf("") }
    var currentProfile by remember { mutableStateOf<Profile?>(null) }
    var draftEvent by remember { mutableStateOf(EventDraft()) }
    val sharedEventListViewModel: EventListViewModel = viewModel()
    val eventRepository = remember { EventRepository() }
    val scope = rememberCoroutineScope()


    KalasetuTheme {
        when (val currentScreen = screen) {

            // ─── Onboarding & Auth ───
            Screen.OnboardingWelcome -> OnboardingWelcomeScreen {
                screen = Screen.AuthSignup
            }
            Screen.AuthSignup -> AuthSignupScreen(
                onSignUp = { name, email, password ->
                    scope.launch {
                        val result = AuthRepository().register(
                            name = name,
                            email = email,
                            password = password
                        )

                        if (result.isSuccess) {
                            println("========== APP SIGNUP SUCCESS ==========")

                            screen = Screen.AuthLogin
                        } else {
                            println(
                                "SIGNUP FAILED: ${
                                    result.exceptionOrNull()?.message
                                }"
                            )
                        }
                    }
                },

                onLogin = {
                    screen = Screen.AuthLogin
                },

                onBack = {
                    screen = Screen.AuthLogin
                },
            )
            Screen.AuthOtp -> AuthOtpScreen(
                onVerify = { screen = Screen.OnboardingBasicInfo },
                onLogin = { screen = Screen.AuthLogin },
                onBack = { screen = Screen.AuthSignup },
            )
            Screen.AuthLogin -> AuthLoginScreen(
                onLogin = { email, password ->
                    scope.launch {

                        val result = AuthRepository().login(
                            email = email,
                            password = password
                        )

                        if (result.isSuccess) {

                            println("========== APP LOGIN SUCCESS ==========")

                            screen = Screen.OrganizerHome(userId = "123")

                        } else {

                            println(
                                "LOGIN FAILED: ${
                                    result.exceptionOrNull()?.message
                                }"
                            )
                        }
                    }
                },

                onSignUp = {
                    screen = Screen.AuthSignup
                },

                onBack = {
                    screen = Screen.AuthSignup
                },
            )
            Screen.OnboardingBasicInfo -> OnboardingBasicInfoScreen(
                onNext = { name, role ->
                    userName = name
                    selectedRole = role
                    screen = Screen.OnboardingLocation
                },
            ) { screen = Screen.OnboardingWelcome }
            Screen.OnboardingLocation -> OnboardingLocationScreen(
                onNext = { location ->
                    userLocation = location
                    screen = when (selectedRole) {
                        "Artist" -> Screen.ArtistExperience
                        "Event Organizer" -> Screen.OrganizerType
                        else -> Screen.AudienceInterests
                    }
                },
            ) { screen = Screen.OnboardingBasicInfo }
            Screen.ArtistExperience -> ExperienceScreen(onNext = { screen = Screen.OnboardingDone }) { screen = Screen.OnboardingLocation }
            Screen.OrganizerType -> OrganizerTypeScreen(onNext = { screen = Screen.OnboardingDone }) { screen = Screen.OnboardingLocation }
            Screen.OrganizerIntent -> OrganizerIntentScreen(onNext = { screen = Screen.OnboardingDone }) { screen = Screen.OrganizerType }
            Screen.AudienceInterests -> InterestsScreen(onNext = { screen = Screen.OnboardingDone }) { screen = Screen.OnboardingLocation }

            Screen.OnboardingDone -> OnboardingDoneScreen {
                screen = when (selectedRole) {
                    "Artist" -> Screen.ArtistHome(userId = "123")
                    "Event Organizer" -> Screen.OrganizerHome(userId = "123")
                    else -> Screen.Profile(userId = "123")
                }
            }

            // ─── Profile ───
            is Screen.Profile -> {
                val presenter = remember(currentScreen.userId, currentProfile) {
                    ProfilePresenter(
                        repository = ProfileRepository(
                            initialProfile = currentProfile ?: Profile(
                                name = userName,
                                location = userLocation,
                            )
                        )
                    )
                }
                ProfileScreen(
                    presenter = presenter,
                    userId = currentScreen.userId,
                    onEditProfile = { screen = Screen.EditProfile(currentScreen.userId) },
                    onShare = { },
                )
            }
            is Screen.EditProfile -> {
                val profileToEdit = currentProfile ?: Profile(
                    id = currentScreen.userId,
                    name = userName,
                    location = userLocation,
                    username = "",
                    email = "",
                )
                EditProfileScreen(
                    profile = profileToEdit,
                    onBack = { screen = Screen.Profile(userId = currentScreen.userId) },
                    onSave = { updated ->
                        currentProfile = updated
                        userName = updated.name
                        screen = Screen.Profile(userId = currentScreen.userId)
                    },
                )
            }

            // ─── Artist Flow ───
            is Screen.ArtistHome -> ArtistHomeScreen(
                viewModel = sharedEventListViewModel,
                onEventClick = { eventId -> screen = Screen.EventDetails(eventId) },
                onSwitchRole = { screen = Screen.OrganizerHome(userId = "123") },
            )

            is Screen.EventDetails -> EventDetailsScreen(
                eventId = currentScreen.eventId,
                viewModel = sharedEventListViewModel,
                onBack = { screen = Screen.ArtistHome(userId = "123") },
                onApply = { screen = Screen.ApplicationForm(currentScreen.eventId) },
            )

            is Screen.ApplicationForm -> {
                val events by sharedEventListViewModel.events.collectAsState()
                val event = events.firstOrNull { it.id == currentScreen.eventId }

                ApplicationFormScreen(
                    eventId = currentScreen.eventId,
                    eventTitle = event?.title ?: "Event",
                    eventCoverBytes = event?.coverImageBytes,
                    applicantAvatarBytes = currentProfile?.avatarBytes,
                    onBack = { screen = Screen.EventDetails(currentScreen.eventId) },
                    onSubmit = { application: Application ->
                        ApplicationStore.addApplication(application)
                        screen = Screen.ApplicationStatus(application.id)
                    },
                )
            }

            is Screen.ApplicationStatus -> {
                val app = ApplicationStore.applicationById(currentScreen.applicationId)
                ApplicationStatusScreen(
                    eventTitle = app?.eventTitle ?: "Event",
                    onBack = { screen = Screen.MyApplications(userId = "123") },
                    onDone = { screen = Screen.MyApplications(userId = "123") },
                )
            }

            is Screen.MyApplications -> MyApplicationsScreen(
                onApplicationClick = { appId -> screen = Screen.ApplicationStatus(appId) },
                onBack = { screen = Screen.ArtistHome(userId = "123") },
                onSwitchRole = { screen = Screen.OrganizerHome(userId = "123") },
            )

            // ─── Organizer Flow ───
            is Screen.OrganizerHome -> OrganizerHomeScreen(
                userId = currentScreen.userId,
                viewModel = sharedEventListViewModel,
                onCreateEvent = {
                    draftEvent = EventDraft()
                    screen = Screen.CreateEvent
                },
                onEventClick = { eventId -> screen = Screen.EventApplications(eventId) },
                onSwitchRole = { screen = Screen.ArtistHome(userId = "123") },
            )

            Screen.CreateEvent -> CreateEventScreen(
                initialDraft = draftEvent,
                onNext = { title, description, email, phone, coverBytes, galleryBytes ->
                    draftEvent = draftEvent.copy(
                        title = title,
                        description = description,
                        email = email,
                        phone = phone,
                        coverImageBytes = coverBytes,
                        galleryBytes = galleryBytes,
                    )
                    screen = Screen.SelectArtistCategories
                },
                onBack = { screen = Screen.OrganizerHome(userId = "123") },
            )

            Screen.SelectArtistCategories -> SelectArtistCategoriesScreen(
                onNext = { categories ->
                    draftEvent = draftEvent.copy(categories = categories)
                    screen = Screen.TimelineAndLocation
                },
                onBack = { screen = Screen.CreateEvent },
            )

            Screen.TimelineAndLocation -> TimelineAndLocationScreen(
                onNext = { location, startDate, endDate ->
                    draftEvent = draftEvent.copy(
                        location = location,
                        startDate = startDate,
                        endDate = endDate,
                    )
                    screen = Screen.ReviewEvent
                },
                onBack = { screen = Screen.SelectArtistCategories },
            )

            Screen.ReviewEvent -> ReviewEventScreen(
                draft = draftEvent,
                onBack = { screen = Screen.TimelineAndLocation },
                onEdit = { screen = Screen.CreateEvent },
                onPublish = {
                    println("========== PUBLISH CLICKED ==========")
                    val startDate = draftEvent.startDate

                    val duration = if (draftEvent.startDate != null && draftEvent.endDate != null) {
                        val startDate = draftEvent.startDate!!
                        val endDate = draftEvent.endDate!!

                        val days = endDate.toEpochDays() - startDate.toEpochDays() + 1
                        "$days days"
                    } else {
                        "1 day"
                    }

                    scope.launch {

                        try {
                            println("========== CALLING GRAPHQL ==========")
                            val response = eventRepository.createEvent(
                                name = draftEvent.title.trim(),
                                startDate = startDate?.toString() ?: "",
                                duration = duration
                            )

                            if (response.data != null && response.exception == null && response.errors.isNullOrEmpty()) {
                                println("========== EVENT CREATED SUCCESSFULLY ==========")

                                draftEvent = EventDraft()
                                screen = Screen.OrganizerHome(userId = "123")
                            } else {
                                println("========== EVENT CREATION FAILED ==========")
                                println("data = ${response.data}")
                                println("errors = ${response.errors}")
                                println("exception = ${response.exception}")
                            }

                        } catch (e: Exception) {

                            println("NETWORK ERROR: ${e.message}")
                            e.printStackTrace()

                        }
                    }
                },
            )

            is Screen.OrganizerEventList -> OrganizerHomeScreen(
                userId = currentScreen.userId,
                viewModel = sharedEventListViewModel,
                onCreateEvent = {
                    draftEvent = EventDraft()
                    screen = Screen.CreateEvent
                },
                onEventClick = { eventId -> screen = Screen.EventApplications(eventId) },
                onSwitchRole = { screen = Screen.ArtistHome(userId = "123") },
            )

            // ─── Event Applications (Organizer) ───
            is Screen.EventApplications -> {
                val events by sharedEventListViewModel.events.collectAsState()
                val event = events.firstOrNull { it.id == currentScreen.eventId }
                if (event == null) {
                    LaunchedEffect(Unit) {
                        screen = Screen.OrganizerHome(userId = "123")
                    }
                } else {
                    EventApplicationsScreen(
                        event = event,
                        onBack = { screen = Screen.OrganizerHome(userId = "123") },
                        onApplicationClick = { appId ->
                            screen = Screen.ApplicationPreview(appId)
                        },
                    )
                }
            }

            // ─── Application Preview (Organizer) ───
            is Screen.ApplicationPreview -> {
                val app = ApplicationStore.applicationById(currentScreen.applicationId)
                if (app == null) {
                    LaunchedEffect(Unit) {
                        screen = Screen.OrganizerHome(userId = "123")
                    }
                } else {
                    ApplicationPreviewScreen(
                        application = app,
                        onBack = { screen = Screen.EventApplications(app.eventId) },
                        onAccept = {
                            ApplicationStore.updateStatus(app.id, ApplicationStatus.ACCEPTED)
                        },
                        onReject = {
                            ApplicationStore.updateStatus(app.id, ApplicationStatus.REJECTED)
                        },
                        onDone = { screen = Screen.EventApplications(app.eventId) },
                    )
                }
            }
        }
    }
}