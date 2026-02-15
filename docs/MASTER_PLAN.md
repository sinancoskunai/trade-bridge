# trade-bridge Kurulum ve Gelistirme Master Plani

## Ozet

Bu proje tamamen izole bir yapida kurulur ve tum teknik/urun kararlarini repo icinde yasatir.

- Proje koku: `/Users/snn/fyi/dev/trade-bridge`
- Mimari: Monorepo
- Backend: Java Spring Boot
- Mobil: React Native Expo
- Yonetim: Web panel (Admin + Broker)
- Dil stratejisi: i18n (`tr-TR` default, `en-US` second, `zh-CN` planned)
- CI/CD: GitHub Actions + GHCR
- Deploy: Docker Compose on VPS
- Gelistirme modeli: Trunk-based + short-lived branches
- Kalite kapisi: Strict gates

## Klasor Yapisi

- `apps/backend`
- `apps/mobile`
- `apps/admin-web`
- `infra/docker`
- `infra/deploy`
- `docs`
- `.github/workflows`

## Izolasyon Garantisi

- Compose project name: `tradebridge`
- Container prefix: `tradebridge-*`
- Network: `tradebridge-net`
- Volume prefix: `tradebridge_*`
- DB: `tradebridge_app`, `tradebridge_audit`
- GHCR namespace: `ghcr.io/<hesap>/trade-bridge/*`
- Repo-local git identity kullanimi
- SSH alias ayrimi (`github-general`, alternatif `github-tradebridge`)

## Yasayan Dokumantasyon ve Yonetim

- Standartlar: `docs/STANDARDS.md`
- Prensipler: `docs/PRINCIPLES.md`
- Atlananlar: `docs/MISSED_PRINCIPLES.md`
- Karar gunlugu: `docs/DECISIONS.md`
- Analiz: `docs/ANALYSIS.md`
- API yuzeyi: `docs/API_SURFACE.md`
- CI/CD: `docs/CICD.md`
- Guvenlik: `docs/SECURITY.md`
- Faz plani: `docs/ROADMAP.md`
- Durum ve takvim: `docs/PROJECT_PLAN.md`

## Codex Skill Yonetimi

- Proje ici skill spec:
  - `docs/skills/TRADE_BRIDGE_REVIEW_SKILL_SPEC.md`
- Calisan skill:
  - `/Users/snn/.codex/skills/trade-bridge-review/SKILL.md`

## Public API (MVP Yuzeyi)

### Auth

- `POST /auth/register-company`
- `POST /auth/login`
- `POST /auth/refresh`
- `GET /users/me`
- `POST /admin/companies/{companyId}/approve`

### Kategori

- `GET /categories`
- `POST /admin/categories`
- `POST /admin/categories/{id}/attributes`

### Urun / Draft

- `POST /seller/products/drafts/upload`
- `GET /seller/products/drafts/{draftId}`
- `PUT /seller/products/drafts/{draftId}`
- `POST /seller/products/drafts/{draftId}/confirm`
- `GET /buyer/products`

### AI Arama

- `POST /buyer/search/qa`

### RFQ / Teklif

- `POST /buyer/rfqs`
- `POST /seller/rfqs/{id}/offers`
- `POST /buyer/offers/{id}/counter`
- `POST /buyer/offers/{id}/accept`
- `POST /buyer/offers/{id}/reject`
- `POST /broker/rfqs/{id}/interventions`

### Bildirim

- `GET /notifications`
- `POST /notifications/device-token`

## Fazlandirma

### Faz 0 - Foundation

- Monorepo iskeleti
- Dokumantasyon
- Local docker infra
- CI/CD iskeleti

### Faz 1 - Auth ve Tenant

- Sirket kaydi + admin onayi
- JWT + refresh
- RBAC + audit baseline

### Faz 2 - Kategori ve Urun Temeli

- Dinamik kategori semasi
- Satici manuel urun girisi
- Alici listeleme ve klasik filtre

### Faz 3 - AI Parse + Wizard

- Dokuman upload
- Parse pipeline
- Draft confidence + onay wizard

### Faz 4 - AI Q&A + RFQ/Teklif

- NL -> FilterDSL
- RFQ/teklif/karşi teklif
- Broker mudahale

### Faz 5 - Hardening

- Security hardening
- Performans
- Monitoring/alerting

## CI/CD Ozeti

### PR Pipeline

- Backend test
- Admin lint + test
- Mobile lint + test
- SAST + dependency + secret scan
- Dockerfile lint

### Main Pipeline

- Image build
- GHCR push

### Release Pipeline

- SSH deploy
- `docker compose pull && up -d`
- Smoke test + rollback

## Kalite Kriterleri

- Auth/RBAC ihlali yok
- Tenant izolasyonu korunur
- Required attribute eksiginde publish engeli
- Gecersiz state transition engeli
- Kritik aksiyonlar audit log'a duser
- TR/EN/ZH lokalizasyon fallback kurallari bozulmaz

## GitHub ve Hesap Ayrimi

- Repo: `sinancoskunai/trade-bridge`
- General SSH alias: `github-general`
- Alternatif izole alias: `github-tradebridge`

## Mevcut Durum (Bugun)

- Faz 0 tamamlandi.
- Faz 1 backend auth/tenant baseline tamamlandi (JWT + DB + Flyway + audit).
- Docker altyapisi ayakta (Postgres/Redis/MinIO).
- Faz 3 baslatildi (upload persistence + async parse job + admin parse ops).
