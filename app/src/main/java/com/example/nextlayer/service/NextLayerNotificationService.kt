package com.example.nextlayer.service

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Service utama yang sekarang sangat bersih.
 * Timpa total file NextLayerNotificationService.kt lu yang lama dengan kode ini.
 */
class NextLayerNotificationService : NotificationListenerService() {

    private val notificationsFlow = MutableStateFlow<List<StatusBarNotification>>(emptyList())
    private lateinit var overlayController: OverlayController
    private lateinit var sharedPreferences: SharedPreferences
    
    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == "isActive") {
            val isActive = prefs.getBoolean("isActive", false)
            updateOverlayState(isActive)
        }
    }

    override fun onCreate() {
        super.onCreate()
        overlayController = OverlayController(this)
        
        sharedPreferences = getSharedPreferences("NextLayerPrefs", Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefListener)
        
        val isActive = sharedPreferences.getBoolean("isActive", false)
        updateOverlayState(isActive)
    }

    private fun updateOverlayState(isActive: Boolean) {
        val hasOverlayPermission = Settings.canDrawOverlays(this)
        val shouldAttach = isActive && hasOverlayPermission
        
        if (shouldAttach) {
            overlayController.showOverlay(notificationsFlow)
        } else {
            overlayController.hideOverlay()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        notificationsFlow.update { currentList ->
            val mutableList = currentList.toMutableList()
            val existingIndex = mutableList.indexOfFirst { it.key == sbn.key }
            if (existingIndex != -1) {
                mutableList[existingIndex] = sbn // Update notifikasi lama
            } else {
                mutableList.add(0, sbn) // Notifikasi baru masuk di atas
            }
            mutableList
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        notificationsFlow.update { currentList ->
            currentList.filter { it.key != sbn.key }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(prefListener)
        overlayController.hideOverlay()
    }
}
