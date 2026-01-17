package com.deva.statussaver.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deva.statussaver.data.Status
import com.deva.statussaver.data.StatusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class Screen {
    object Home : Screen()
    object Settings : Screen()
    data class Preview(val index: Int, val isVideo: Boolean) : Screen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StatusRepository(application)
    private val fileSaver = com.deva.statussaver.data.FileSaver(application)
    private val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _statuses = MutableStateFlow<List<Status>>(emptyList())
    val statuses: StateFlow<List<Status>> = _statuses.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _darkModeEnabled = MutableStateFlow(false)
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    // Filtered lists for Photos and Videos tabs
    val photoStatuses: List<Status>
        get() = _statuses.value.filter { !it.isVideo }

    val videoStatuses: List<Status>
        get() = _statuses.value.filter { it.isVideo }

    init {
        _darkModeEnabled.value = prefs.getBoolean("dark_mode", false)
        checkPermission()
    }

    fun checkPermission() {
        val uri = repository.getPersistedUri()
        if (uri != null) {
            _hasPermission.value = true
            loadStatuses(uri)
        } else {
            _hasPermission.value = false
        }
    }

    fun onPermissionGranted(uri: Uri) {
        val contentResolver = getApplication<Application>().contentResolver
        val takeFlags: Int = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, takeFlags)

        _hasPermission.value = true
        loadStatuses(uri)
    }

    fun refresh() {
        val uri = repository.getPersistedUri()
        if (uri != null) {
            loadStatuses(uri, forceRefresh = true)
        }
    }

    private fun loadStatuses(uri: Uri, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _statuses.value = withContext(Dispatchers.IO) {
                repository.getStatuses(uri, forceRefresh)
            }
            _isLoading.value = false
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openPreview(index: Int, isVideo: Boolean) {
        _currentScreen.value = Screen.Preview(index, isVideo)
    }

    fun goBack() {
        _currentScreen.value = Screen.Home
    }

    fun saveStatus(status: Status) {
        viewModelScope.launch {
            fileSaver.saveStatus(status)
        }
    }

    fun deleteStatuses(statusesToDelete: List<Status>) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            statusesToDelete.forEach { status ->
                try {
                    val docFile = DocumentFile.fromSingleUri(context, status.uri)
                    docFile?.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // Refresh the list
            refresh()
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _darkModeEnabled.value = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun clearPermissions() {
        val contentResolver = getApplication<Application>().contentResolver
        contentResolver.persistedUriPermissions.forEach { permission ->
            contentResolver.releasePersistableUriPermission(
                permission.uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
        _hasPermission.value = false
        _statuses.value = emptyList()
    }
}
