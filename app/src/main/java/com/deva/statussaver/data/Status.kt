package com.deva.statussaver.data

import android.net.Uri

data class Status(
    val uri: Uri,
    val name: String,
    val isVideo: Boolean,
    val timestamp: Long
)
