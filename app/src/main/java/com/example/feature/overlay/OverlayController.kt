package com.example.feature.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.example.core.database.AppDatabase
import com.example.core.datastore.OverlayPreferencesRepository
import com.example.core.network.NetworkModule
import com.example.core.repository.TftRepository
import com.example.ui.theme.MyApplicationTheme
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

class OverlayLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    fun onCreate() {
        savedStateRegistryController.performRestore(Bundle())
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    fun onStart() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}

class OverlayController(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var composeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val prefsRepo = OverlayPreferencesRepository(context)

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "tft_companion.db"
    ).fallbackToDestructiveMigration().build()

    private val repository = TftRepository(db, NetworkModule.createRiotApiService())

    var isExpanded by mutableStateOf(false)
    var isTouchPassthrough by mutableStateOf(false)
    var overlayAlpha by mutableStateOf(0.90f)

    private lateinit var layoutParams: WindowManager.LayoutParams

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    fun showOverlay() {
        if (composeView != null) return

        serviceScope.launch {
            repository.initializePrepopulatedDataIfNeeded()
            val initialSettings = prefsRepo.overlaySettingsFlow.first()
            overlayAlpha = initialSettings.alpha
            isTouchPassthrough = initialSettings.touchPassthrough

            setupWindowManager(initialSettings.posX, initialSettings.posY)
        }
    }

    private fun setupWindowManager(startX: Int, startY: Int) {
        val owner = OverlayLifecycleOwner()
        owner.onCreate()
        owner.onStart()
        lifecycleOwner = owner

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

        if (isTouchPassthrough) {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = startX
            y = startY
            alpha = overlayAlpha
        }

        val view = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)

            setContent {
                MyApplicationTheme {
                    OverlayContainerUi(
                        controller = this@OverlayController,
                        repository = repository,
                        prefsRepo = prefsRepo,
                        onDrag = { dx, dy -> handleDrag(dx, dy) },
                        onDragEnd = { snapToEdge() }
                    )
                }
            }
        }

        // Handle Touch Motion for window dragging
        view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null || isTouchPassthrough) return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (abs(dx) > 10 || abs(dy) > 10) {
                            layoutParams.x = initialX + dx
                            layoutParams.y = initialY + dy
                            try {
                                windowManager.updateViewLayout(view, layoutParams)
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                        return false
                    }
                    MotionEvent.ACTION_UP -> {
                        serviceScope.launch {
                            prefsRepo.updatePosition(layoutParams.x, layoutParams.y)
                        }
                        snapToEdge()
                        return false
                    }
                }
                return false
            }
        })

        composeView = view
        try {
            windowManager.addView(view, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleDrag(dx: Float, dy: Float) {
        if (::layoutParams.isInitialized && composeView != null) {
            layoutParams.x += dx.toInt()
            layoutParams.y += dy.toInt()
            try {
                windowManager.updateViewLayout(composeView, layoutParams)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun snapToEdge() {
        if (!::layoutParams.isInitialized || composeView == null) return
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val middle = screenWidth / 2

        layoutParams.x = if (layoutParams.x + 100 < middle) {
            10 // Snap to left edge
        } else {
            screenWidth - 220 // Snap to right edge
        }

        try {
            windowManager.updateViewLayout(composeView, layoutParams)
            serviceScope.launch {
                prefsRepo.updatePosition(layoutParams.x, layoutParams.y)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun toggleExpand() {
        isExpanded = !isExpanded
    }

    fun toggleTouchPassthrough(enabled: Boolean) {
        isTouchPassthrough = enabled
        if (!::layoutParams.isInitialized || composeView == null) return

        if (enabled) {
            layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        } else {
            layoutParams.flags = layoutParams.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        }

        try {
            windowManager.updateViewLayout(composeView, layoutParams)
            serviceScope.launch {
                prefsRepo.updateTouchPassthrough(enabled)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun setAlphaValue(value: Float) {
        overlayAlpha = value
        if (::layoutParams.isInitialized && composeView != null) {
            layoutParams.alpha = value
            try {
                windowManager.updateViewLayout(composeView, layoutParams)
                serviceScope.launch {
                    prefsRepo.updateAlpha(value)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun removeOverlay() {
        composeView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) { e.printStackTrace() }
        }
        composeView = null
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
        serviceScope.cancel()
    }
}
