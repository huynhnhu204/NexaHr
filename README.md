# NexaHR — Hệ thống Quản lý Nhân sự (HRM SaaS)

NexaHR là nền tảng HRM **đa công ty (multi-tenant)**, gồm web admin, careers portal công khai và app mobile. Phù hợp demo khách hàng, triển khai thương mại và bàn giao source code cho SME.

---

## Tổng quan dự án

| Thành phần | Mô tả | Công nghệ |
|------------|--------|-----------|
| **nexahr-backend** | REST API, JWT, phân quyền, upload file, seed demo | Spring Boot 3.2, Java 21, MySQL 8 |
| **nexahr-frontend** | Web app quản trị HR (SPA) | React 19, Vite 8, Ant Design 6, Redux |
| **nexahr-mobile** | App nhân viên (chấm công, nghỉ phép, lương) | React Native, Expo 52 |
| **docker-compose** | Chạy full stack (MySQL + API + Nginx frontend) | Docker Compose |

### URL mặc định (local)

| Dịch vụ | URL |
|---------|-----|
| Web app | http://localhost:5173 |
| API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API docs (JSON) | http://localhost:8080/api-docs |
| Careers portal | http://localhost:5173/careers/NEXA-DEMO |
| Upload ảnh chấm công | http://localhost:8080/uploads/... (cần JWT) |

---

## Tính năng chính

- **Đa công ty:** chuyển công ty, dữ liệu tách theo tenant
- **Phân quyền:** ADMIN / HR / MANAGER / EMPLOYEE (+ custom roles)
- **Nhân sự:** hồ sơ, phòng ban, chức vụ, sơ đồ tổ chức, tài liệu
- **Chấm công:** check-in/out có **ảnh minh chứng + GPS**, kiểm tra bán kính công ty (Haversine)
- **Nghỉ phép, bảng lương:** duyệt đơn, xuất Excel/PDF
- **Tuyển dụng:** job posting, ứng viên, phỏng vấn, careers portal công khai
- **Đào tạo, tài sản, KPI, báo cáo, AI Copilot**
- **Workflow, Data Hub, billing, SSO/SAML, Google login**
- **Thông báo, audit log, theme sáng/tối, ngôn ngữ VI/EN**

---

## Cấu trúc thư mục

```
NexaHR/
├── nexahr-backend/          # API Spring Boot
│   ├── src/main/java/       # Controllers, services, entities
│   ├── src/main/resources/  # application*.properties
│   ├── uploads/             # File upload (ảnh chấm công, tài liệu)
│   └── Dockerfile
├── nexahr-frontend/         # Web React
│   ├── src/features/        # Trang theo module (attendance, payroll, ...)
│   ├── src/services/        # axiosClient, apiEndpoints
│   ├── e2e/                 # Playwright E2E tests
│   ├── playwright.config.js
│   └── Dockerfile
├── nexahr-mobile/           # Expo app
│   ├── App.js
│   └── src/api.js
├── docs/                    # Tài liệu kỹ thuật
│   ├── API.md
│   ├── DATABASE.md
│   ├── DEPLOYMENT.md
│   ├── FEATURES.md
│   ├── QA_CHECKLIST.md
│   └── ROADMAP.md
└── docker-compose.yml
```

---

## Yêu cầu môi trường

| Công cụ | Phiên bản |
|---------|-----------|
| Node.js | 18+ (khuyến nghị 20+) |
| Java | **21** (theo `pom.xml`) |
| Maven | 3.8+ |
| MySQL | 8.0 (local, Herd, Laragon, hoặc Docker) |
| (Tuỳ chọn) Docker | Compose v2 |
| (Tuỳ chọn) Expo Go | Cho test mobile |

---

## Chạy local (khuyến nghị)

### Bước 1 — Database

Tạo database (hoặc để Spring tự tạo với `createDatabaseIfNotExist=true`):

```sql
CREATE DATABASE nexahr_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Bước 2 — Backend

```bash
cd nexahr-backend

# macOS + Laravel Herd / MySQL root không mật khẩu:
unset DB_PASSWORD

# Hoặc nếu MySQL có mật khẩu:
# export DB_PASSWORD=mat_khau_cua_ban

mvn spring-boot:run
```

**Lệnh build / test backend:**

```bash
cd nexahr-backend
mvn clean package -DskipTests    # Build JAR
mvn spring-boot:run              # Chạy dev
```

- API: http://localhost:8080/api  
- Swagger: http://localhost:8080/swagger-ui.html  
- Lần chạy đầu tự seed dữ liệu demo (`DataSeeder` + `DemoDataExpander`)

### Bước 3 — Frontend

```bash
cd nexahr-frontend
npm install
npm run dev
```

- Web: http://localhost:5173  
- Dev proxy: `/api` → `http://localhost:8080` (xem `vite.config.js`)

**Lệnh frontend khác:**

```bash
npm run build          # Build production → dist/
npm run preview        # Xem bản build local
npm run lint           # ESLint
npm run test:e2e       # Playwright (cần backend + frontend đang chạy)
npm run test:e2e:ui    # Playwright UI mode
```

> E2E lần đầu: `npx playwright install chromium`

### Bước 4 — Mobile (Expo)

```bash
cd nexahr-mobile
npm install

# Thay <IP-LAN> bằng IP máy (vd: 192.168.1.10), KHÔNG dùng localhost trên điện thoại thật
EXPO_PUBLIC_API_URL=http://<IP-LAN>:8080/api npm start
```

```bash
npm run android    # Mở trên emulator Android
npm run ios        # Mở trên simulator iOS (macOS)
```

---

## Chạy bằng Docker

