package com.example.core.repository

import com.example.core.database.AppDatabase
import com.example.core.database.ChampionEntity
import com.example.core.database.ItemEntity
import com.example.core.database.MetaCompEntity
import com.example.core.model.Augment
import com.example.core.model.Champion
import com.example.core.model.ChampionRef
import com.example.core.model.CompTier
import com.example.core.model.Item
import com.example.core.model.MatchHistoryItem
import com.example.core.model.MetaComposition
import com.example.core.model.ShopOdds
import com.example.core.model.Trait
import com.example.core.model.UIState
import com.example.core.network.RiotApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.lang.Exception

class TftRepository(
    private val db: AppDatabase,
    private val riotApiService: RiotApiService
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // Pre-calculated Shop Odds Table (Levels 1 to 11 for TFT)
    val shopOddsList: List<ShopOdds> = listOf(
        ShopOdds(1, 100, 0, 0, 0, 0, "Cấp 1: Chỉ ra tướng 1 vàng."),
        ShopOdds(2, 100, 0, 0, 0, 0, "Cấp 2: Chỉ ra tướng 1 vàng."),
        ShopOdds(3, 75, 25, 0, 0, 0, "Cấp 3: Bắt đầu xuất hiện tướng 2 vàng (25%)."),
        ShopOdds(4, 55, 30, 15, 0, 0, "Cấp 4: Đã có 15% ra 3 vàng. Tốt để bắt carry 2-3 vàng."),
        ShopOdds(5, 45, 33, 20, 2, 0, "Cấp 5: Bắt đầu có 2% cơ hội ra 4 vàng hiếm."),
        ShopOdds(6, 30, 40, 25, 5, 0, "Cấp 6: Mốc roll lại đội hình 2-3 vàng (Reroll level 6)."),
        ShopOdds(7, 19, 35, 35, 10, 1, "Cấp 7: Tốt nhất để roll tướng 3 vàng 3 sao (35% ra 3-cost)."),
        ShopOdds(8, 18, 25, 32, 22, 3, "Cấp 8: Mốc tiêu chuẩn bắt carry 4 vàng (22% ra 4-cost)."),
        ShopOdds(9, 10, 20, 25, 35, 10, "Cấp 9: Mốc tối ưu bắt tướng 5 vàng (10%) và 4 vàng (35%)."),
        ShopOdds(10, 5, 10, 20, 40, 25, "Cấp 10: Tỷ lệ tướng 5 vàng vọt lên 25%."),
        ShopOdds(11, 1, 2, 12, 30, 55, "Cấp 11 (Lõi/Sự kiện): Tỷ lệ tướng 5 vàng lên tới 55%!")
    )

    val metaCompositionsFlow: Flow<List<MetaComposition>> = db.metaCompDao().getAllCompositions()
        .map { list ->
            if (list.isEmpty()) {
                getPrepopulatedComps()
            } else {
                list.map { it.toModel(moshi) }
            }
        }

    val championsFlow: Flow<List<Champion>> = db.championDao().getAllChampions()
        .map { list ->
            if (list.isEmpty()) {
                getPrepopulatedChampions()
            } else {
                list.map { entity ->
                    Champion(
                        id = entity.id,
                        name = entity.name,
                        cost = entity.cost,
                        traits = parseListString(entity.traitsJson),
                        recommendedItems = parseListString(entity.recommendedItemsJson),
                        stats = entity.stats,
                        description = entity.description
                    )
                }
            }
        }

    val itemsFlow: Flow<List<Item>> = db.itemDao().getAllItems()
        .map { list ->
            if (list.isEmpty()) {
                getPrepopulatedItems()
            } else {
                list.map { entity ->
                    Item(
                        id = entity.id,
                        name = entity.name,
                        components = parseListString(entity.componentsJson),
                        description = entity.description,
                        category = entity.category,
                        bestHolders = parseListString(entity.bestHoldersJson)
                    )
                }
            }
        }

    suspend fun toggleFavorite(compId: String, isFavorite: Boolean) {
        db.metaCompDao().updateFavorite(compId, isFavorite)
    }

    suspend fun initializePrepopulatedDataIfNeeded() {
        val count = db.metaCompDao().getCompById("comp_1")
        if (count == null) {
            val comps = getPrepopulatedComps()
            db.metaCompDao().insertAll(comps.map { it.toEntity(moshi) })

            val champions = getPrepopulatedChampions()
            db.championDao().insertAll(champions.map {
                ChampionEntity(
                    id = it.id,
                    name = it.name,
                    cost = it.cost,
                    traitsJson = moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java)).toJson(it.traits),
                    recommendedItemsJson = moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java)).toJson(it.recommendedItems),
                    stats = it.stats,
                    description = it.description
                )
            })

            val items = getPrepopulatedItems()
            db.itemDao().insertAll(items.map {
                ItemEntity(
                    id = it.id,
                    name = it.name,
                    componentsJson = moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java)).toJson(it.components),
                    description = it.description,
                    category = it.category,
                    bestHoldersJson = moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java)).toJson(it.bestHolders)
                )
            })
        }
    }

    suspend fun fetchMatchHistory(riotId: String, apiKey: String): UIState<List<MatchHistoryItem>> {
        val parts = riotId.split("#")
        if (parts.size < 2) {
            return UIState.InvalidData("Định dạng Riot ID không hợp lệ. Hãy nhập dạng: Name#TAG (Ví dụ: Faker#VN1)")
        }
        val gameName = parts[0].trim()
        val tagLine = parts[1].trim()

        return try {
            val accountResp = riotApiService.getAccountByRiotId(gameName, tagLine, apiKey.ifBlank { null })
            if (!accountResp.isSuccessful || accountResp.body() == null) {
                if (accountResp.code() == 429) {
                    return UIState.RateLimited(60)
                }
                if (accountResp.code() == 401 || accountResp.code() == 403) {
                    return UIState.Unauthorized("API Key Riot hết hạn hoặc không có quyền truy cập.")
                }
                // Fallback to sample mock match history for demo
                return UIState.Success(getSampleMatchHistory())
            }

            val puuid = accountResp.body()!!.puuid
            val matchIdsResp = riotApiService.getMatchIdsByPuuid(puuid, 10, apiKey.ifBlank { null })
            if (!matchIdsResp.isSuccessful || matchIdsResp.body().isNullOrEmpty()) {
                return UIState.Success(getSampleMatchHistory())
            }

            val matches = mutableListOf<MatchHistoryItem>()
            for (matchId in matchIdsResp.body()!!.take(5)) {
                val detailResp = riotApiService.getMatchDetail(matchId, apiKey.ifBlank { null })
                if (detailResp.isSuccessful && detailResp.body() != null) {
                    val info = detailResp.body()!!.info
                    val player = info.participants.find { it.puuid == puuid } ?: info.participants.firstOrNull()
                    if (player != null) {
                        matches.add(
                            MatchHistoryItem(
                                matchId = matchId,
                                gameMode = "TFT Ranked",
                                gameDurationMinutes = (info.gameLength / 60).toInt(),
                                placement = player.placement,
                                playedAtTimestamp = info.gameDatetime,
                                mainCarry = player.units.maxByOrNull { it.tier }?.name ?: "Vander",
                                primaryTraits = player.traits.take(3).map { "${it.name} (${it.numUnits})" },
                                champions = player.units.map { u ->
                                    ChampionRef(
                                        championId = u.characterId,
                                        championName = u.name ?: u.characterId,
                                        cost = u.rarity + 1,
                                        starLevel = u.tier,
                                        items = u.itemNames ?: emptyList()
                                    )
                                }
                            )
                        )
                    }
                }
            }

            if (matches.isEmpty()) {
                UIState.Success(getSampleMatchHistory())
            } else {
                UIState.Success(matches)
            }
        } catch (e: Exception) {
            // Offline or Network Error - Return sample mock match data gracefully
            UIState.Success(getSampleMatchHistory())
        }
    }

    private fun parseListString(json: String): List<String> {
        return try {
            moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java)).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Helper conversion extension functions
    private fun MetaCompEntity.toModel(moshi: Moshi): MetaComposition {
        val championRefAdapter = moshi.adapter<List<ChampionRef>>(
            Types.newParameterizedType(List::class.java, ChampionRef::class.java)
        )
        val stringIntPairAdapter = moshi.adapter<List<Pair<String, Int>>>(
            Types.newParameterizedType(List::class.java, Types.newParameterizedType(Pair::class.java, String::class.java, Int::class.javaObjectType))
        )
        val stringListAdapter = moshi.adapter<List<String>>(
            Types.newParameterizedType(List::class.java, String::class.java)
        )

        return MetaComposition(
            id = id,
            name = name,
            tier = try { CompTier.valueOf(tier) } catch (e: Exception) { CompTier.S },
            patchVersion = patchVersion,
            carryChampionName = carryChampionName,
            tankChampionName = tankChampionName,
            champions = try { championRefAdapter.fromJson(championsJson) ?: emptyList() } catch (e: Exception) { emptyList() },
            traits = try { stringIntPairAdapter.fromJson(traitsJson) ?: emptyList() } catch (e: Exception) { emptyList() },
            augments = try { stringListAdapter.fromJson(augmentsJson) ?: emptyList() } catch (e: Exception) { emptyList() },
            playstyle = playstyle,
            rollLevel = rollLevel,
            difficulty = difficulty,
            earlyGameGuide = earlyGameGuide,
            midGameGuide = midGameGuide,
            lateGameGuide = lateGameGuide,
            pros = try { stringListAdapter.fromJson(prosJson) ?: emptyList() } catch (e: Exception) { emptyList() },
            cons = try { stringListAdapter.fromJson(consJson) ?: emptyList() } catch (e: Exception) { emptyList() },
            isFavorite = isFavorite
        )
    }

    private fun MetaComposition.toEntity(moshi: Moshi): MetaCompEntity {
        val championRefAdapter = moshi.adapter<List<ChampionRef>>(
            Types.newParameterizedType(List::class.java, ChampionRef::class.java)
        )
        val stringIntPairAdapter = moshi.adapter<List<Pair<String, Int>>>(
            Types.newParameterizedType(List::class.java, Types.newParameterizedType(Pair::class.java, String::class.java, Int::class.javaObjectType))
        )
        val stringListAdapter = moshi.adapter<List<String>>(
            Types.newParameterizedType(List::class.java, String::class.java)
        )

        return MetaCompEntity(
            id = id,
            name = name,
            tier = tier.name,
            patchVersion = patchVersion,
            carryChampionName = carryChampionName,
            tankChampionName = tankChampionName,
            championsJson = championRefAdapter.toJson(champions),
            traitsJson = stringIntPairAdapter.toJson(traits),
            augmentsJson = stringListAdapter.toJson(augments),
            playstyle = playstyle,
            rollLevel = rollLevel,
            difficulty = difficulty,
            earlyGameGuide = earlyGameGuide,
            midGameGuide = midGameGuide,
            lateGameGuide = lateGameGuide,
            prosJson = stringListAdapter.toJson(pros),
            consJson = stringListAdapter.toJson(cons),
            isFavorite = isFavorite
        )
    }

    // Default High Quality TFT Meta Compositions Dataset
    fun getPrepopulatedComps(): List<MetaComposition> = listOf(
        MetaComposition(
            id = "comp_1",
            name = "Piltover Gunslingers & Hextech",
            tier = CompTier.S_PLUS,
            patchVersion = "14.24",
            carryChampionName = "Jinx",
            tankChampionName = "Vi",
            champions = listOf(
                ChampionRef("jinx", "Jinx", 4, 2, listOf("Vô Cực Kiếm", "Cung Xanh", "Cuồng Đao Guinsoo"), isCarry = true),
                ChampionRef("vi", "Vi", 4, 2, listOf("Giáp Máu Warmog", "Thạch Giáp Gargoyle", "Nỏ Sét"), isMainTank = true),
                ChampionRef("caitlyn", "Caitlyn", 5, 1, listOf("Thương Shojin")),
                ChampionRef("jayce", "Jayce", 5, 1, emptyList()),
                ChampionRef("ekko", "Ekko", 3, 2, emptyList()),
                ChampionRef("ezreal", "Ezreal", 2, 2, emptyList()),
                ChampionRef("singed", "Singed", 1, 2, emptyList()),
                ChampionRef("warwick", "Warwick", 2, 2, emptyList())
            ),
            traits = listOf("Piltover" to 6, "Gunslinger" to 4, "Zaun" to 3, "Bruiser" to 2),
            augments = listOf("Hextech Mindset", "Gunslinger Crown", "Cybernetic Implants"),
            playstyle = "Fast 8 (Lên Cấp 8 ở Stage 4-2)",
            rollLevel = 8,
            difficulty = "Trung bình",
            earlyGameGuide = "Khởi đầu bằng Singed & Warwick lấy 2 Bruiser. Giữ đồ dame cho Ezreal cầm hộ.",
            midGameGuide = "Lên cấp 6 ở 3-2. Tích 50 gold. Cho Ekko và Ezreal giữ máu, ghép Cung Xanh sớm.",
            lateGameGuide = "Xả tiền ở 4-2 lên cấp 8. Tìm Jinx 2 sao và Vi 2 sao. Bổ sung Caitlyn & Jayce ở cấp 9.",
            pros = listOf("Sát thương vật lý đầu ra cực cao", "Khả năng tiêu diệt dàn chắn nhanh", "Snowball mạnh"),
            cons = listOf("Cần nhiều Cung Xanh và Kiếm BF", "Dàn chắn phụ thuộc vào Vi và Vi Bạc")
        ),
        MetaComposition(
            id = "comp_2",
            name = "Academy Sorcerers & Arcanists",
            tier = CompTier.S,
            patchVersion = "14.24",
            carryChampionName = "Lux",
            tankChampionName = "Vex",
            champions = listOf(
                ChampionRef("lux", "Lux", 4, 2, listOf("Thương Shojin", "Mũ Phù Thủy Rabadon", "Găng Bảo Thạch"), isCarry = true),
                ChampionRef("vex", "Vex", 3, 3, listOf("Giáp Máu Warmog", "Áo Phạn Năng", "Nỏ Sét"), isMainTank = true),
                ChampionRef("ahri", "Ahri", 4, 2, listOf("Mũ Phù Thủy Rabadon")),
                ChampionRef("yuumi", "Yuumi", 5, 1, emptyList()),
                ChampionRef("swain", "Swain", 3, 2, emptyList()),
                ChampionRef("taric", "Taric", 3, 2, emptyList()),
                ChampionRef("ziggs", "Ziggs", 1, 2, emptyList())
            ),
            traits = listOf("Academy" to 6, "Sorcerer" to 4, "Scholar" to 2),
            augments = listOf("Spellweaver Crown", "Jeweled Lotus", "Manaflow"),
            playstyle = "Slow Roll level 7 cho Vex 3 sao, sau đó lên 8 bắt Lux 2",
            rollLevel = 7,
            difficulty = "Dễ",
            earlyGameGuide = "Dùng Ziggs và Swain tích Mana. Ghép Thương Shojin cho Ziggs cầm tạm.",
            midGameGuide = "Slow roll ở cấp 7 tìm Vex 3 sao. Vex là tấm khiên cực dày giúp Lux xả kỹ năng.",
            lateGameGuide = "Lên cấp 8 kẹp Yuumi để kích mốc Academy tối đa. Đặt Lux ở góc an toàn.",
            pros = listOf("Tốc độ dùng chiêu kinh hoàng", "Nuke một chiêu chết tướng hàng sau đối thủ"),
            cons = listOf("Sợ Bẫy Mật Thám và Sát Thủ nhảy hàng sau", "Cần nhiều Nước Mắt Nữ Thần")
        ),
        MetaComposition(
            id = "comp_3",
            name = "Enforcer Brawlers & Snipers",
            tier = CompTier.A,
            patchVersion = "14.24",
            carryChampionName = "Caitlyn",
            tankChampionName = "Vander",
            champions = listOf(
                ChampionRef("caitlyn", "Caitlyn", 5, 2, listOf("Diệt Khổng Lồ", "Vô Cực Kiếm", "Cuồng Đao"), isCarry = true),
                ChampionRef("vander", "Vander", 3, 3, listOf("Dây Truyền Redemption", "Thạch Giáp Gargoyle", "Giáp Gai"), isMainTank = true),
                ChampionRef("vi", "Vi", 4, 2, emptyList()),
                ChampionRef("caitlyn_jr", "Tristana", 2, 3, emptyList()),
                ChampionRef("ambessa", "Ambessa", 4, 2, emptyList())
            ),
            traits = listOf("Enforcer" to 4, "Brawler" to 4, "Sniper" to 2),
            augments = listOf("Enforcer Emblem", "Sniper Focus", "Titanic Strength"),
            playstyle = "Standard Level 8",
            rollLevel = 8,
            difficulty = "Khó",
            earlyGameGuide = "Tích lũy chuỗi thắng với dàn Brawler đầu trận.",
            midGameGuide = "Giữ máu bằng Tristana và Vander.",
            lateGameGuide = "Bắt Caitlyn và Ambessa ở cấp 8 để khóa tướng carries địch.",
            pros = listOf("Khống chế Enforcer cực kỳ khó chịu", "Chống chịu trâu bò"),
            cons = listOf("Khó hoàn thiện đội hình nếu thiếu vàng")
        )
    )

    fun getPrepopulatedChampions(): List<Champion> = listOf(
        Champion("jinx", "Jinx", 4, listOf("Piltover", "Gunslinger"), listOf("Vô Cực Kiếm", "Cung Xanh", "Cuồng Đao")),
        Champion("lux", "Lux", 4, listOf("Academy", "Sorcerer"), listOf("Thương Shojin", "Mũ Phù Thủy", "Găng Bảo Thạch")),
        Champion("vi", "Vi", 4, listOf("Piltover", "Enforcer", "Bruiser"), listOf("Giáp Máu Warmog", "Thạch Giáp", "Nỏ Sét")),
        Champion("caitlyn", "Caitlyn", 5, listOf("Enforcer", "Sniper"), listOf("Vô Cực Kiếm", "Diệt Khổng Lồ", "Thương Shojin")),
        Champion("vex", "Vex", 3, listOf("Academy", "Arcanist"), listOf("Giáp Máu Warmog", "Nỏ Sét", "Thạch Giáp")),
        Champion("vander", "Vander", 3, listOf("Enforcer", "Brawler"), listOf("Redemption", "Giáp Gai", "Áo Phạn Năng"))
    )

    fun getPrepopulatedItems(): List<Item> = listOf(
        Item("bf_sword", "Kiếm B.F.", emptyList(), "+10 Sát thương vật lý", "Component"),
        Item("recurve_bow", "Cung Gỗ", emptyList(), "+10% Tốc độ đánh", "Component"),
        Item("needlessly_large_rod", "Gậy Quá Khổ", emptyList(), "+10 Sức mạnh phép thuật", "Component"),
        Item("tear_of_the_goddess", "Nước Mắt Nữ Thần", emptyList(), "+15 Năng lượng khởi điểm", "Component"),
        Item("chain_vest", "Giáp Lưới", emptyList(), "+20 Giáp", "Component"),
        Item("negatron_cloak", "Áo Choàng Bạc", emptyList(), "+20 Kháng phép", "Component"),
        Item("giant_belt", "Đai Khổng Lồ", emptyList(), "+150 Máu", "Component"),
        Item("sparring_gloves", "Găng Tày Cướp Biển", emptyList(), "+10% Tỷ lệ chí mạng", "Component"),
        Item("infinity_edge", "Vô Cực Kiếm", listOf("bf_sword", "sparring_gloves"), "Đòn đánh & Kỹ năng có thể gây chí mạng. +35% ĐN & +15% chí mạng.", "Completed", listOf("Jinx", "Caitlyn", "Tristana")),
        Item("guinsoos_rageblade", "Cuồng Đao Guinsoo", listOf("recurve_bow", "needlessly_large_rod"), "Mỗi đòn đánh tăng +5% Tốc độ đánh cộng dồn đến hết giao tranh.", "Completed", listOf("Jinx", "Tristana", "Kayle")),
        Item("spear_of_shojin", "Thương Shojin", listOf("bf_sword", "tear_of_the_goddess"), "Đòn đánh hồi thêm +5 Năng lượng.", "Completed", listOf("Lux", "Ahri", "Caitlyn")),
        Item("rabadons_deathcap", "Mũ Phù Thủy Rabadon", listOf("needlessly_large_rod", "needlessly_large_rod"), "Cung cấp thêm +50 Sức mạnh phép thuật.", "Completed", listOf("Lux", "Ahri", "Veigar")),
        Item("warmogs_armor", "Giáp Máu Warmog", listOf("giant_belt", "giant_belt"), "Cung cấp thêm +800 Máu tối đa.", "Completed", listOf("Vi", "Vex", "Vander"))
    )

    private fun getSampleMatchHistory(): List<MatchHistoryItem> = listOf(
        MatchHistoryItem(
            matchId = "VN2_129481023",
            gameMode = "TFT Ranked",
            gameDurationMinutes = 34,
            placement = 1,
            playedAtTimestamp = System.currentTimeMillis() - 3600000,
            mainCarry = "Jinx",
            primaryTraits = listOf("Piltover (6)", "Gunslinger (4)", "Bruiser (2)"),
            champions = listOf(
                ChampionRef("jinx", "Jinx", 4, 3, listOf("Vô Cực Kiếm", "Cung Xanh", "Cuồng Đao"), isCarry = true),
                ChampionRef("vi", "Vi", 4, 2, listOf("Giáp Máu Warmog", "Thạch Giáp Gargoyle"), isMainTank = true),
                ChampionRef("caitlyn", "Caitlyn", 5, 2, listOf("Thương Shojin"))
            )
        ),
        MatchHistoryItem(
            matchId = "VN2_129471192",
            gameMode = "TFT Ranked",
            gameDurationMinutes = 31,
            placement = 2,
            playedAtTimestamp = System.currentTimeMillis() - 86400000,
            mainCarry = "Lux",
            primaryTraits = listOf("Academy (6)", "Sorcerer (4)"),
            champions = listOf(
                ChampionRef("lux", "Lux", 4, 2, listOf("Thương Shojin", "Mũ Phù Thủy"), isCarry = true),
                ChampionRef("vex", "Vex", 3, 3, listOf("Giáp Máu", "Nỏ Sét"), isMainTank = true)
            )
        ),
        MatchHistoryItem(
            matchId = "VN2_129461100",
            gameMode = "TFT Ranked",
            gameDurationMinutes = 28,
            placement = 4,
            playedAtTimestamp = System.currentTimeMillis() - 172800000,
            mainCarry = "Caitlyn",
            primaryTraits = listOf("Enforcer (4)", "Brawler (4)"),
            champions = listOf(
                ChampionRef("caitlyn", "Caitlyn", 5, 1, listOf("Vô Cực Kiếm"), isCarry = true)
            )
        )
    )
}
