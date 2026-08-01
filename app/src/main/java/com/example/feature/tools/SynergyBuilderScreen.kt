package com.example.feature.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
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
import com.example.core.model.Champion

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SynergyBuilderScreen() {
    val allChampions = remember {
        listOf(
            Champion("c1", "Kassadin", 1, listOf("Giám Sát", "Mật Thám"), listOf("Vô Cực Kiếm", "Bàn Tay Công Lý")),
            Champion("c2", "Irelia", 1, listOf("Song Đấu", "Cảnh Binh"), listOf("Huyết Kiếm", "Quyền Nay")),
            Champion("c3", "Singed", 1, listOf("Hóa Kỹ", "Dũng Sĩ"), listOf("Nỏ Sét", "Giáp Gai")),
            Champion("c4", "Tristana", 2, listOf("Pháo Thủ", "Yordle"), listOf("Cuồng Đao", "Diệt Khổng Lồ")),
            Champion("c5", "Vi", 2, listOf("Dũng Sĩ", "Cảnh Binh"), listOf("Áo Choàng Lửa", "Dây Chuyền Chữ Thập")),
            Champion("c6", "Akali", 3, listOf("Mật Thám", "Song Đấu"), listOf("Vô Cực Kiếm", "Móng Vuốt Sterak")),
            Champion("c7", "Jinx", 3, listOf("Pháo Thủ", "Hóa Kỹ"), listOf("Cuồng Đao", "Diệt Khổng Lồ")),
            Champion("c8", "Ambessa", 4, listOf("Dũng Sĩ", "Giám Sát"), listOf("Huyết Kiếm", "Móng Vuốt Sterak")),
            Champion("c9", "Caitlyn", 4, listOf("Cảnh Binh", "Pháo Thủ"), listOf("Vô Cực Kiếm", "Cung Xanh")),
            Champion("c10", "Sevika", 5, listOf("Thần Thoại", "Hóa Kỹ"), listOf("Bàn Tay Công Lý", "Giáp Gai"))
        )
    }

    val selectedChamps = remember { mutableStateListOf<Champion>() }

    // Dynamic Traits Calculation
    val traitCounts = remember(selectedChamps.toList()) {
        val map = mutableMapOf<String, Int>()
        selectedChamps.forEach { champ ->
            champ.traits.forEach { trait ->
                map[trait] = (map[trait] ?: 0) + 1
            }
        }
        map.toList().sortedByDescending { it.second }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("synergy_builder_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🧬 Tính Toán Tộc Hệ VIP & Builder",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC8AA6E)
                )
                Text(
                    text = "Xây dựng đội hình thử nghiệm & tự động tối ưu hóa mốc kích hoạt Tộc Hệ",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
            Text(
                text = "Sức Mạnh: ${selectedChamps.size * 125} PT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Team Slot Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Đội Hình Hiện Tại (${selectedChamps.size}/10 Tướng)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (selectedChamps.isNotEmpty()) {
                        Text(
                            text = "Xóa Tất Cả",
                            fontSize = 10.sp,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { selectedChamps.clear() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedChamps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Bấm tướng bên dưới để thêm vào bàn cờ tính toán", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(selectedChamps) { champ ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(getCostColor(champ.cost).copy(alpha = 0.2f))
                                    .border(1.dp, getCostColor(champ.cost), RoundedCornerShape(8.dp))
                                    .clickable { selectedChamps.remove(champ) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${champ.name} (${champ.cost}G)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Active Traits Summary Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "⚡ Tộc Hệ Kích Hoạt Active Traits",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC8AA6E)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (traitCounts.isEmpty()) {
                    Text("Chưa có mốc Tộc Hệ nào được kích hoạt.", fontSize = 10.sp, color = Color(0xFF64748B))
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        traitCounts.forEach { (traitName, count) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, Color(0xFFC8AA6E), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFFC8AA6E), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$traitName ($count)",
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
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Danh Sách Tướng ĐTCL Mùa Mới (Chọn để thêm/bớt):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(allChampions) { champ ->
                val isSelected = selectedChamps.contains(champ)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isSelected) selectedChamps.remove(champ) else selectedChamps.add(champ)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF0A0E17)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFFC8AA6E) else getCostColor(champ.cost).copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = champ.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(getCostColor(champ.cost))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("${champ.cost}G", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Tộc hệ: ${champ.traits.joinToString(" • ")}",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF22C55E) else Color(0xFF334155))
                                .padding(6.dp)
                        ) {
                            Icon(
                                if (isSelected) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getCostColor(cost: Int): Color = when (cost) {
    1 -> Color(0xFF9E9E9E)
    2 -> Color(0xFF4CAF50)
    3 -> Color(0xFF2196F3)
    4 -> Color(0xFF9C27B0)
    5 -> Color(0xFFFF9800)
    else -> Color.White
}
