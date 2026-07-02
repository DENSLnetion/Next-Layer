package com.example.nextlayer.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedVolumeSlider(
    modifier: Modifier = Modifier,
    initialVolume: Float = 0.5f
) {
    var volume by remember { mutableFloatStateOf(initialVolume) }
    var isPressed by remember { mutableStateOf(false) } // Deteksi sentuhan buat Haptic Scale
    
    // Animasi volume (pergerakan baris isi)
    val animatedVolume by animateFloatAsState(
        targetValue = volume,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "volume_anim"
    )

    // Animasi scale (Chunky feel pas ditekan)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.04f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "scale_anim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp) // Lebih tebal (Chunky) khas M3 Expressive
            .scale(scale)
            .clip(RoundedCornerShape(36.dp)) // Radius 50% dari height
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isPressed = true },
                    onDragEnd = { isPressed = false },
                    onDragCancel = { isPressed = false }
                ) { change, _ ->
                    val width = size.width
                    val newVolume = (change.position.x / width).coerceIn(0f, 1f)
                    volume = newVolume
                }
            }
    ) {
        // Bar yang terisi
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedVolume)
                .background(MaterialTheme.colorScheme.primary)
        )
        
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Volume",
                tint = if (animatedVolume > 0.15f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.scale(if (isPressed) 1.1f else 1f) // Ikon ikut bereaksi
            )
        }
    }
}
