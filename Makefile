ROOT := $(shell pwd)

.PHONY: bootstrap up down logs test lint backend-test mobile-test admin-test

bootstrap:
	@echo "[bootstrap] backend deps"
	cd apps/backend && ./mvnw -q -DskipTests dependency:go-offline
	@echo "[bootstrap] admin deps"
	cd apps/admin-web && npm install
	@echo "[bootstrap] mobile deps"
	cd apps/mobile && npm install

up:
	docker compose -f infra/docker/compose.local.yml --project-name tradebridge up -d

down:
	docker compose -f infra/docker/compose.local.yml --project-name tradebridge down

logs:
	docker compose -f infra/docker/compose.local.yml --project-name tradebridge logs -f --tail=200

backend-test:
	cd apps/backend && ./mvnw test

mobile-test:
	cd apps/mobile && npm run test

admin-test:
	cd apps/admin-web && npm run test

test: backend-test mobile-test admin-test

lint:
	cd apps/backend && ./mvnw -q -DskipTests compile
	cd apps/mobile && npm run lint
	cd apps/admin-web && npm run lint
