package com.example.feature.tools

import androidx.compose.ui.graphics.Color

enum class TftClientVersion(
    val id: String,
    val displayName: String,
    val packageName: String,
    val regionBadge: String,
    val activeSet: String,
    val defaultRollX: Float,
    val defaultRollY: Float,
    val defaultXpX: Float,
    val defaultXpY: Float,
    val badgeColor: Color,
    val description: String
) {
    TFT_VNG(
        id = "TFT_VNG",
        displayName = "TFT VNG (Việt Nam)",
        packageName = "com.vng.tft",
        regionBadge = "VN 🇻🇳",
        activeSet = "Set 13 - Bước Vào Cõi Mộng",
        defaultRollX = 250f,
        defaultRollY = 1850f,
        defaultXpX = 250f,
        defaultXpY = 1600f,
        badgeColor = Color(0xFFEF4444),
        description = "Máy chủ VNG Mobile chính thức Việt Nam. Tọa độ tối ưu màn hình tỷ lệ 20:9."
    ),
    TFT_GLOBAL(
        id = "TFT_GLOBAL",
        displayName = "TFT Global (Riot)",
        packageName = "com.riotgames.league.teamfighttactics",
        regionBadge = "GLOBAL 🌐",
        activeSet = "Set 13 - Into The Arcane",
        defaultRollX = 240f,
        defaultRollY = 1820f,
        defaultXpX = 240f,
        defaultXpY = 1580f,
        badgeColor = Color(0xFF38BDF8),
        description = "Máy chủ Riot Games Quốc Tế chính thức. Hỗ trợ máy tính bảng & điện thoại đa tỉ lệ."
    ),
    TFT_PBE(
        id = "TFT_PBE",
        displayName = "TFT PBE (Thử Nghiệm)",
        packageName = "com.riotgames.league.teamfighttacticspbe",
        regionBadge = "PBE 🧪",
        activeSet = "Set 14 PBE (Mùa Mới)",
        defaultRollX = 260f,
        defaultRollY = 1880f,
        defaultXpX = 260f,
        defaultXpY = 1620f,
        badgeColor = Color(0xFFA855F7),
        description = "Máy chủ thử nghiệm Riot PBE. Cập nhật tướng, lõi & tộc hệ mùa mới nhất trước 2 tuần."
    ),
    TFT_NEW_SEASON(
        id = "TFT_NEW_SEASON",
        displayName = "TFT Thử Nghiệm Mùa Mới",
        packageName = "com.riotgames.league.teamfighttactics.alpha",
        regionBadge = "TEST 🚀",
        activeSet = "Set 14 Playtest & Prototype Meta",
        defaultRollX = 250f,
        defaultRollY = 1850f,
        defaultXpX = 250f,
        defaultXpY = 1600f,
        badgeColor = Color(0xFF10B981),
        description = "Chế độ thử nghiệm các siêu đội hình mùa mới & thuật toán Macro tự động nhận diện."
    );

    companion object {
        fun fromId(id: String): TftClientVersion {
            return entries.find { it.id == id } ?: TFT_VNG
        }
    }
}
