package com.example.feature.matchhistory

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.datastore.OverlayPreferencesRepository
import com.example.core.model.MatchHistoryItem
import com.example.core.model.UIState
import com.example.core.repository.TftRepository
import kotlinx.coroutines.launch

@Composable
fun MatchHistoryScreen(
    repository: TftRepository,
    prefsRepo: OverlayPreferencesRepository
) {
    val settings by prefsRepo.overlaySettingsFlow.collectAsState(initial = com.example.core.datastore.OverlaySettings())
    var riotIdInput by remember { mutableStateOf("Faker#VN1") }
    var uiState by remember { mutableStateOf<UIState<List<MatchHistoryItem>>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun performSearch() {
        isLoading = true
        scope.launch {
            val result = repository.fetchMatchHistory(riotIdInput, settings.riotApiKey)
            uiState = result
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
            .testTag("match_history_screen")
    ) {
        Text(
            text = "Lịch sử đấu Riot API",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC8AA6E)
        )
        Text(
            text = "Tra cứu phong độ, thứ hạng và đội hình kết thúc trận đấu",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = riotIdInput,
                onValueChange = { riotIdInput = it },
                placeholder = { Text("Nhập Riot ID (ví dụ: Faker#VN1)", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("riot_id_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedBorderColor = Color(0xFFC8AA6E),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { performSearch() },
                enabled = !isLoading && riotIdInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8AA6E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("search_riot_id_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF0F172A), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = Color(0xFF0F172A))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // State Output
        when (val state = uiState) {
            null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nhập Riot ID ở trên để tìm kiếm lịch sử đấu.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
            is UIState.InvalidData -> {
                Text(text = state.reason, color = Color(0xFFEF4444), fontSize = 12.sp)
            }
            is UIState.Unauthorized -> {
                Text(text = state.message, color = Color(0xFFF97316), fontSize = 12.sp)
            }
            is UIState.Success -> {
                val matches = state.data
                val top4Count = matches.count { it.placement <= 4 }
                val top1Count = matches.count { it.placement == 1 }
                val top4Rate = if (matches.isNotEmpty()) (top4Count * 100) / matches.size else 0

                // Stat Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Tổng số trận", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(text = "${matches.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Tỷ lệ Top 4", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(text = "$top4Rate%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Số trận Top 1", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(text = "$top1Count", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEAB308))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(matches) { match ->
                        MatchCard(match)
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun MatchCard(match: MatchHistoryItem) {
    val isTop4 = match.placement <= 4
    val isTop1 = match.placement == 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isTop1) Color(0xFFEAB308) else if (isTop4) Color(0xFF22C55E) else Color(0xFFEF4444),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placement Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isTop1) Color(0xFFEAB308) else if (isTop4) Color(0xFF15803D) else Color(0xFF991B1B)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "#${match.placement}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = match.gameMode,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "${match.gameDurationMinutes} phút",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Carry chính: ${match.mainCarry}",
                    fontSize = 11.sp,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(match.primaryTraits) { trait ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF334155))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = trait, fontSize = 9.sp, color = Color(0xFFE2E8F0))
                        }
                    }
                }
            }
        }
    }
}
