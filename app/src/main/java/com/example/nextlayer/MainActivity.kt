package com.example.nextlayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.nextlayer.service.NextLayerNotificationService
import com.example.nextlayer.ui.main.MainScreen
import com.example.nextlayer.ui.onboarding.OnboardingScreen
import com.example.nextlayer.ui.theme.NextLayerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private var hasOverlayPermission by mutableStateOf(false)
    private var hasNotificationPermission by mutableStateOf(false)
    private var isActive by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val sharedPrefs = getSharedPreferences("NextLayerPrefs", Context.MODE_PRIVATE)
        isActive = sharedPrefs.getBoolean("isActive", false)
        
        checkPermissions()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                checkPermissions()
            }
        }

        setContent {
            NextLayerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (hasOverlayPermission && hasNotificationPermission) {
                        MainScreen(
                            isActive = isActive,
                            onActiveChanged = { newActive ->
                                isActive = newActive
                                sharedPrefs.edit().putBoolean("isActive", newActive).apply()
                            }
                        )
                    } else {
                        OnboardingScreen(
                            hasOverlayPermission = hasOverlayPermission,
                            hasNotificationPermission = hasNotificationPermission,
                            onRequestOverlay = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:$packageName")
                                )
                                startActivity(intent)
                            },
                            onRequestNotification = {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                startActivity(intent)
                            },
                            onFinish = {
                                // Permissions are granted, UI will react to state changes
                                // Auto-enable if it's the first time
                                isActive = true
                                sharedPrefs.edit().putBoolean("isActive", true).apply()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private fun checkPermissions() {
        hasOverlayPermission = Settings.canDrawOverlays(this)
        val cn = ComponentName(this, NextLayerNotificationService::class.java)
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        hasNotificationPermission = enabledListeners?.contains(cn.flattenToString()) == true
    }
}

