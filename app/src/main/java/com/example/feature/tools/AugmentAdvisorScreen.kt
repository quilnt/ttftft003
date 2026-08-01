package com.example.feature.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.core.model.Augment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun AugmentAdvisorScreen() {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTierFilter by remember { mutableStateOf("Tất cả") }
    var userCurrentComp by remember { mutableStateOf("Kassadin Reroll 3 Sao") }

    var aiAdviceText by remember { mutableStateOf<String?>(null) }
    var isAnalyzingAi by remember { mutableStateOf(false) }

    val sampleAugments = remember {
        listOf(
            Augment("aug_1", "Vé Kim Cương", "Prismatic", "Khi đổi lại cửa hàng, bạn có 50% cơ hội nhận được 1 lần đổi lại miễn phí."),
            Augment("aug_2", "Khuyến Mãi Lớn", "Gold", "Tất cả các tướng trong cửa hàng của bạn được giảm 1 vàng (tối thiểu 1 vàng)."),
            Augment("aug_3", "Hy Sinh Cần Thiết", "Gold", "Khi linh thú chịu tổn hại, nhận ngay 3 vàng và 2 lượt đổi miễn phí."),
            Augment("aug_4", "Thượng Bằng Thượng", "Prismatic", "Nhận ngay 1 tướng 5 vàng ngẫu nhiên và 15 vàng tích lũy."),
            Augment("aug_5", "Đầu Đầu Tư Lợi Tức", "Silver", "Sau khi hoàn thành 5 vòng đấu với trên 50 vàng, nhận 1 Trang Bị Hoàn Chỉnh."),
            Augment("aug_6", "Trí Tuệ Nhân Tạo", "Silver", "Đội hình của bạn tăng 15% Tốc Độ Đánh và 150 Máu tối đa."),
            Augment("aug_7", "Cặp Đôi Hoàn Hảo", "Gold", "Nếu bạn ra trận đúng 2 bản sao của cùng 1 tướng, cả 2 nhận 30% AD và AP."),
            Augment("aug_8", "Xẻng Vàng Hextech", "Prismatic", "Nhận 1 Ấn Tộc Hệ Ngẫu Nhiên và 1 Máy Nhân Bản Tướng.")
        )
    }

    val filteredAugments = remember(searchQuery, selectedTierFilter) {
        sampleAugments.filter { aug ->
            val matchesQuery = aug.name.contains(searchQuery, ignoreCase = true) ||
                    aug.description.contains(searchQuery, ignoreCase = true)
            val matchesTier = when (selectedTierFilter) {
                "Bạc" -> aug.tier == "Silver"
                "Vàng" -> aug.tier == "Gold"
                "Kim Cương" -> aug.tier == "Prismatic"
                else -> true
            }
            matchesQuery && matchesTier
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("augment_advisor_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🏆 Lõi Công Nghệ & Augment VIP",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC8AA6E)
                )
                Text(
                    text = "Tra cứu tỷ lệ thắng, đánh giá xếp hạng S/A/B & Đề xuất AI",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFA855F7))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("VIP TIER LIST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search & Filter Row
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Tìm tên lõi, hiệu ứng...", fontSize = 12.sp, color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFC8AA6E)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B),
                focusedBorderColor = Color(0xFFC8AA6E),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tier Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Tất cả", "Bạc", "Vàng", "Kim Cương").forEach { tier ->
                val isSelected = selectedTierFilter == tier
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFFC8AA6E) else Color(0xFF1E293B))
                        .clickable { selectedTierFilter = tier }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tier,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AI Augment Selector Assistant
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🤖 AI Chấm Điểm Lõi Cho Đội Hình Của Bạn", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE0E7FF))
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = userCurrentComp,
                        onValueChange = { userCurrentComp = it },
                        label = { Text("Đội hình đang chơi", fontSize = 9.sp) },
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

                    Button(
                        onClick = {
                            isAnalyzingAi = true
                            scope.launch {
                                aiAdviceText = evaluateAugmentsWithAi(userCurrentComp)
                                isAnalyzingAi = false
                            }
                        },
                        enabled = !isAnalyzingAi,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isAnalyzingAi) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("AI Đánh Giá", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                aiAdviceText?.let { advice ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F172A))
                            .padding(8.dp)
                    ) {
                        Text(text = advice, fontSize = 10.sp, color = Color(0xFF38BDF8))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Augment List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filteredAugments) { augment ->
                AugmentItemCard(augment)
            }
        }
    }
}

@Composable
fun AugmentItemCard(augment: Augment) {
    val tierColor = when (augment.tier) {
        "Prismatic" -> Color(0xFFA855F7)
        "Gold" -> Color(0xFFC8AA6E)
        else -> Color(0xFF94A3B8)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tierColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = augment.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(tierColor.copy(alpha = 0.2f))
                        .border(1.dp, tierColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = augment.tier,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = tierColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = augment.description,
                fontSize = 11.sp,
                color = Color(0xFFCBD5E1)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Top Tier S", fontSize = 9.sp, color = Color(0xFFEAB308), fontWeight = FontWeight.Bold)
                }

                Text("Winrate: 54.2%", fontSize = 9.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                Text("Pickrate: 18.5%", fontSize = 9.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}

private suspend fun evaluateAugmentsWithAi(comp: String): String = withContext(Dispatchers.IO) {
    val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext "💡 [Gợi Ý Nhanh]: Với đội hình $comp, hãy ưu tiên các lõi tăng Tốc Độ Đánh, Lợi Tức Vàng, hoặc lượt Đổi Lại Cửa Hàng miễn phí để nhanh chóng nâng cấp 3 sao!"
    }

    try {
        val client = OkHttpClient()
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val prompt = "Đội hình TFT đang chơi: $comp. Hãy chọn và xếp hạng 3 Lõi Công Nghệ tốt nhất phù hợp nhất để đẩy tỷ lệ Win Rate lên cao nhất. Trả lời cực kỳ ngắn gọn dưới 3 dòng."

        val json = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""
        if (response.isSuccessful && responseBody.isNotEmpty()) {
            val root = JSONObject(responseBody)
            val text = root.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")
            if (!text.isNullOrBlank()) {
                return@withContext "🤖 AI Coach: ${text.trim()}"
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return@withContext "💡 [Gợi Ý Nhanh]: Cho $comp, chọn lõi Vé Kim Cương hoặc Khuyến Mãi Lớn để Reroll xả vàng tối đa."
}

