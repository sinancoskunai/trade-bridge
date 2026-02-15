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
- [ ] A6. OpenAI structured extractor (category aware)
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

### Sprint 4 (Week 4)

- C3 security/quality gates in CI
- C4 staging deploy flow
- C5 runbook + smoke + rollback rehearsal

## Exit Criteria for MVP

- Seller uploads document and publishes through wizard without manual SQL/data edits.
- Buyer can filter and create RFQ from mobile.
- Broker can intervene and all key actions are observable.
- CI blocks insecure or untested regressions on `main`.
