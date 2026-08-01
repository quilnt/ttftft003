package com.example.feature.automation

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import kotlinx.coroutines.launch

@Composable
fun AutoClickerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isAccessibilityEnabled by TftAccessibilityService.isServiceActive.collectAsState()
    val isAutoClicking by TftAccessibilityService.isAutoClicking.collectAsState()

    var targetXInput by remember { mutableStateOf("250") }
    var targetYInput by remember { mutableStateOf("1850") }
    var intervalMs by remember { mutableFloatStateOf(450f) }

    // AI State
    var aiTargetComp by remember { mutableStateOf("Kassadin Reroll 3 Sao") }
    var aiStage by remember { mutableStateOf("Giai Đoạn 3 (Mid Game)") }
    var aiGoldInput by remember { mutableStateOf("50") }
    var aiHpInput by remember { mutableStateOf("75") }
    var aiObjective by remember { mutableStateOf("Toàn Diện TOP 1") }

    var isAnalyzing by remember { mutableStateOf(false) }
    var aiResult by remember { mutableStateOf<AiStrategyResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("auto_clicker_screen")
    ) {
        Text(
            text = "⚡ TFT Smart Auto Clicker & AI Strategist",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC8AA6E)
        )
        Text(
            text = "Phân tích Gemini AI & Tự động hóa chiến thuật tối ưu hóa Win Rate",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // AI Strategy Coach Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🔮 AI Coach: Tối Ưu Chiến Thắng & Smart Macro",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE0E7FF)
                        )
                    }
                }

                Text(
                    text = "Nhập trạng thái trận đấu hiện tại để AI phân tích xác suất thắng & đề xuất Macro chuẩn xác:",
                    fontSize = 10.sp,
                    color = Color(0xFFA5B4FC),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Inputs
                OutlinedTextField(
                    value = aiTargetComp,
                    onValueChange = { aiTargetComp = it },
                    label = { Text("Đội hình đang chơi", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFF818CF8),
                        unfocusedBorderColor = Color(0xFF3730A3),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = aiGoldInput,
                        onValueChange = { aiGoldInput = it },
                        label = { Text("Số Vàng (G)", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color(0xFF3730A3),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = aiHpInput,
                        onValueChange = { aiHpInput = it },
                        label = { Text("Số Máu (HP)", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color(0xFF3730A3),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        isAnalyzing = true
                        scope.launch {
                            val gold = aiGoldInput.toIntOrNull() ?: 50
                            val hp = aiHpInput.toIntOrNull() ?: 75
                            val res = TftAiStrategyAdvisor.analyzeAndRecommend(
                                targetCompName = aiTargetComp,
                                gameStage = aiStage,
                                currentGold = gold,
                                playerHp = hp,
                                streak = "Thắng 2",
                                objective = aiObjective
                            )
                            aiResult = res
                            isAnalyzing = false
                        }
                    },
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_analyze_button")
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI Đang Phân Tích Game...", fontSize = 12.sp, color = Color.White)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Phân Tích AI & Đề Xuất Tối Ưu Chiến Thắng", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // AI Result Display
                aiResult?.let { res ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF4338CA), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📊 Tỷ Lệ Thắng Dự Kiến",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF22C55E))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = res.winRateEstimate,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "💡 Chiến Thuật: ${res.summaryAdvice}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "🎯 Yếu Tố Quyết Định Thắng Lợi:", fontSize = 10.sp, color = Color(0xFFC8AA6E), fontWeight = FontWeight.Bold)
                            res.keyVictoryFactors.forEach { factor ->
                                Text(text = "• $factor", fontSize = 10.sp, color = Color(0xFFCBD5E1))
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🤖 Đề Xuất Smart Macro: ${res.recommendedMacroType} (${res.recommendedClicksCount} lần chọc, ${res.recommendedIntervalMs}ms)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA855F7)
                            )
                            Text(
                                text = res.reasoning,
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    targetXInput = res.targetX.toInt().toString()
                                    targetYInput = res.targetY.toInt().toString()
                                    intervalMs = res.recommendedIntervalMs.toFloat()
                                    TftAccessibilityService.instance?.startAutoClickLoop(
                                        x = res.targetX,
                                        y = res.targetY,
                                        intervalMs = res.recommendedIntervalMs,
                                        totalClicks = res.recommendedClicksCount
                                    )
                                },
                                enabled = isAccessibilityEnabled,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("apply_ai_macro_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🚀 Chạy Smart Macro AI Đề Xuất Ngay",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Accessibility Permission Status
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Trạng thái Dịch Vụ Cảm Ứng (Accessibility Service)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isAccessibilityEnabled) "Đã bật dịch vụ - Sẵn sàng tự động chạm" else "Chưa bật dịch vụ - Cần bật trong Cài Đặt Hỗ Trợ Accessibility",
                            fontSize = 11.sp,
                            color = if (isAccessibilityEnabled) Color(0xFF22C55E) else Color(0xFFEF4444)
                        )
                    }

                    if (!isAccessibilityEnabled) {
                        Button(
                            onClick = { TftAccessibilityService.openAccessibilitySettings(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8AA6E)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("open_accessibility_perm_button")
                        ) {
                            Text("Bật Dịch Vụ", fontSize = 11.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Icon(Icons.Default.Check, contentDescription = "OK", tint = Color(0xFF22C55E))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset TFT Quick Buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🎯 Mẫu Vị Trí Thao Tác Chuẩn TFT Mobile",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Chọn vị trí để tự động điền tọa độ nút bấm game:",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetButton("🔄 Roll Shop (Roll 2G)", "250", "1850") { x, y ->
                        targetXInput = x
                        targetYInput = y
                    }
                    PresetButton("⬆️ Lên Cấp (XP)", "250", "1600") { x, y ->
                        targetXInput = x
                        targetYInput = y
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(text = "Tọa độ hàng tướng cửa hàng:", fontSize = 10.sp, color = Color(0xFFC8AA6E))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PresetButton("Ô 1", "500", "1850") { x, y -> targetXInput = x; targetYInput = y }
                    PresetButton("Ô 2", "700", "1850") { x, y -> targetXInput = x; targetYInput = y }
                    PresetButton("Ô 3", "900", "1850") { x, y -> targetXInput = x; targetYInput = y }
                    PresetButton("Ô 4", "1100", "1850") { x, y -> targetXInput = x; targetYInput = y }
                    PresetButton("Ô 5", "1300", "1850") { x, y -> targetXInput = x; targetYInput = y }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Coordinate & Interval Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "⚙️ Điều Chỉnh Tọa Độ & Tốc Độ Auto Click",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetXInput,
                        onValueChange = { targetXInput = it },
                        label = { Text("Tọa độ X (px)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedBorderColor = Color(0xFFC8AA6E),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = targetYInput,
                        onValueChange = { targetYInput = it },
                        label = { Text("Tọa độ Y (px)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
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

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Khoảng thời gian giữa các lần chọc (Interval): ${intervalMs.toInt()} ms",
                    fontSize = 12.sp,
                    color = Color.White
                )

                Slider(
                    value = intervalMs,
                    onValueChange = { intervalMs = it },
                    valueRange = 100f..2000f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFC8AA6E),
                        activeTrackColor = Color(0xFFC8AA6E),
                        inactiveTrackColor = Color(0xFF334155)
                    )
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF334155))

                // Action controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            val x = targetXInput.toFloatOrNull() ?: 300f
                            val y = targetYInput.toFloatOrNull() ?: 1800f
                            TftAccessibilityService.instance?.clickAt(x, y)
                        },
                        enabled = isAccessibilityEnabled,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chạm Thử 1 Lần", fontSize = 11.sp)
                    }

                    if (!isAutoClicking) {
                        Button(
                            onClick = {
                                val x = targetXInput.toFloatOrNull() ?: 300f
                                val y = targetYInput.toFloatOrNull() ?: 1800f
                                TftAccessibilityService.instance?.startAutoClickLoop(x, y, intervalMs.toLong())
                            },
                            enabled = isAccessibilityEnabled,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("start_auto_click_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bắt Đầu Auto", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                TftAccessibilityService.instance?.stopAutoClickLoop()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("stop_auto_click_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dừng Auto", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetButton(
    label: String,
    x: String,
    y: String,
    onClick: (String, String) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF334155))
            .clickable { onClick(x, y) }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

