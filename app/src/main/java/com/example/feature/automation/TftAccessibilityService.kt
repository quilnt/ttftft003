package com.example.feature.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TftAccessibilityService : AccessibilityService() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var autoClickJob: Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Accessibility events monitoring
    }

    override fun onInterrupt() {
        _isServiceActive.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceActive.value = false
        serviceJob.cancel()
    }

    fun clickAt(x: Float, y: Float, durationMs: Long = 50L, callback: ((Boolean) -> Unit)? = null) {
        val path = Path().apply {
            moveTo(x, y)
        }
        val gestureBuilder = GestureDescription.Builder()
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        gestureBuilder.addStroke(stroke)

        dispatchGesture(
            gestureBuilder.build(),
            object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    callback?.invoke(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    callback?.invoke(false)
                }
            },
            null
        )
    }

    fun startAutoClickLoop(x: Float, y: Float, intervalMs: Long, totalClicks: Int = Int.MAX_VALUE) {
        stopAutoClickLoop()
        _isAutoClicking.value = true
        autoClickJob = serviceScope.launch {
            var count = 0
            while (count < totalClicks && _isAutoClicking.value) {
                clickAt(x, y)
                count++
                delay(intervalMs.coerceAtLeast(100L))
            }
            _isAutoClicking.value = false
        }
    }

    fun stopAutoClickLoop() {
        autoClickJob?.cancel()
        autoClickJob = null
        _isAutoClicking.value = false
    }

    companion object {
        var instance: TftAccessibilityService? = null
            private set

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        private val _isAutoClicking = MutableStateFlow(false)
        val isAutoClicking: StateFlow<Boolean> = _isAutoClicking.asStateFlow()

        fun isAccessibilityEnabled(context: Context): Boolean {
            val expectedService = "${context.packageName}/${TftAccessibilityService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return enabledServices.contains(expectedService)
        }

        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}
