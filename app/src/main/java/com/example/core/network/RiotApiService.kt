package com.example.core.network

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class SummonerDto(
    val id: String?,
    val accountId: String?,
    val puuid: String?,
    val name: String?,
    val profileIconId: Int?,
    val summonerLevel: Long?
)

data class RiotAccountDto(
    val puuid: String,
    val gameName: String,
    val tagLine: String
)

data class MatchDto(
    val metadata: MatchMetadataDto,
    val info: MatchInfoDto
)

data class MatchMetadataDto(
    val dataVersion: String,
    val matchId: String,
    val participants: List<String>
)

data class MatchInfoDto(
    val gameDatetime: Long,
    val gameLength: Float,
    val gameVariation: String?,
    val gameVersion: String,
    val participants: List<ParticipantDto>
)

data class ParticipantDto(
    val puuid: String,
    val placement: Int,
    val level: Int,
    @Json(name = "gold_left") val goldLeft: Int,
    @Json(name = "last_round") val lastRound: Int,
    @Json(name = "total_damage_to_players") val totalDamageToPlayers: Int,
    val units: List<UnitDto>,
    val traits: List<TraitDto>
)

data class UnitDto(
    @Json(name = "character_id") val characterId: String,
    val name: String?,
    val rarity: Int,
    val tier: Int, // star level
    @Json(name = "itemNames") val itemNames: List<String>?
)

data class TraitDto(
    val name: String,
    @Json(name = "num_units") val numUnits: Int,
    val tier_current: Int,
    val tier_total: Int
)

interface RiotApiService {
    @GET("riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}")
    suspend fun getAccountByRiotId(
        @Path("gameName") gameName: String,
        @Path("tagLine") tagLine: String,
        @Header("X-Riot-Token") apiKey: String? = null
    ): Response<RiotAccountDto>

    @GET("tft/match/v1/matches/by-puuid/{puuid}/ids")
    suspend fun getMatchIdsByPuuid(
        @Path("puuid") puuid: String,
        @Query("count") count: Int = 10,
        @Header("X-Riot-Token") apiKey: String? = null
    ): Response<List<String>>

    @GET("tft/match/v1/matches/{matchId}")
    suspend fun getMatchDetail(
        @Path("matchId") matchId: String,
        @Header("X-Riot-Token") apiKey: String? = null
    ): Response<MatchDto>
}

object NetworkModule {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    fun createRiotApiService(baseUrl: String = "https://asia.api.riotgames.com/"): RiotApiService {
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RiotApiService::class.java)
    }
}
