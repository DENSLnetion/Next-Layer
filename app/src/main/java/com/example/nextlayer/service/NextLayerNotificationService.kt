package com.example.nextlayer.service

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.nextlayer.ui.overlay.OverlayView
import com.example.nextlayer.ui.theme.NextLayerTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class NextLayerNotificationService : NotificationListenerService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    val notificationsFlow = MutableStateFlow<List<StatusBarNotification>>(emptyList())
    private var isWindowExpanded = false
    private var isViewAttached = false

    private lateinit var sharedPreferences: SharedPreferences
    
    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == "isActive") {
            val isActive = prefs.getBoolean("isActive", false)
            updateOverlayState(isActive)
        }
    }

    private val layoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = 0
        y = 0
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        sharedPreferences = getSharedPreferences("NextLayerPrefs", Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefListener)
        
        val isActive = sharedPreferences.getBoolean("isActive", false)
        updateOverlayState(isActive)
    }

    private fun updateOverlayState(isActive: Boolean) {
        val hasOverlayPermission = Settings.canDrawOverlays(this)
        val shouldAttach = isActive && hasOverlayPermission
        
        if (shouldAttach && !isViewAttached) {
            setupOverlay()
        } else if (!shouldAttach && isViewAttached) {
            removeOverlay()
        }
    }

    private fun setupOverlay() {
        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@NextLayerNotificationService)
            setViewTreeViewModelStoreOwner(this@NextLayerNotificationService)
            setViewTreeSavedStateRegistryOwner(this@NextLayerNotificationService)
            setContent {
                NextLayerTheme {
                    OverlayView(
                        notificationsFlow = notificationsFlow,
                        onExpansionIntent = { expand ->
                            if (expand) {
                                setWindowExpanded(true)
                            }
                        },
                        onAnimationFinished = { expanded ->
                            if (!expanded) {
                                setWindowExpanded(false)
                            }
                        }
                    )
                }
            }
        }

        try {
            windowManager.addView(composeView, layoutParams)
            isViewAttached = true
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        } catch (e: Exception) {
            e.printStackTrace()
            isViewAttached = false
        }
    }

    private fun removeOverlay() {
        composeView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        composeView = null
        isViewAttached = false
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    private fun setWindowExpanded(expand: Boolean) {
        if (isWindowExpanded == expand || !isViewAttached) return
        isWindowExpanded = expand
        if (expand) {
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        } else {
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        try {
            composeView?.let { windowManager.updateViewLayout(it, layoutParams) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        notificationsFlow.update { currentList ->
            val mutableList = currentList.toMutableList()
            val existingIndex = mutableList.indexOfFirst { it.key == sbn.key }
            if (existingIndex != -1) {
                mutableList[existingIndex] = sbn
            } else {
                mutableList.add(0, sbn)
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
        removeOverlay()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}