```bash
# Từ thư mục gốc dự án
export JWT_SECRET=your-256-bit-secret-key-change-in-production
export DB_PASSWORD=nexahr

docker compose up --build
```

| Dịch vụ | Port |
|---------|------|
| Frontend (Nginx) | http://localhost:5173 |
| Backend | http://localhost:8080 |
| MySQL | localhost:3306 |

Dừng stack:

```bash
docker compose down
docker compose down -v   # Xóa cả volume MySQL (reset DB)
```

---

## Tài khoản & công ty demo

**Mật khẩu chung:** `123456`

| Email | Vai trò | Ghi chú |
|-------|---------|---------|
| admin@nexahr.com | ADMIN | Toàn quyền, cài đặt công ty |
| hr@nexahr.com | HR | Duyệt nghỉ phép, payroll, xem chấm công |
| manager@nexahr.com | MANAGER | Xem team, duyệt nghỉ phép |
| employee@nexahr.com | EMPLOYEE | Chấm công, tạo đơn nghỉ, xem lương |

**Công ty demo (chọn khi đăng nhập / Company Switcher):**

| Mã | Tên | Dữ liệu |
|----|-----|---------|
| `NEXA-DEMO` | NexaHR Demo | ~30 nhân viên, đầy đủ module (khuyến nghị test) |
| `NEXA-LABS` | NexaHR Labs | Công ty phụ, dữ liệu tối thiểu |

**Vị trí chấm công NexaHR Demo:** 53A Tăng Nhơn Phú, Phước Long B, TP. Thủ Đức  
Tọa độ: `10.8277714, 106.7715260` — bán kính **300m**

---

## Biến môi trường

### Backend

| Biến | Mặc định | Mô tả |
|------|----------|--------|
| `SPRING_PROFILES_ACTIVE` | `mysql` | Profile: `mysql`, `dev` (H2), `prod` |
| `DB_HOST` | `127.0.0.1` | Host MySQL |
| `DB_PORT` | `3306` | Port MySQL |
| `DB_USERNAME` | `root` | User MySQL |
| `DB_PASSWORD` | *(trống)* | Mật khẩu MySQL |
| `JWT_SECRET` | *(trong properties)* | **Bắt buộc đổi khi production** |
| `CORS_ORIGIN` | `http://localhost:5173` | Origin frontend được phép |
| `GOOGLE_CLIENT_ID` | — | Bật đăng nhập Google |
| `OPENAI_API_KEY` | — | Bật AI Copilot (không có = rule-based) |
| `STRIPE_API_KEY` | — | Bật thanh toán Stripe |

### Frontend

| File / Biến | Giá trị dev | Mô tả |
|-------------|-------------|--------|
| `.env.development` → `VITE_API_URL` | `/api` | Proxy qua Vite tới backend |
| `.env.production` → `VITE_API_URL` | `/api` | Build Docker / Nginx |

### Mobile

| Biến | Ví dụ | Mô tả |
|------|-------|--------|
| `EXPO_PUBLIC_API_URL` | `http://192.168.1.10:8080/api` | URL API từ thiết bị thật |

---

## Seed data & reset

- Dữ liệu mẫu tự chạy khi backend khởi động (`DataSeeder`, `DemoDataExpander`)
- Vị trí công ty demo được **patch mỗi lần restart** backend
- **Reset toàn bộ:** xóa database `nexahr_db` rồi chạy lại backend

```sql
DROP DATABASE nexahr_db;
CREATE DATABASE nexahr_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## Test chấm công (GPS + ảnh)

1. Đăng nhập `employee@nexahr.com` / `123456`, chọn **NexaHR Demo**
2. Vào http://localhost:5173/attendance
3. Cho phép quyền **vị trí** trên trình duyệt
4. Nếu test từ xa: Chrome DevTools → **Sensors** → Location → `10.8277714, 106.7715260`
5. Bấm **Check-in** → upload ảnh → xác nhận

---

## Troubleshooting

| Triệu chứng | Cách xử lý |
|-------------|------------|
| `Access denied for user 'root'` | `unset DB_PASSWORD` (Herd) hoặc `export DB_PASSWORD=...` |
| Port 8080 / 5173 đã dùng | `lsof -i :8080` / `lsof -i :5173` rồi dừng process |
| Nút Check-in bị disable | Restart backend; kiểm tra banner vàng trên trang attendance |
| API 404 `/companies/attendance-location` | Backend chưa build mới — `mvn spring-boot:run` lại |
| Trang trắng / Error Boundary | Kiểm tra backend đang chạy; xem Console browser |
| Đổi công ty lỗi 500 | Đăng xuất và đăng nhập lại |
| Mobile không kết nối API | Dùng IP LAN, không dùng `localhost` |
| E2E fail login 500 | Chạy test với `workers: 1` (đã cấu hình trong `playwright.config.js`) |
| `mvn install` lỗi quyền `.m2` | Dùng `mvn package` hoặc `mvn spring-boot:run` |

---

## Tài liệu kỹ thuật

| File | Nội dung |
|------|----------|
| [docs/FEATURES.md](docs/FEATURES.md) | Chi tiết từng module |
| [docs/API.md](docs/API.md) | REST API & auth |
| [docs/DATABASE.md](docs/DATABASE.md) | Schema, seed, multi-tenant |
| [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) | Triển khai production |
| [docs/QA_CHECKLIST.md](docs/QA_CHECKLIST.md) | Checklist kiểm thử UAT |
| [docs/ROADMAP.md](docs/ROADMAP.md) | Lộ trình phát triển |

---

## License

Source code bàn giao theo thỏa thuận với khách hàng. Không phân phối công khai nếu chưa được phép.
