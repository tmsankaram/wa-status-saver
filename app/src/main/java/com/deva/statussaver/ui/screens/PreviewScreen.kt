package com.deva.statussaver.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.deva.statussaver.data.Status
import com.deva.statussaver.ui.components.VideoPlayer

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreviewScreen(
    statuses: List<Status>,
    initialIndex: Int,
    onBack: () -> Unit,
    onSave: (Status) -> Unit
) {
    val context = LocalContext.current
    var showBars by remember { mutableStateOf(true) }

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (statuses.size - 1).coerceAtLeast(0)),
        pageCount = { statuses.size }
    )

    val currentStatus = statuses.getOrNull(pagerState.currentPage)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Swipeable content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showBars = !showBars
                },
            key = { statuses[it].uri.toString() }
        ) { page ->
            val status = statuses[page]

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (status.isVideo) {
                    // Only render video player for current page
                    if (page == pagerState.currentPage) {
                        VideoPlayer(status = status)
                    }
                } else {
                    ZoomableImage(status = status)
                }
            }
        }

        // Top bar
        if (showBars) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "${pagerState.currentPage + 1} / ${statuses.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )

                if (currentStatus != null) {
                    Text(
                        text = if (currentStatus.isVideo) "Video" else "Photo",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                    )
                }
            }
        }

        // Bottom bar
        if (showBars && currentStatus != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Share Button
                    OutlinedButton(
                        onClick = {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = if (currentStatus.isVideo) "video/*" else "image/*"
                                putExtra(android.content.Intent.EXTRA_STREAM, currentStatus.uri)
                                setPackage("com.whatsapp")
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(shareIntent)
                            } catch (e: Exception) {
                                // If WhatsApp not installed, use generic share
                                val genericIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = if (currentStatus.isVideo) "video/*" else "image/*"
                                    putExtra(android.content.Intent.EXTRA_STREAM, currentStatus.uri)
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(genericIntent, "Share via"))
                            }
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color.White)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Share",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Save Button
                    Button(
                        onClick = {
                            onSave(currentStatus)
                            Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Save",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomableImage(status: Status) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Reset zoom when status changes
    LaunchedEffect(status.uri) {
        scale = 1f
        offset = Offset.Zero
    }

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        if (scale > 1f) {
            offset += offsetChange
        } else {
            offset = Offset.Zero
        }
    }

    Image(
        painter = rememberAsyncImagePainter(status.uri),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .transformable(state = state),
        contentScale = ContentScale.Fit
    )
}

// Keep old signature for backward compatibility
@Composable
fun PreviewScreen(
    status: Status,
    onBack: () -> Unit,
    onSave: (Status) -> Unit
) {
    PreviewScreen(
        statuses = listOf(status),
        initialIndex = 0,
        onBack = onBack,
        onSave = onSave
    )
}
