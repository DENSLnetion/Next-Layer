package com.example.nextlayer.ui.overlay

import android.service.notification.StatusBarNotification
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow

@Composable
fun OverlayView(
    notificationsFlow: StateFlow<List<StatusBarNotification>>,
    onExpansionIntent: (Boolean) -> Unit,
    onAnimationFinished: (Boolean) -> Unit
) {
    val notifications by notificationsFlow.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }
    var pullOffset by remember { mutableFloatStateOf(0f) }

    // Fisika Animasi Material 3 Expressive murni (Organik & Bouncy)
    val expressiveSpring = spring<Dp>(dampingRatio = 0.65f, stiffness = 300f)
    val alphaSpring = spring<Float>(dampingRatio = 1f, stiffness = 350f)

    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    val screenHeight = config.screenHeightDp.dp

    val baseCapsuleWidth = 140.dp
    val baseCapsuleHeight = 44.dp

    // Kalkulasi bentuk responsif (Shape-Shifting) saat ditarik
    val targetWidth = if (isExpanded) screenWidth else baseCapsuleWidth - (pullOffset.dp * 0.15f) // Ngetat pas ditarik
    val targetHeight = if (isExpanded) screenHeight else baseCapsuleHeight + pullOffset.dp
    val targetRadius = if (isExpanded) 0.dp else (baseCapsuleHeight + pullOffset.dp) / 2
    val targetPadding = if (isExpanded) 0.dp else 12.dp
    val targetAlpha = if (isExpanded) 1f else 0f
    val bgAlpha = if (isExpanded) 0.5f else 0f

    // Parameter dinamis
    val animatedWidth by animateDpAsState(targetValue = targetWidth, animationSpec = expressiveSpring, label = "width")
    val animatedRadius by animateDpAsState(targetValue = targetRadius, animationSpec = expressiveSpring, label = "radius")
    val animatedPadding by animateDpAsState(targetValue = targetPadding, animationSpec = expressiveSpring, label = "padding")
    
    // Penanganan presisi lifecycle WindowManager biar ga kepotong
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = expressiveSpring,
        label = "height",
        finishedListener = {
            if (!isExpanded) {
                onExpansionIntent(false) // Balikin ke WRAP_CONTENT pas udah ciut penuh
                onAnimationFinished(false)
            }
        }
    )

    val transitionFraction by animateFloatAsState(targetValue = targetAlpha, animationSpec = alphaSpring, label = "fraction")
    val animatedBgAlpha by animateFloatAsState(targetValue = bgAlpha, animationSpec = alphaSpring, label = "bgAlpha")

    // Minta Full Screen ke WindowManager *langsung* pas niat buka
    LaunchedEffect(isExpanded) {
        if (isExpanded) onExpansionIntent(true)
    }

    // Tahan layout full-screen di Compose selama animasi nutup belum beres
    val requiresFullScreen = isExpanded || transitionFraction > 0f

    Box(
        modifier = Modifier
            .background(Color.Black.copy(alpha = animatedBgAlpha))
            .then(if (requiresFullScreen) Modifier.fillMaxSize() else Modifier)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Snap threshold: Buka kalau tarikan tembus batas
                        if (!isExpanded && pullOffset > 70f) {
                            isExpanded = true
                        }
                        pullOffset = 0f
                    },
                    onDragCancel = { pullOffset = 0f }
                ) { change, dragAmount ->
                    change.consume()
                    if (!isExpanded) {
                        if (dragAmount.y > 0) {
                            pullOffset += dragAmount.y * 0.4f // Resistensi karet (Squish)
                        }
                    } else {
                        // Kalau panel udah buka, usap ke atas buat tutup
                        if (dragAmount.y < -20) {
                            isExpanded = false
                        }
                    }
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .padding(top = animatedPadding)
                .width(animatedWidth)
                .height(animatedHeight)
                .clip(RoundedCornerShape(animatedRadius))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // Matikan efek ripple bawaan biar kerasa native OS
                    enabled = !isExpanded
                ) {
                    isExpanded = true
                }
        ) {
            // Sinkronisasi Crossfade Mulus
            if (transitionFraction > 0.01f) {
                ControlPanel(
                    modifier = Modifier.fillMaxSize(),
                    notifications = notifications,
                    onClose = { isExpanded = false },
                    transitionFraction = transitionFraction
                )
            }
            if (transitionFraction < 0.99f) {
                CapsuleNotification(
                    modifier = Modifier.fillMaxSize(),
                    notification = notifications.firstOrNull(),
                    transitionFraction = transitionFraction,
                    isSquishing = pullOffset > 10f
                )
            }
        }
    }
}
