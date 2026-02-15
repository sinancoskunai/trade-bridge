# ARCHITECTURE

## Stack

- Backend: Java 21 + Spring Boot 3.5
- Mobile: React Native Expo
- Admin: React + Vite
- DB: PostgreSQL
- Cache/Queue: Redis
- Object Storage: MinIO (S3-compatible)
- AI: OpenAI API

## Topology

- Monorepo
- API service (`apps/backend`)
- Background workers will run in backend process initially; split later if needed.
- Admin and mobile are separate clients over HTTP API.

## Isolation

All local runtime resources use `tradebridge` naming and dedicated network/volumes.
