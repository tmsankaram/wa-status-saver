package com.deva.statussaver.util

import android.net.Uri

object SafUtils {
    // Basic path for standard WhatsApp.
    // Note: Android 11+ (API 30+) restricts access to Android/data and Android/media.
    // However, requesting ACTION_OPEN_DOCUMENT_TREE with this initial URI often helps the user land in the right place.
    // Encoded: Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses
    private const val WA_STATUS_PATH = "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"

    // For WhatsApp Business it would be: Android%2Fmedia%2Fcom.whatsapp.w4b%2FWhatsApp%20Business%2FMedia%2F.Statuses

    // Construct the initial URI for the intent.
    // This is the "Primary" storage volume format.
    val INITIAL_URI: Uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:$WA_STATUS_PATH")
}
