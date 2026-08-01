package com.example.feature.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.datastore.OverlayPreferencesRepository
import com.example.core.model.CompTier
import com.example.core.model.Item
import com.example.core.model.MetaComposition
import com.example.core.model.ShopOdds
import com.example.core.repository.TftRepository
import kotlinx.coroutines.launch

@Composable
fun OverlayContainerUi(
    controller: OverlayController,
    repository: TftRepository,
    prefsRepo: OverlayPreferencesRepository,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val isExpanded = controller.isExpanded

    Box(
        modifier = Modifier
            .testTag("overlay_container")
            .padding(4.dp)
    ) {
        if (!isExpanded) {
            BubbleOverlayView(
                controller = controller,
                onDrag = onDrag,
                onDragEnd = onDragEnd
            )
        } else {
            ExpandedOverlayPanel(
                controller = controller,
                repository = repository,
                prefsRepo = prefsRepo
            )
        }
    }
}

@Composable
fun BubbleOverlayView(
    controller: OverlayController,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val pulseScale by animateFloatAsState(targetValue = 1.0f, label = "pulse")

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(2.dp, Color(0xFFC8AA6E), CircleShape) // Hextech Gold border
            .shadow(8.dp, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    },
                    onDragEnd = { onDragEnd() }
                )
            }
            .clickable { controller.toggleExpand() }
            .testTag("overlay_bubble"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TFT",
                color = Color(0xFFC8AA6E),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "META",
                color = Color(0xFF38BDF8),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ExpandedOverlayPanel(
    controller: OverlayController,
    repository: TftRepository,
    prefsRepo: OverlayPreferencesRepository
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Đội Hình", "Trang Bị", "Shop Odds", "Ghi Chú", "Tra Cứu")
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .width(320.dp)
            .height(440.dp)
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .border(1.5.dp, Color(0xFFC8AA6E).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .testTag("overlay_expanded_panel"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A).copy(alpha = controller.overlayAlpha)
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TFT Companion",
                        color = Color(0xFFF8FAFC),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "v14.24",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Touch Passthrough Toggle
                    IconButton(
                        onClick = {
                            controller.toggleTouchPassthrough(!controller.isTouchPassthrough)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (controller.isTouchPassthrough) Icons.Default.Lock else Icons.Default.TouchApp,
                            contentDescription = "Chế độ xuyên cảm ứng",
                            tint = if (controller.isTouchPassthrough) Color(0xFFEF4444) else Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Collapse Button
                    IconButton(
                        onClick = { controller.toggleExpand() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Thu gọn panel",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF1E293B).copy(alpha = 0.8f),
                contentColor = Color(0xFFC8AA6E),
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFFC8AA6E),
                        height = 2.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) Color(0xFFC8AA6E) else Color(0xFF94A3B8)
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> OverlayCompsTab(repository)
                    1 -> OverlayItemsTab(repository)
                    2 -> OverlayShopOddsTab(repository, prefsRepo)
                    3 -> OverlayNotesTab()
                    4 -> OverlayQuickSearchTab(repository)
                }
            }
        }
    }
}

@Composable
fun OverlayCompsTab(repository: TftRepository) {
    val comps by repository.metaCompositionsFlow.collectAsState(initial = emptyList())
    var selectedTierFilter by remember { mutableStateOf<CompTier?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // Tier Filters Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            item {
                FilterChipMini(
                    label = "Tất cả",
                    isSelected = selectedTierFilter == null,
                    onClick = { selectedTierFilter = null }
                )
            }
            items(CompTier.entries.toTypedArray()) { tier ->
                FilterChipMini(
                    label = tier.name.replace("_PLUS", "+"),
                    isSelected = selectedTierFilter == tier,
                    onClick = { selectedTierFilter = tier }
                )
            }
        }

        val filteredComps = if (selectedTierFilter == null) comps else comps.filter { it.tier == selectedTierFilter }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(filteredComps) { comp ->
                MetaCompOverlayItem(
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

@Composable
fun FilterChipMini(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFC8AA6E) else Color(0xFF334155))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0)
        )
    }
}

