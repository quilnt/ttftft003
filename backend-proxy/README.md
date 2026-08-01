# TFT Companion Backend Proxy

## Lý do chọn Node.js Express / Cloudflare Workers cho Backend Proxy
1. **Bảo vệ Riot API Key**: Giữ Riot Developer / Production API Key trên server, tránh bị decompilation APK khai thác.
2. **Rate Limiting**: Riot Developer API giới hạn 20 req/1s và 100 req/2 phút. Proxy áp dụng rate limiter giúp dàn đều request.
3. **Caching Response**: Chi tiết trận đấu TFT sau khi kết thúc là dữ liệu bất biến. Proxy cache lại kết quả trong 1 giờ giúp giảm 80% lượt gọi Riot API.
4. **Không lưu trữ PII**: Proxy chỉ trung chuyển dữ liệu trận đấu, không ghi lại thông tin cá nhân hay lưu log nhạy cảm của người chơi.

## Hướng dẫn Deploy lên Render / Railway / Cloudflare Workers
1. `npm install express axios express-rate-limit node-cache`
2. Đặt biến môi trường `RIOT_API_KEY=RGAPI-xxx` trên dashboard dịch vụ hosting.
3. Chạy `node server.js` hoặc cấu hình Docker container.
4. Nhập URL của Backend Proxy vào mục **Cài Đặt** trong ứng dụng Android TFT Overlay.
