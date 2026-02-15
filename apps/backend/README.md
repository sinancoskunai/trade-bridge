# trade-bridge backend

## Purpose

Spring Boot API for Trade Bridge domain workflows.

## Bootstrapped endpoints

- Auth: `/auth/register-company`, `/auth/login`, `/auth/refresh`, `/users/me`
- Categories: `GET /categories`, `POST /admin/categories`, `POST /admin/categories/{id}/attributes`
- Draft/listings: upload/get/update/confirm draft and buyer listing
- Search: `/buyer/search/qa`
- RFQ/offer flow
- Notifications

## Development note

Current implementation is DB-backed (Postgres + Flyway) with JWT auth baseline.

### Parse pipeline flags

- `APP_PARSE_OCR_OPENAI_ENABLED` (default: `false`)
- `APP_PARSE_OCR_OPENAI_MODEL` (default: `gpt-4o-mini`)
- `APP_PARSE_EXTRACT_OPENAI_ENABLED` (default: `false`)
- `APP_PARSE_EXTRACT_OPENAI_MODEL` (default: `gpt-4o-mini`)

To enable model-based extraction:

```bash
OPENAI_API_KEY=... APP_PARSE_EXTRACT_OPENAI_ENABLED=true ./mvnw spring-boot:run
```
