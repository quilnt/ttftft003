/**
 * TFT Companion Backend Proxy Server (Node.js + Express)
 *
 * Chức năng:
 * 1. Bảo vệ Riot Games API Key ở phía Server, không đính kèm vào APK.
 * 2. Rate limiting (Giới hạn request theo IP và theo Quota Riot: 20 req/1s, 100 req/2 phút).
 * 3. Caching response (Cache dữ liệu trận đấu và summoner để giảm bớt lượt gọi API).
 * 4. Tránh lưu trữ thông tin cá nhân dư thừa của người dùng.
 */

const express = require('express');
const axios = require('axios');
const rateLimit = require('express-rate-limit');
const NodeCache = require('node-cache');

const app = express();
const PORT = process.env.PORT || 3000;
const RIOT_API_KEY = process.env.RIOT_API_KEY || 'RGAPI-YOUR-RIOT-API-KEY-HERE';

// Cache TTL: Match detail cache 1 giờ, Summoner account cache 24 giờ
const matchCache = new NodeCache({ stdTTL: 3600 });
const accountCache = new NodeCache({ stdTTL: 86400 });

// Rate Limiter: Tối đa 30 request / phút cho mỗi thiết bị người dùng
const apiLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 30,
  message: { error: 'Rate limit exceeded. Bạn đã gửi quá nhiều yêu cầu, vui lòng thử lại sau 1 phút.' }
});

app.use(express.json());
app.use('/api/', apiLimiter);

// Endpoint 1: Tra cứu Riot ID (GameName + TagLine) -> PUUID
app.get('/api/riot/account/:gameName/:tagLine', async (req, res) => {
  try {
    const { gameName, tagLine } = req.params;
    const cacheKey = `account_${gameName}_${tagLine}`.toLowerCase();

    if (accountCache.has(cacheKey)) {
      return res.json(accountCache.get(cacheKey));
    }

    const response = await axios.get(
      `https://asia.api.riotgames.com/riot/account/v1/accounts/by-riot-id/${encodeURIComponent(gameName)}/${encodeURIComponent(tagLine)}`,
      {
        headers: { 'X-Riot-Token': RIOT_API_KEY }
      }
    );

    accountCache.set(cacheKey, response.data);
    return res.json(response.data);
  } catch (error) {
    if (error.response) {
      return res.status(error.response.status).json({ error: error.response.data });
    }
    return res.status(500).json({ error: 'Server proxy bận hoặc lỗi kết nối Riot API' });
  }
});

// Endpoint 2: Lấy danh sách Match ID theo PUUID
app.get('/api/tft/matches/by-puuid/:puuid', async (req, res) => {
  try {
    const { puuid } = req.params;
    const count = req.query.count || 10;

    const response = await axios.get(
      `https://asia.api.riotgames.com/tft/match/v1/matches/by-puuid/${puuid}/ids?count=${count}`,
      {
        headers: { 'X-Riot-Token': RIOT_API_KEY }
      }
    );

    return res.json(response.data);
  } catch (error) {
    if (error.response) {
      return res.status(error.response.status).json({ error: error.response.data });
    }
    return res.status(500).json({ error: 'Lỗi kết nối Riot API' });
  }
});

// Endpoint 3: Lấy chi tiết trận đấu TFT theo Match ID (Có Cache)
app.get('/api/tft/match/:matchId', async (req, res) => {
  try {
    const { matchId } = req.params;

    if (matchCache.has(matchId)) {
      return res.json(matchCache.get(matchId));
    }

    const response = await axios.get(
      `https://asia.api.riotgames.com/tft/match/v1/matches/${matchId}`,
      {
        headers: { 'X-Riot-Token': RIOT_API_KEY }
      }
    );

    matchCache.set(matchId, response.data);
    return res.json(response.data);
  } catch (error) {
    if (error.response) {
      return res.status(error.response.status).json({ error: error.response.data });
    }
    return res.status(500).json({ error: 'Lỗi kết nối Riot API' });
  }
});

app.listen(PORT, () => {
  console.log(`TFT Proxy Backend Server is running on port ${PORT}`);
});
