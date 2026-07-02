package com.example.nextlayer.service

import android.content.Context
import android.graphics.PixelFormat
import android.service.notification.StatusBarNotification
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.nextlayer.ui.overlay.OverlayView
import com.example.nextlayer.ui.theme.NextLayerTheme
import kotlinx.coroutines.flow.StateFlow

/**
 * Controller khusus untuk WindowManager.
 * Memisahkan logika rendering dari Service utama.
 */
class OverlayController(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var composeView: ComposeView? = null
    private var lifecycleOwner: ComposeLifecycleOwner? = null

    var isAttached = false
        private set

    private var isExpanded = false

    // Layout Params dasar: Mulai sebagai Kapsul di tengah atas (Android 16 style)
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
        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        y = 50 // Margin dari atas
    }

    fun showOverlay(notificationsFlow: StateFlow<List<StatusBarNotification>>) {
        if (isAttached) return

        lifecycleOwner = ComposeLifecycleOwner().apply { start() }
        
        composeView = ComposeView(context).apply {
            // Tangkap tombol back pakai listener biasa (Karena ComposeView itu final)
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP && isExpanded) {
                    // Tarikan 2 nanti kita sambungin biar panel nutup dengan animasi
                    true
                } else {
                    false
                }
            }

            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            
            setContent {
                NextLayerTheme {
                    OverlayView(
                        notificationsFlow = notificationsFlow,
                        onExpansionIntent = { expand -> 
                            updateWindowFlags(expand) 
                        },
                        onAnimationFinished = { expanded ->
                            // Cleanup jika diperlukan pasca animasi
                        }
                    )
                }
            }
        }

        try {
            windowManager.addView(composeView, layoutParams)
            isAttached = true
        } catch (e: Exception) {
            e.printStackTrace()
            isAttached = false
        }
    }

    fun hideOverlay() {
        if (!isAttached) return
        composeView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        lifecycleOwner?.stop()
        composeView = null
        lifecycleOwner = null
        isAttached = false
    }

    /**
     * Rahasia Android 16 Feel: 
     * Saat kapsul -> Ga bisa di-fokus, sentuhan tembus ke belakang.
     * Saat panel -> Fokus penuh, nangkep tombol back, lebar full screen.
     */
    private fun updateWindowFlags(expand: Boolean) {
        if (isExpanded == expand || !isAttached) return
        isExpanded = expand
        
        if (expand) {
            // Jadi Panel Control (nangkep sentuhan & fokus)
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                                 WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        } else {
            // Kembali jadi Kapsul
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                 WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                                 WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        }

        try {
            composeView?.let { windowManager.updateViewLayout(it, layoutParams) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
