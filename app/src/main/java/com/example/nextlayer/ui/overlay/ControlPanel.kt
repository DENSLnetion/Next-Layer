package com.example.nextlayer.ui.overlay

import android.service.notification.StatusBarNotification
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nextlayer.ui.components.AnimatedVolumeSlider

@Composable
fun ControlPanel(
    modifier: Modifier = Modifier,
    notifications: List<StatusBarNotification>,
    onClose: () -> Unit,
    transitionFraction: Float
) {
    var expandedTile by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
            .alpha(transitionFraction)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Control Panel",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.animateContentSize(animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MorphingTile(
                    modifier = Modifier.weight(if (expandedTile == "WiFi") 1f else 0.5f),
                    icon = Icons.Default.Wifi,
                    label = "Wi-Fi",
                    active = true,
                    isExpanded = expandedTile == "WiFi",
                    isAnotherExpanded = expandedTile != null && expandedTile != "WiFi",
                    onTap = { expandedTile = if (expandedTile == "WiFi") null else expandedTile },
                    onLongPress = { expandedTile = if (expandedTile == "WiFi") null else "WiFi" }
                )
                MorphingTile(
                    modifier = Modifier.weight(if (expandedTile == "BT") 1f else 0.5f),
                    icon = Icons.Default.Bluetooth,
                    label = "Bluetooth",
                    active = false,
                    isExpanded = expandedTile == "BT",
                    isAnotherExpanded = expandedTile != null && expandedTile != "BT",
                    onTap = { expandedTile = if (expandedTile == "BT") null else expandedTile },
                    onLongPress = { expandedTile = if (expandedTile == "BT") null else "BT" }
                )
            }
            
            AnimatedVisibility(visible = expandedTile == null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MorphingTile(
                        modifier = Modifier.weight(0.5f),
                        icon = Icons.Default.FlashlightOn,
                        label = "Flashlight",
                        active = false,
                        isExpanded = false,
                        isAnotherExpanded = false,
                        onTap = {},
                        onLongPress = {}
                    )
                    MorphingTile(
                        modifier = Modifier.weight(0.5f),
                        icon = Icons.Default.AirplanemodeActive,
                        label = "Airplane",
                        active = false,
                        isExpanded = false,
                        isAnotherExpanded = false,
                        onTap = {},
                        onLongPress = {}
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        MediaPlayerCard()

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Display & Audio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        AnimatedVolumeSlider()
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Recent Notifications",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        if (notifications.isEmpty()) {
            Text(
                text = "No new notifications",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                notifications.take(4).forEach { sbn ->
                    NotificationItem(sbn)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun MorphingTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    active: Boolean,
    isExpanded: Boolean,
    isAnotherExpanded: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    AnimatedVisibility(
        visible = !isAnotherExpanded,
        modifier = modifier
    ) {
        val height = if (isExpanded) 180.dp else 88.dp
        
        Box(
            modifier = Modifier
                .height(height)
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onLongPress = { onLongPress() }
                    )
                }
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    if (isExpanded) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (active) "On" else "Off",
                    style = MaterialTheme.typography.bodyMedium,
                    color = (if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f)
                )
                
                AnimatedVisibility(visible = isExpanded) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Available Networks", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("NextLayer_5G", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Composable
fun MediaPlayerCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(36.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lofi Vibes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Chillhop Music",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.onSecondaryContainer)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(28.dp))
                }
                IconButton(onClick = {}, modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.onSecondaryContainer, CircleShape)) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause", tint = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(28.dp))
                }
                IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
fun NotificationItem(sbn: StatusBarNotification) {
    // BUG FIX: Safety check buat extras notifikasi
    val title = sbn.notification.extras?.getString("android.title") ?: "Notification"
    val text = sbn.notification.extras?.getCharSequence("android.text")?.toString() ?: ""
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .clickable { /* Tindakan klik notif */ }
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (text.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
