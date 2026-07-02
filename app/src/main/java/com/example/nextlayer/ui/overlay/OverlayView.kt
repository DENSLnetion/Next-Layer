package com.example.nextlayer.ui.overlay

import android.service.notification.StatusBarNotification
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
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

    val transition = updateTransition(targetState = isExpanded, label = "expansionTransition")

    LaunchedEffect(isExpanded) {
        onExpansionIntent(isExpanded)
    }

    LaunchedEffect(transition.currentState, transition.targetState) {
        if (transition.currentState == transition.targetState) {
            onAnimationFinished(transition.currentState)
        }
    }

    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    val screenHeight = config.screenHeightDp.dp

    val width by transition.animateDp(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) },
        label = "width"
    ) { state ->
        if (state) screenWidth else 140.dp
    }

    val height by transition.animateDp(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) },
        label = "height"
    ) { state ->
        if (state) screenHeight else 44.dp
    }

    val cornerRadius by transition.animateDp(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) },
        label = "cornerRadius"
    ) { state ->
        if (state) 0.dp else 22.dp
    }

    val paddingOffset by transition.animateDp(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) },
        label = "padding"
    ) { state ->
        if (state) 0.dp else 16.dp
    }
    
    val bgAlpha by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow) },
        label = "bgAlpha"
    ) { state ->
        if (state) 0.4f else 0.0f
    }

    Box(
        modifier = Modifier
            .background(Color.Black.copy(alpha = bgAlpha))
            .then(if (isExpanded || transition.isRunning) Modifier.fillMaxSize() else Modifier)
            .pointerInput(isExpanded) {
                if (isExpanded) {
                    detectDragGestures { change, dragAmount ->
                        if (dragAmount.y < -30) {
                            isExpanded = false
                        }
                    }
                }
            }
            .clickable(enabled = isExpanded) { isExpanded = false },
        contentAlignment = Alignment.TopStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = paddingOffset, y = paddingOffset)
                .width(width)
                .height(height)
                .clip(RoundedCornerShape(cornerRadius))
                .clickable(enabled = !isExpanded) { isExpanded = true }
        ) {
            if (isExpanded || transition.isRunning) {
                ControlPanel(
                    modifier = Modifier.fillMaxSize(),
                    notifications = notifications,
                    onClose = { isExpanded = false },
                    transitionFraction = transition.segment.targetState.let { if (it) 1f else 0f } // basic mapping
                )
            }
            if (!isExpanded || transition.isRunning) {
                CapsuleNotification(
                    modifier = Modifier.fillMaxSize(),
                    notification = notifications.firstOrNull(),
                    transitionFraction = transition.segment.targetState.let { if (it) 1f else 0f }
                )
            }
        }
    }
}
