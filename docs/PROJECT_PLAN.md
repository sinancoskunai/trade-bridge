# PROJECT PLAN

## Current Snapshot (February 16, 2026)

- Active phase: `Phase 3 - AI Parse + Wizard`
- Completed phases: `Phase 0`, `Phase 1`, major baseline of `Phase 2`
- In-progress capabilities: persisted upload + async parse jobs + admin requeue

## Step-by-Step Execution Board

### Track A - Parse Productization

- [x] A1. Draft/document/parse-job DB schema
- [x] A2. Seller upload persistence
- [x] A3. Async parse orchestration
- [x] A4. Admin parse jobs list + requeue API
- [x] A5. OCR adapter baseline (`pdfbox` + optional OpenAI vision)
- [x] A6. OpenAI structured extractor baseline (category aware, feature-flagged)
- [x] A6.1 Service boundary refactor (repository access only via services + architecture guard test)
- [ ] A7. Confidence policies and blocking rules
- [ ] A8. Seller wizard UI and manual correction flow
- [ ] A9. Parse telemetry + error analytics

### Track B - RFQ and Broker Hardening

- [x] B1. RFQ create/offer/counter/accept/reject baseline
- [x] B2. Broker intervention baseline
- [ ] B3. RFQ + Offer persistence model
- [ ] B4. State-machine validation and test suite
- [ ] B5. Broker operations dashboard

### Track C - Delivery and Ops

- [x] C1. Docker-isolated local stack
- [x] C2. Monorepo quality scripts
- [ ] C3. Full PR quality gates (SAST/dependency/secret scan)
- [ ] C4. Staging deployment pipeline
- [ ] C5. Production runbook + rollback drills

### Track D - Localization (i18n)

- [ ] D1. Backend localization foundation (`Accept-Language`, message bundles)
- [ ] D2. Mobile translation dictionaries (`tr`, `en`, `zh`)
- [ ] D3. Admin-web translation dictionaries (`tr`, `en`, `zh`)
- [ ] D4. Language selector and persistence (per user/client)
- [ ] D5. Category/attribute multilingual labels in API + DB
- [ ] D6. Localization QA checklist and fallback tests

## Suggested Calendar (MVP)

### Sprint 1 (Week 1)

- A5 OCR adapter
- A6 OpenAI extractor
- A7 confidence policy enforcement

### Sprint 2 (Week 2)

- A8 seller wizard UI
- A9 parse metrics and admin quality view

### Sprint 3 (Week 3)

- B3 RFQ persistence
- B4 state-machine and test hardening
- D1 backend localization foundation
- D2 mobile translations (TR/EN)

### Sprint 4 (Week 4)

- C3 security/quality gates in CI
- C4 staging deploy flow
- C5 runbook + smoke + rollback rehearsal
- D3 admin-web translations (TR/EN)
- D4 language selector and persistence
- D5 multilingual category labels
- D6 localization QA and zh-CN rollout

## Exit Criteria for MVP

- Seller uploads document and publishes through wizard without manual SQL/data edits.
- Buyer can filter and create RFQ from mobile.
- Broker can intervene and all key actions are observable.
- CI blocks insecure or untested regressions on `main`.
