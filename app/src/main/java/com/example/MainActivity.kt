package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ime.model.KeyboardPreferences
import com.example.ui.screens.AppearanceScreen
import com.example.ui.screens.EngineSettingsScreen
import com.example.ui.screens.LanguagesScreen
import com.example.ui.screens.SandboxScreen
import com.example.ui.screens.ShortcutsScreen
import com.example.ui.theme.MyApplicationTheme

enum class NavTab(val title: String, val icon: ImageVector, val tag: String) {
    SANDBOX("Thử gõ", Icons.Default.EditNote, "tab_sandbox"),
    APPEARANCE("Giao diện", Icons.Default.Palette, "tab_appearance"),
    ENGINE("Cử chỉ & Gõ", Icons.Default.Tune, "tab_engine"),
    LANGUAGES("Ngôn ngữ", Icons.Default.Public, "tab_languages"),
    SHORTCUTS("Viết tắt", Icons.AutoMirrored.Filled.ShortText, "tab_shortcuts")
}

class MainActivity : ComponentActivity() {

    private lateinit var preferences: KeyboardPreferences

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = KeyboardPreferences.getInstance(this)

        setContent {
            MyApplicationTheme {
                var selectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = "AnyKeyboard26",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("main_bottom_nav"),
                            tonalElevation = 6.dp
                        ) {
                            NavTab.values().forEachIndexed { index, tab ->
                                NavigationBarItem(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    icon = {
                                        Icon(imageVector = tab.icon, contentDescription = tab.title)
                                    },
                                    label = { Text(text = tab.title) },
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> SandboxScreen(
                                preferences = preferences,
                                onNavigateToSettings = { selectedTab = 1 }
                            )
                            1 -> AppearanceScreen(preferences = preferences)
                            2 -> EngineSettingsScreen(preferences = preferences)
                            3 -> LanguagesScreen(preferences = preferences)
                            4 -> ShortcutsScreen()
                        }
                    }
                }
            }
        }
    }
}
