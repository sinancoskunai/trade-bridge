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

Current implementation uses in-memory stores and token model for phase-0/1 skeleton.
DB-backed persistence and JWT hardening are tracked for next phases.
