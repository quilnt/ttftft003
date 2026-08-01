package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.feature.automation.AutoClickerScreen
import com.example.feature.items.ItemsScreen
import com.example.feature.matchhistory.MatchHistoryScreen
import com.example.feature.meta.MetaCompsScreen
import com.example.feature.overlay.OverlayForegroundService
import com.example.feature.overlay.OverlayPermissionManager
import com.example.feature.settings.SettingsScreen
import com.example.feature.tools.AugmentAdvisorScreen
import com.example.feature.tools.SynergyBuilderScreen
import com.example.feature.tools.TftClientVersion
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

    val settings by prefsRepo.overlaySettingsFlow.collectAsState(
        initial = com.example.core.datastore.OverlaySettings()
    )

    val currentClient = remember(settings.selectedClientVersion) {
        TftClientVersion.fromId(settings.selectedClientVersion)
    }

    var showClientDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TFT Overlay Companion",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFFC8AA6E)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Client Badge Chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(currentClient.badgeColor)
                                    .clickable { showClientDialog = true }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = currentClient.regionBadge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            text = "${currentClient.displayName} • ${currentClient.activeSet}",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                actions = {
                    // Client Switcher Button
                    TextButton(
                        onClick = { showClientDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC8AA6E))
                    ) {
                        Text("Đổi Server", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

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
                    label = { Text("Đội Hình", fontSize = 9.sp) },
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
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "Lõi VIP") },
                    label = { Text("Lõi VIP", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F172A),
                        selectedTextColor = Color(0xFFC8AA6E),
                        indicatorColor = Color(0xFFC8AA6E),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_augments")
                )

                NavigationBarItem(
                    selected = selectedBottomTab == 2,
                    onClick = { selectedBottomTab = 2 },
                    icon = { Icon(Icons.Default.Extension, contentDescription = "Tộc Hệ VIP") },
                    label = { Text("Tộc Hệ VIP", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F172A),
                        selectedTextColor = Color(0xFFC8AA6E),
                        indicatorColor = Color(0xFFC8AA6E),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_synergy")
                )

                NavigationBarItem(
                    selected = selectedBottomTab == 3,
                    onClick = { selectedBottomTab = 3 },
                    icon = { Icon(Icons.Default.TouchApp, contentDescription = "Auto Click AI") },
                    label = { Text("Auto Click", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F172A),
                        selectedTextColor = Color(0xFFC8AA6E),
                        indicatorColor = Color(0xFFC8AA6E),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_autoclick")
                )

                NavigationBarItem(
                    selected = selectedBottomTab == 4,
                    onClick = { selectedBottomTab = 4 },
                    icon = { Icon(Icons.Default.GridOn, contentDescription = "Trang Bị") },
                    label = { Text("Trang Bị", fontSize = 9.sp) },
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
                    selected = selectedBottomTab == 5,
                    onClick = { selectedBottomTab = 5 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Cài Đặt") },
                    label = { Text("Cài Đặt", fontSize = 9.sp) },
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
                1 -> AugmentAdvisorScreen()
                2 -> SynergyBuilderScreen()
                3 -> AutoClickerScreen()
                4 -> ItemsScreen(repository)
                5 -> SettingsScreen(prefsRepo)
            }
        }

        // Client Switcher Dialog
        if (showClientDialog) {
            AlertDialog(
                onDismissRequest = { showClientDialog = false },
                containerColor = Color(0xFF0F172A),
                title = {
                    Text("🎮 Chọn Phiên Bản / Server TFT", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC8AA6E))
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Chọn client TFT bạn muốn hỗ trợ Overlay và Auto-Clicker:",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )

                        TftClientVersion.entries.forEach { client ->
                            val isSelected = client.id == settings.selectedClientVersion
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF0A0E17))
                                    .border(1.dp, if (isSelected) client.badgeColor else Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .clickable {
                                        scope.launch {
                                            prefsRepo.updateClientVersion(client.id)
                                            prefsRepo.updateActiveSetVersion(client.activeSet)
                                            showClientDialog = false
                                        }
                                    }
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                client.displayName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(client.badgeColor)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(client.regionBadge, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                        Text(client.activeSet, fontSize = 10.sp, color = Color(0xFFC8AA6E))
                                        Text(client.description, fontSize = 9.sp, color = Color(0xFF64748B))
                                    }

                                    if (isSelected) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = client.badgeColor)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showClientDialog = false }) {
                        Text("Đóng", color = Color(0xFFC8AA6E))
                    }
                }
            )
        }
    }
}

