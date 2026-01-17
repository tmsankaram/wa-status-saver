package com.deva.statussaver

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.deva.statussaver.ui.MainViewModel
import com.deva.statussaver.ui.Screen
import com.deva.statussaver.ui.screens.HomeScreen
import com.deva.statussaver.ui.screens.PermissionScreen
import com.deva.statussaver.ui.screens.PreviewScreen
import com.deva.statussaver.ui.screens.SettingsScreen
import com.deva.statussaver.ui.theme.WAStatusSaverTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val folderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onPermissionGranted(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()

            WAStatusSaverTheme(darkTheme = darkModeEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val hasPermission by viewModel.hasPermission.collectAsState()
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val statuses by viewModel.statuses.collectAsState()

                    when {
                        !hasPermission -> {
                            PermissionScreen(
                                onPermissionGranted = { uri ->
                                    viewModel.onPermissionGranted(uri)
                                }
                            )
                        }
                        currentScreen is Screen.Settings -> {
                            SettingsScreen(
                                onBack = { viewModel.goBack() },
                                onChangeFolder = {
                                    folderPickerLauncher.launch(null)
                                },
                                darkModeEnabled = darkModeEnabled,
                                onDarkModeChanged = { enabled ->
                                    viewModel.setDarkMode(enabled)
                                }
                            )
                        }
                        currentScreen is Screen.Preview -> {
                            val previewState = currentScreen as Screen.Preview
                            val previewStatuses = remember(statuses, previewState.isVideo) {
                                if (previewState.isVideo) {
                                    statuses.filter { it.isVideo }
                                } else {
                                    statuses.filter { !it.isVideo }
                                }
                            }

                            PreviewScreen(
                                statuses = previewStatuses,
                                initialIndex = previewState.index,
                                onBack = { viewModel.goBack() },
                                onSave = { status -> viewModel.saveStatus(status) }
                            )
                        }
                        else -> {
                            val isLoading by viewModel.isLoading.collectAsState()

                            HomeScreen(
                                statuses = statuses,
                                isLoading = isLoading,
                                onRefreshed = { viewModel.refresh() },
                                onStatusClick = { status, index ->
                                    viewModel.openPreview(index, status.isVideo)
                                },
                                onSettingsClick = {
                                    viewModel.navigateTo(Screen.Settings)
                                },
                                onDeleteStatuses = { toDelete ->
                                    viewModel.deleteStatuses(toDelete)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