@Composable
fun MetaCompOverlayItem(
    comp: MetaComposition,
    onFavoriteToggle: () -> Unit
) {
    var isExpandedDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { isExpandedDetails = !isExpandedDetails },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (comp.tier) {
                                    CompTier.S_PLUS -> Color(0xFFEF4444)
                                    CompTier.S -> Color(0xFFF97316)
                                    CompTier.A -> Color(0xFFA855F7)
                                    CompTier.B -> Color(0xFF3B82F6)
                                    CompTier.C -> Color(0xFF64748B)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = comp.tier.name.replace("_PLUS", "+"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = comp.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (comp.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Yêu thích",
                        tint = if (comp.isFavorite) Color(0xFFEAB308) else Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Key Carries
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Carry chính: ",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "${comp.carryChampionName} • ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
                Text(
                    text = "Tank: ",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = comp.tankChampionName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF22C55E)
                )
            }

            // Expanded Operating Guide
            if (isExpandedDetails) {
                Divider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = Color(0xFF334155)
                )
                Text(
                    text = "Lối chơi: ${comp.playstyle} (Cấp roll: ${comp.rollLevel})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC8AA6E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Đầu trận: ${comp.earlyGameGuide}",
                    fontSize = 9.sp,
                    color = Color(0xFFCBD5E1)
                )
                Text(
                    text = "Cuối trận: ${comp.lateGameGuide}",
                    fontSize = 9.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}

@Composable
fun OverlayItemsTab(repository: TftRepository) {
    val items by repository.itemsFlow.collectAsState(initial = emptyList())
    var selectedComponent1 by remember { mutableStateOf<Item?>(null) }
    var selectedComponent2 by remember { mutableStateOf<Item?>(null) }

    val components = items.filter { it.category == "Component" }
    val completedItems = items.filter { it.category == "Completed" }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Bộ ghép trang bị TFT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC8AA6E)
        )
        Text(
            text = "Chọn 2 trang bị thành phần để xem trang bị hoàn chỉnh:",
            fontSize = 9.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Component Selector Grid
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(components) { comp ->
                val isSelected = selectedComponent1?.id == comp.id || selectedComponent2?.id == comp.id
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Color(0xFFC8AA6E) else Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(6.dp))
                        .clickable {
                            if (selectedComponent1 == null) {
                                selectedComponent1 = comp
                            } else if (selectedComponent2 == null) {
                                selectedComponent2 = comp
                            } else {
                                selectedComponent1 = comp
                                selectedComponent2 = null
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = comp.name.take(2),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Recipe Result Output
        if (selectedComponent1 != null && selectedComponent2 != null) {
            val resultItem = completedItems.find { item ->
                (item.components.contains(selectedComponent1!!.id) && item.components.contains(selectedComponent2!!.id))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Kết quả ghép: ${resultItem?.name ?: "Không có công thức"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    if (resultItem != null) {
                        Text(
                            text = resultItem.description,
                            fontSize = 9.sp,
                            color = Color(0xFFCBD5E1)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tướng dùng tốt: ${resultItem.bestHolders.joinToString(", ")}",
                            fontSize = 9.sp,
                            color = Color(0xFFEAB308)
                        )
                    }
                }
            }
        } else {
            // Display all completed items list
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(completedItems) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E293B))
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF1F5F9)
                            )
                            Text(
                                text = item.description,
                                fontSize = 8.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OverlayShopOddsTab(
    repository: TftRepository,
    prefsRepo: OverlayPreferencesRepository
) {
    val scope = rememberCoroutineScope()
    var selectedLevel by remember { mutableIntStateOf(7) }
    val currentOdds = repository.shopOddsList.find { it.level == selectedLevel } ?: repository.shopOddsList[6]

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tỷ lệ Shop Cấp ${selectedLevel}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC8AA6E)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (selectedLevel > 1) {
                            selectedLevel--
                            scope.launch { prefsRepo.updatePlayerLevel(selectedLevel) }
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("-", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Lv $selectedLevel",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = {
                        if (selectedLevel < 11) {
                            selectedLevel++
                            scope.launch { prefsRepo.updatePlayerLevel(selectedLevel) }
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Probability Cost Bar Visualizers
        CostOddBar(cost = "1 Vàng (Xám)", pct = currentOdds.cost1Pct, color = Color(0xFF94A3B8))
        CostOddBar(cost = "2 Vàng (Lục)", pct = currentOdds.cost2Pct, color = Color(0xFF22C55E))
        CostOddBar(cost = "3 Vàng (Lam)", pct = currentOdds.cost3Pct, color = Color(0xFF3B82F6))
        CostOddBar(cost = "4 Vàng (Tím)", pct = currentOdds.cost4Pct, color = Color(0xFFA855F7))
        CostOddBar(cost = "5 Vàng (Cam)", pct = currentOdds.cost5Pct, color = Color(0xFFF97316))

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Lời khuyên vận hành:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
                Text(
                    text = currentOdds.rollTip,
                    fontSize = 9.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}

@Composable
fun CostOddBar(cost: String, pct: Int, color: Color) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = cost, fontSize = 9.sp, color = Color.White)
            Text(text = "$pct%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF334155))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct / 100f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun OverlayNotesTab() {
    val notes = remember { mutableStateListOf("Dành tiền lên Cấp 8 ở 4-2", "Đối thủ nhà #2 cầm Cung Xanh, né góc trái", "Tích 50 gold trước Vòng Lắp Đồ") }
    var newNoteText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Ghi chú trận đấu",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC8AA6E)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newNoteText,
                onValueChange = { newNoteText = it },
                placeholder = { Text("Thêm ghi chú...", fontSize = 9.sp, color = Color.Gray) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedBorderColor = Color(0xFFC8AA6E),
                    unfocusedBorderColor = Color(0xFF475569),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            IconButton(
                onClick = {
                    if (newNoteText.isNotBlank()) {
                        notes.add(newNoteText.trim())
                        newNoteText = ""
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm", tint = Color(0xFFC8AA6E))
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(notes) { note ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• $note",
                        fontSize = 9.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { notes.remove(note) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun OverlayQuickSearchTab(repository: TftRepository) {
    var query by remember { mutableStateOf("") }
    val champions by repository.championsFlow.collectAsState(initial = emptyList())

    val filteredChamps = if (query.isBlank()) champions else champions.filter {
        it.name.contains(query, ignoreCase = true) || it.traits.any { trait -> trait.contains(query, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Tìm tướng, tộc/hệ...", fontSize = 9.sp, color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B),
                focusedBorderColor = Color(0xFFC8AA6E),
                unfocusedBorderColor = Color(0xFF475569),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(filteredChamps) { champ ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${champ.cost}$",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEAB308),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Column {
                            Text(text = champ.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = champ.traits.joinToString(" • "), fontSize = 8.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }
    }
}
