package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.core.database.AppDatabase
import com.example.core.datastore.OverlayPreferencesRepository
import com.example.core.network.NetworkModule
import com.example.core.repository.TftRepository
import com.example.feature.items.ItemsScreen
import com.example.feature.matchhistory.MatchHistoryScreen
import com.example.feature.meta.MetaCompsScreen
import com.example.feature.overlay.OverlayForegroundService
import com.example.feature.overlay.OverlayPermissionManager
import com.example.feature.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var repository: TftRepository
    private lateinit var prefsRepo: OverlayPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "tft_companion.db"
        ).fallbackToDestructiveMigration().build()

        repository = TftRepository(db, NetworkModule.createRiotApiService())
        prefsRepo = OverlayPreferencesRepository(applicationContext)

        setContent {
            MyApplicationTheme {
                TftMainAppScreen(
                    repository = repository,
                    prefsRepo = prefsRepo
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TftMainAppScreen(
    repository: TftRepository,
    prefsRepo: OverlayPreferencesRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedBottomTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TFT Overlay Companion",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFFC8AA6E)
                        )
                        Text(
                            text = "Phiên bản 14.24 • Đội hình Meta & Bảng nổi",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (OverlayPermissionManager.hasOverlayPermission(context)) {
                                OverlayForegroundService.start(context)
                            } else {
                                OverlayPermissionManager.requestOverlayPermission(context)
                            }
                        },
                        modifier = Modifier.testTag("top_bar_start_overlay_button")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = Color(0xFF22C55E))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Bật Bảng Nổi",
                                tint = Color(0xFFC8AA6E)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E293B),
                contentColor = Color(0xFFC8AA6E),
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Meta") },
                    label = { Text("Đội Hình", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F172A),
                        selectedTextColor = Color(0xFFC8AA6E),
                        indicatorColor = Color(0xFFC8AA6E),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_comps")
                )

                NavigationBarItem(
                    selected = selectedBottomTab == 1,
                    onClick = { selectedBottomTab = 1 },
                    icon = { Icon(Icons.Default.GridOn, contentDescription = "Trang Bị") },
                    label = { Text("Trang Bị", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F172A),
                        selectedTextColor = Color(0xFFC8AA6E),
                        indicatorColor = Color(0xFFC8AA6E),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_items")
                )

                NavigationBarItem(
                    selected = selectedBottomTab == 2,
                    onClick = { selectedBottomTab = 2 },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Shop Odds") },
                    label = { Text("Tỷ Lệ Shop", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F172A),
                        selectedTextColor = Color(0xFFC8AA6E),
                        indicatorColor = Color(0xFFC8AA6E),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_shop")
                )

                NavigationBarItem(
                    selected = selectedBottomTab == 3,
                    onClick = { selectedBottomTab = 3 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Lịch Sử") },
                    label = { Text("Lịch Sử", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F172A),
                        selectedTextColor = Color(0xFFC8AA6E),
                        indicatorColor = Color(0xFFC8AA6E),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_history")
                )

                NavigationBarItem(
                    selected = selectedBottomTab == 4,
                    onClick = { selectedBottomTab = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Cài Đặt") },
                    label = { Text("Cài Đặt", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F172A),
                        selectedTextColor = Color(0xFFC8AA6E),
                        indicatorColor = Color(0xFFC8AA6E),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0F172A))
        ) {
            when (selectedBottomTab) {
                0 -> MetaCompsScreen(repository)
                1 -> ItemsScreen(repository)
                2 -> MainShopOddsScreen(repository, prefsRepo)
                3 -> MatchHistoryScreen(repository, prefsRepo)
                4 -> SettingsScreen(prefsRepo)
            }
        }
    }
}

@Composable
fun MainShopOddsScreen(
    repository: TftRepository,
    prefsRepo: OverlayPreferencesRepository
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        com.example.feature.overlay.OverlayShopOddsTab(
            repository = repository,
            prefsRepo = prefsRepo
        )
    }
}
