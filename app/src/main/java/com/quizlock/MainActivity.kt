package com.quizlock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import com.quizlock.core.i18n.AppLanguage
import com.quizlock.core.i18n.EnStrings
import com.quizlock.core.i18n.LocalAppLanguage
import com.quizlock.core.i18n.LocalStrings
import com.quizlock.core.i18n.ViStrings
import com.quizlock.ui.parent.AppLockScreen
import com.quizlock.ui.parent.DashboardScreen
import com.quizlock.ui.parent.OnboardingWizard
import com.quizlock.ui.parent.ProfileManagerScreen
import com.quizlock.ui.parent.SettingsScreen
import com.quizlock.ui.theme.QuizGateTheme

class MainActivity : ComponentActivity() {

    private var isPinConfigured by mutableStateOf<Boolean?>(null)
    private var isUnlocked by mutableStateOf(false)

    override fun onStop() {
        super.onStop()
        if (isPinConfigured == true) {
            isUnlocked = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as QuizGateApplication
        val repo = app.repository

        setContent {
            val appLanguage by repo.appLanguageFlow.collectAsState(initial = AppLanguage.VI)
            val strings = if (appLanguage == AppLanguage.EN) EnStrings else ViStrings

            CompositionLocalProvider(
                LocalStrings provides strings,
                LocalAppLanguage provides appLanguage
            ) {
                QuizGateTheme {
                    var selectedTab by remember { mutableIntStateOf(0) }

                    LaunchedEffect(Unit) {
                        isPinConfigured = repo.isPinConfigured()
                    }

                    if (isPinConfigured == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (!isPinConfigured!!) {
                        // Onboarding flow
                        OnboardingWizard(
                            repository = repo,
                            onCompleted = {
                                isPinConfigured = true
                                isUnlocked = true
                            }
                        )
                    } else if (!isUnlocked) {
                        AppLockScreen(
                            repository = repo,
                            onUnlocked = { isUnlocked = true }
                        )
                    } else {
                        // Main Parent Hub
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                NavigationBar(
                                    modifier = Modifier
                                        .navigationBarsPadding()
                                        .testTag("parent_bottom_nav")
                                ) {
                                    NavigationBarItem(
                                        selected = selectedTab == 0,
                                        onClick = { selectedTab = 0 },
                                        icon = { Icon(Icons.Default.Dashboard, contentDescription = strings.navDashboard) },
                                        label = { Text(strings.navDashboard) },
                                        modifier = Modifier.testTag("nav_tab_dashboard")
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 1,
                                        onClick = { selectedTab = 1 },
                                        icon = { Icon(Icons.Default.People, contentDescription = strings.navProfiles) },
                                        label = { Text(strings.navProfiles) },
                                        modifier = Modifier.testTag("nav_tab_profiles")
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 2,
                                        onClick = { selectedTab = 2 },
                                        icon = { Icon(Icons.Default.Settings, contentDescription = strings.navSettings) },
                                        label = { Text(strings.navSettings) },
                                        modifier = Modifier.testTag("nav_tab_settings")
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (selectedTab) {
                                    0 -> DashboardScreen(
                                        repository = repo,
                                        onNavigateToProfiles = { selectedTab = 1 },
                                        onNavigateToSettings = { selectedTab = 2 }
                                    )
                                    1 -> ProfileManagerScreen(
                                        repository = repo,
                                        onBack = { selectedTab = 0 }
                                    )
                                    2 -> SettingsScreen(
                                        repository = repo,
                                        onBack = { selectedTab = 0 }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
