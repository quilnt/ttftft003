package com.example.core.model

import androidx.annotation.DrawableRes

enum class CompTier {
    S_PLUS, S, A, B, C
}

data class Champion(
    val id: String,
    val name: String,
    val cost: Int,
    val traits: List<String>,
    val recommendedItems: List<String>,
    val stats: String = "HP: 650 | AD: 55 | Armor: 30",
    val description: String = ""
)

data class Trait(
    val id: String,
    val name: String,
    val breakpoints: List<Int>,
    val description: String,
    val effectAtBreakpoints: Map<Int, String> = emptyMap()
)

data class Item(
    val id: String,
    val name: String,
    val components: List<String>, // IDs of 2 component items, empty if component itself
    val description: String,
    val category: String, // "Component", "Completed", "Radiant", "Artifact"
    val bestHolders: List<String> = emptyList()
)

data class Augment(
    val id: String,
    val name: String,
    val tier: String, // "Silver", "Gold", "Prismatic"
    val description: String
)

data class ChampionRef(
    val championId: String,
    val championName: String,
    val cost: Int,
    val starLevel: Int = 2,
    val items: List<String> = emptyList(),
    val isCarry: Boolean = false,
    val isMainTank: Boolean = false
)

data class MetaComposition(
    val id: String,
    val name: String,
    val tier: CompTier,
    val patchVersion: String,
    val carryChampionName: String,
    val tankChampionName: String,
    val champions: List<ChampionRef>,
    val traits: List<Pair<String, Int>>, // Trait name to count
    val augments: List<String>,
    val playstyle: String, // "Slow Roll", "Fast 8", "Standard 4-cost"
    val rollLevel: Int,
    val difficulty: String, // "Dễ", "Trung bình", "Khó"
    val earlyGameGuide: String,
    val midGameGuide: String,
    val lateGameGuide: String,
    val pros: List<String>,
    val cons: List<String>,
    val isFavorite: Boolean = false
)

data class ShopOdds(
    val level: Int,
    val cost1Pct: Int,
    val cost2Pct: Int,
    val cost3Pct: Int,
    val cost4Pct: Int,
    val cost5Pct: Int,
    val rollTip: String
)

data class PatchManifest(
    val patchVersion: String,
    val releaseDate: String,
    val totalComps: Int,
    val checksum: String
)

data class MatchParticipant(
    val puuid: String,
    val riotIdName: String,
    val riotIdTag: String,
    val placement: Int,
    val level: Int,
    val goldLeft: Int,
    val lastRound: String,
    val totalDamageToPlayers: Int,
    val champions: List<ChampionRef>,
    val traits: List<Pair<String, Int>>
)

data class MatchHistoryItem(
    val matchId: String,
    val gameMode: String,
    val gameDurationMinutes: Int,
    val placement: Int,
    val participantCount: Int = 8,
    val playedAtTimestamp: Long,
    val mainCarry: String,
    val primaryTraits: List<String>,
    val champions: List<ChampionRef>
)

sealed interface UIState<out T> {
    data object Loading : UIState<Nothing>
    data class Success<T>(val data: T) : UIState<T>
    data object Empty : UIState<Nothing>
    data class Offline(val cachedData: Any? = null) : UIState<Nothing>
    data class RateLimited(val retryAfterSeconds: Int) : UIState<Nothing>
    data class Unauthorized(val message: String) : UIState<Nothing>
    data class ServerError(val message: String) : UIState<Nothing>
    data class InvalidData(val reason: String) : UIState<Nothing>
}
