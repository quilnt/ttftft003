package com.example.feature.automation

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiStrategyResult(
    val winRateEstimate: String,
    val summaryAdvice: String,
    val keyVictoryFactors: List<String>,
    val recommendedMacroType: String, // "ROLL", "LEVEL_UP", "BUY_SHOP", "HOLD_GOLD"
    val recommendedClicksCount: Int,
    val recommendedIntervalMs: Long,
    val targetX: Float,
    val targetY: Float,
    val reasoning: String
)

object TftAiStrategyAdvisor {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeAndRecommend(
        targetCompName: String,
        gameStage: String,
        currentGold: Int,
        playerHp: Int,
        streak: String,
        objective: String
    ): AiStrategyResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Fallback smart heuristic strategy if no valid key set
            return@withContext generateFallbackStrategy(targetCompName, gameStage, currentGold, playerHp, objective)
        }

        val promptText = """
            Bạn là một Chuyên Gia / Coach Đấu Trường Chân Lý (TFT / ĐTCL) chuyên nghiệp bậc Thách Đấu.
            Hãy phân tích tình huống trận đấu hiện tại và đưa ra lời khuyên + cấu hình Macro Auto-Clicker chính xác nhất để tối ưu tỷ lệ thắng (Win Rate).

            Thông tin trận đấu:
            - Đội hình mục tiêu: $targetCompName
            - Giai đoạn game: $gameStage
            - Số vàng hiện có: $currentGold gold
            - Máu của người chơi: $playerHp HP
            - Chuỗi thắng/thua: $streak
            - Mục tiêu ưu tiên: $objective

            Vui lòng trả về kết quả dạng JSON duy nhất không kèm markdown code block với cấu trúc:
            {
              "winRateEstimate": "Tỷ lệ TOP 1/4 dự kiến (ví dụ: 78% TOP 4, 32% TOP 1)",
              "summaryAdvice": "Lời khuyên chiến thuật ngắn gọn, súc tích",
              "keyVictoryFactors": ["Yếu tố thắng 1", "Yếu tố thắng 2", "Yếu tố thắng 3"],
              "recommendedMacroType": "ROLL", // Chỉ chọn 1 trong: ROLL, LEVEL_UP, BUY_SHOP, HOLD_GOLD
              "recommendedClicksCount": 5, // Số lần bấm phím auto
              "recommendedIntervalMs": 400, // Tốc độ bấm ms
              "targetX": 250, // Tọa độ X chuẩn TFT (250 cho Roll, 250 cho Level, 500 cho Mua Tướng)
              "targetY": 1850, // Tọa độ Y chuẩn TFT (1850 cho Roll/Shop, 1600 cho Level)
              "reasoning": "Giải thích lý do lựa chọn macro này"
            }
        """.trimIndent()

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", promptText)
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful && responseBody.isNotEmpty()) {
                val rootObj = JSONObject(responseBody)
                val candidates = rootObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val contentObj = candidates.getJSONObject(0).optJSONObject("content")
                    val partsArr = contentObj?.optJSONArray("parts")
                    if (partsArr != null && partsArr.length() > 0) {
                        var rawText = partsArr.getJSONObject(0).optString("text", "")
                        rawText = rawText.replace("```json", "").replace("```", "").trim()

                        val parsed = JSONObject(rawText)
                        val victoryFactors = mutableListOf<String>()
                        val factorsArr = parsed.optJSONArray("keyVictoryFactors")
                        if (factorsArr != null) {
                            for (i in 0 until factorsArr.length()) {
                                victoryFactors.add(factorsArr.getString(i))
                            }
                        }

                        return@withContext AiStrategyResult(
                            winRateEstimate = parsed.optString("winRateEstimate", "75% Top 4"),
                            summaryAdvice = parsed.optString("summaryAdvice", "Tập trung giữ kinh tế lợi tức 50 vàng."),
                            keyVictoryFactors = if (victoryFactors.isNotEmpty()) victoryFactors else listOf("Tích 50 vàng", "Xả vàng cấp 7", "Ghép đồ chuẩn"),
                            recommendedMacroType = parsed.optString("recommendedMacroType", "ROLL"),
                            recommendedClicksCount = parsed.optInt("recommendedClicksCount", 5),
                            recommendedIntervalMs = parsed.optLong("recommendedIntervalMs", 450L),
                            targetX = parsed.optDouble("targetX", 250.0).toFloat(),
                            targetY = parsed.optDouble("targetY", 1850.0).toFloat(),
                            reasoning = parsed.optString("reasoning", "Roll nhẹ để tìm khung bài 2 sao ổn định máu.")
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext generateFallbackStrategy(targetCompName, gameStage, currentGold, playerHp, objective)
    }

    private fun generateFallbackStrategy(
        targetCompName: String,
        gameStage: String,
        currentGold: Int,
        playerHp: Int,
        objective: String
    ): AiStrategyResult {
        val isLowHp = playerHp < 35
        val macroType = if (isLowHp) {
            "ROLL"
        } else if (currentGold >= 50 && gameStage.contains("Giai Đoạn 3")) {
            "LEVEL_UP"
        } else if (currentGold > 50) {
            "ROLL"
        } else {
            "HOLD_GOLD"
        }

        val targetX = when (macroType) {
            "LEVEL_UP" -> 250f
            "ROLL" -> 250f
            "BUY_SHOP" -> 500f
            else -> 250f
        }

        val targetY = when (macroType) {
            "LEVEL_UP" -> 1600f
            else -> 1850f
        }

        val clicks = if (isLowHp) 8 else if (currentGold >= 50) 4 else 1

        val factors = listOf(
            "Tối ưu hóa $targetCompName với carry chính chuẩn trang bị",
            "Giữ mốc lợi tức vàng từ 30G - 50G để tích lũy giá trị lâu dài",
            "Đổi lại cửa hàng ở thời điểm thích hợp để nâng cấp tướng 2 sao / 3 sao"
        )

        val advice = if (isLowHp) {
            "Máu nguy hiểm ($playerHp HP)! Xả hết vàng (All-in Roll) tìm bộ khung $targetCompName để giữ mạng!"
        } else {
            "Kinh tế ổn định ($currentGold Gold). Hãy lên cấp hoặc roll lợi tức trên 50 vàng để hoàn thiện $targetCompName."
        }

        return AiStrategyResult(
            winRateEstimate = if (isLowHp) "55% Top 4 (Cần All-in)" else "82% Top 4 (Kinh Tế Tốt)",
            summaryAdvice = advice,
            keyVictoryFactors = factors,
            recommendedMacroType = macroType,
            recommendedClicksCount = clicks,
            recommendedIntervalMs = 450L,
            targetX = targetX,
            targetY = targetY,
            reasoning = "Chiến thuật tự động phân tích dựa trên ngưỡng máu $playerHp HP và $currentGold vàng hiện tại."
        )
    }
}
