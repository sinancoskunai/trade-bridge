# ANALYSIS

## Problem Statement

Trade Bridge connects buyers and sellers through category-based product discovery, AI-assisted document ingestion, RFQ negotiation, and broker/admin supervision.

## Users

- Buyer
- Seller
- Broker
- Admin

## Core Workflows

1. Company onboarding and admin approval.
2. Seller uploads documents (pdf/image/doc) and confirms parsed draft in wizard.
3. Buyer searches listings with filters and AI question-answer flow.
4. Buyer opens RFQ, seller responds with offer, buyer accepts/rejects/counters.
5. Broker can intervene when needed.

## Non-Goals (MVP)

- Payments/escrow
- Real-time chat
- Offline mobile mode

## Success Metrics

- Time from seller upload to publish
- Buyer search-to-RFQ conversion
- RFQ-to-agreement conversion
- Parse confidence and correction rate
- Localization coverage (TR/EN/ZH) on critical journeys

## Phase Tracking (as of February 16, 2026)

### Overall Level

- Product maturity: `MVP Foundation+`
- Current phase focus: `Phase 3 (AI Parse + Wizard) started`
- Delivery confidence: `Medium`

### Progress Checklist

#### Phase 0 - Foundation

- [x] Monorepo skeleton
- [x] Base docs and standards files
- [x] Local Docker infra (Postgres/Redis/MinIO)
- [x] Initial CI/CD skeleton

#### Phase 1 - Auth & Tenant

- [x] Company register + admin approval
- [x] JWT access + refresh
- [x] RBAC baseline
- [x] Audit baseline

#### Phase 2 - Category & Product Core

- [x] Category persistence (Flyway + DB-backed service)
- [x] Admin category create + attribute create
- [x] Buyer category/product listing baseline
- [x] Mobile/admin web API connectivity

#### Phase 3 - AI Parse + Wizard

- [x] Seller upload endpoint persisted to DB (`tb_product_draft`, `tb_document`)
- [x] Async parse job pipeline (`tb_parse_job`, enqueue + runner)
- [x] AI parser stub with confidence scoring (`StubAiDocumentParser`)
- [x] OCR adapter baseline (`PDFBox` for PDF text, optional OpenAI Vision for images)
- [x] Draft status lifecycle (`PENDING_PARSE -> PARSING -> REVIEW_REQUIRED/READY -> REVIEWED -> CONFIRMED`)
- [x] Admin parse job ops (`GET /admin/parse-jobs`, `POST /admin/parse-jobs/{id}/requeue`)
- [ ] Real OpenAI structured extraction
- [ ] Seller wizard UI for low-confidence field resolution
- [ ] Admin parse quality dashboard (error buckets, confidence trend)

#### Phase 4 - AI Q&A + RFQ/Offer

- [x] Buyer Q&A endpoint baseline
- [x] RFQ/offer state flow baseline
- [x] Broker intervention endpoint + admin-web integration
- [ ] FilterDSL explainability hardening
- [ ] Persistence for RFQ/offer states

#### Phase 5 - Hardening

- [ ] Security scans enforced in pipeline
- [ ] Rate limit and abuse controls
- [ ] Performance baseline tests
- [ ] Production observability/alerts

## Language Support Plan

### Product Locales

- Primary: `tr-TR`
- Secondary: `en-US`
- Planned: `zh-CN`

### Localization Checklist

- [ ] Backend message localization (`Accept-Language` + message bundles)
- [ ] Mobile i18n layer (`tr`, `en`, `zh`) and runtime language switch
- [ ] Admin-web i18n layer (`tr`, `en`, `zh`) and runtime language switch
- [ ] Category and attribute display names in multilingual format
- [ ] Seed and fallback strategy (`tr` default, `en` fallback)
