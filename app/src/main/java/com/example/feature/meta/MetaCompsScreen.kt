package com.example.feature.meta

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.core.model.CompTier
import com.example.core.model.MetaComposition
import com.example.core.repository.TftRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetaCompsScreen(repository: TftRepository) {
    val comps by repository.metaCompositionsFlow.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedTierFilter by remember { mutableStateOf<CompTier?>(null) }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val filteredComps = comps.filter { comp ->
        val matchesQuery = searchQuery.isBlank() ||
                comp.name.contains(searchQuery, ignoreCase = true) ||
                comp.carryChampionName.contains(searchQuery, ignoreCase = true) ||
                comp.traits.any { it.first.contains(searchQuery, ignoreCase = true) }

        val matchesTier = selectedTierFilter == null || comp.tier == selectedTierFilter
        val matchesFav = !showOnlyFavorites || comp.isFavorite

        matchesQuery && matchesTier && matchesFav
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
            .testTag("meta_comps_screen")
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Tìm tên đội hình, tướng carry, tộc/hệ...", color = Color.Gray) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFC8AA6E)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_comp_input"),
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

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    TierChip(
                        label = "Tất cả",
                        isSelected = selectedTierFilter == null && !showOnlyFavorites,
                        onClick = {
                            selectedTierFilter = null
                            showOnlyFavorites = false
                        }
                    )
                }
                item {
                    TierChip(
                        label = "⭐ Yêu thích",
                        isSelected = showOnlyFavorites,
                        onClick = {
                            showOnlyFavorites = !showOnlyFavorites
                            selectedTierFilter = null
                        }
                    )
                }
                items(CompTier.entries.toTypedArray()) { tier ->
                    TierChip(
                        label = "Tier ${tier.name.replace("_PLUS", "+")}",
                        isSelected = selectedTierFilter == tier && !showOnlyFavorites,
                        onClick = {
                            selectedTierFilter = tier
                            showOnlyFavorites = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Comps List
        if (filteredComps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không tìm thấy đội hình Meta phù hợp.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredComps) { comp ->
                    MetaCompCard(
                        comp = comp,
                        onFavoriteToggle = {
                            scope.launch {
                                repository.toggleFavorite(comp.id, !comp.isFavorite)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TierChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFFC8AA6E) else Color(0xFF1E293B))
            .border(1.dp, if (isSelected) Color(0xFFF59E0B) else Color(0xFF334155), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0)
        )
    }
}

@Composable
fun MetaCompCard(
    comp: MetaComposition,
    onFavoriteToggle: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (comp.tier) {
                                    CompTier.S_PLUS -> Color(0xFFEF4444)
                                    CompTier.S -> Color(0xFFF97316)
                                    CompTier.A -> Color(0xFFA855F7)
                                    CompTier.B -> Color(0xFF3B82F6)
                                    CompTier.C -> Color(0xFF64748B)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = comp.tier.name.replace("_PLUS", "+"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = comp.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "Phiên bản ${comp.patchVersion} • ${comp.playstyle}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (comp.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Thêm yêu thích",
                        tint = if (comp.isFavorite) Color(0xFFEAB308) else Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Champions Lineup Badges
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(comp.champions) { champ ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (champ.isCarry) Color(0xFF0284C7) else Color(0xFF334155))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${champ.championName} (${champ.cost}$)",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = if (champ.isCarry) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "📖 Hướng dẫn vận hành:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC8AA6E)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "• Đầu trận: ${comp.earlyGameGuide}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Text(text = "• Giữa trận: ${comp.midGameGuide}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Text(text = "• Cuối trận: ${comp.lateGameGuide}", fontSize = 11.sp, color = Color(0xFFCBD5E1))

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚡ Lõi nâng cấp đề xuất: ${comp.augments.joinToString(", ")}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }
    }
}
