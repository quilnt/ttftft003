package com.example.feature.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.datastore.OverlayPreferencesRepository
import com.example.feature.overlay.OverlayForegroundService
import com.example.feature.overlay.OverlayPermissionManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    prefsRepo: OverlayPreferencesRepository
) {
    val context = LocalContext.current
    val settings by prefsRepo.overlaySettingsFlow.collectAsState(initial = com.example.core.datastore.OverlayPreferencesRepository(context).overlaySettingsFlow.collectAsState(initial = com.example.core.datastore.OverlaySettings()).value)
    val scope = rememberCoroutineScope()

    var hasOverlayPerm by remember { mutableStateOf(OverlayPermissionManager.hasOverlayPermission(context)) }
    var apiKeyInput by remember { mutableStateOf(settings.riotApiKey) }
    var proxyUrlInput by remember { mutableStateOf(settings.proxyBackendUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen")
    ) {
        Text(
            text = "Cấu hình Floating Overlay",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC8AA6E)
        )
        Text(
            text = "Cài đặt quyền hiển thị nổi, độ trong suốt và Riot API Key",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Card 1: Overlay Permission & Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Quyền hiển thị trên ứng dụng khác",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (hasOverlayPerm) "Đã cấp quyền SYSTEM_ALERT_WINDOW" else "Chưa cấp quyền - Cần thiết để hiển thị nổi trên game",
                            fontSize = 11.sp,
                            color = if (hasOverlayPerm) Color(0xFF22C55E) else Color(0xFFEF4444)
                        )
                    }

                    if (!hasOverlayPerm) {
                        Button(
                            onClick = {
                                OverlayPermissionManager.requestOverlayPermission(context)
                                hasOverlayPerm = OverlayPermissionManager.hasOverlayPermission(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8AA6E)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("request_overlay_perm_button")
                        ) {
                            Text("Cấp quyền", fontSize = 11.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Icon(Icons.Default.Check, contentDescription = "OK", tint = Color(0xFF22C55E))
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF334155))

                // Start / Stop Overlay Service
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bật / Tắt Bảng nổi TFT Overlay",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row {
                        Button(
                            onClick = {
                                if (OverlayPermissionManager.hasOverlayPermission(context)) {
                                    OverlayForegroundService.start(context)
                                } else {
                                    OverlayPermissionManager.requestOverlayPermission(context)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("start_overlay_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bật", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        OutlinedButton(
                            onClick = {
                                OverlayForegroundService.stop(context)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("stop_overlay_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tắt", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 2: Visual Adjustments (Opacity & Passthrough)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Tùy chỉnh giao diện nổi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Độ trong suốt (Opacity): ${(settings.alpha * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }

                Slider(
                    value = settings.alpha,
                    onValueChange = { alphaVal ->
                        scope.launch { prefsRepo.updateAlpha(alphaVal) }
                    },
                    valueRange = 0.3f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFC8AA6E),
                        activeTrackColor = Color(0xFFC8AA6E),
                        inactiveTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.testTag("opacity_slider")
                )

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF334155))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chế độ xuyên cảm ứng (Touch Passthrough)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Bỏ qua cảm ứng trên overlay để không bị bấm nhầm khi đang thao tác trong game TFT.",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Switch(
                        checked = settings.touchPassthrough,
                        onCheckedChange = { isChecked ->
                            scope.launch { prefsRepo.updateTouchPassthrough(isChecked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFC8AA6E),
                            checkedTrackColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("touch_passthrough_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 3: Riot API & Backend Proxy Config
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Riot API Key & Proxy Backend",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Dùng cho tính năng tra cứu lịch sử đấu Riot Games. Key được bảo mật không đính kèm vào APK sản xuất.",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = {
                        apiKeyInput = it
                        scope.launch { prefsRepo.updateRiotApiKey(it) }
                    },
                    label = { Text("Riot API Key (Ví dụ: RGAPI-xxxx-xxxx)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("riot_api_key_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFFC8AA6E),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = proxyUrlInput,
                    onValueChange = {
                        proxyUrlInput = it
                        scope.launch { prefsRepo.updateProxyUrl(it) }
                    },
                    label = { Text("Backend Proxy URL", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("proxy_url_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFFC8AA6E),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        }
    }
}
