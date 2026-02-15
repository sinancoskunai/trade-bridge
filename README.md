# trade-bridge

Monorepo for Trade Bridge B2B mobile marketplace.

## Layout

- `apps/backend`: Spring Boot API
- `apps/mobile`: React Native Expo app
- `apps/admin-web`: React admin/broker panel
- `infra/docker`: local infra and runtime compose files
- `infra/deploy`: deployment and rollback scripts
- `docs`: analysis, standards, principles, roadmap, security, cicd, decisions

## Quick Start

```bash
make bootstrap
make up
make test
```

## Isolation Guarantees

- Compose project name: `tradebridge`
- Container prefix: `tradebridge-*`
- Dedicated network: `tradebridge-net`
- Dedicated volumes: `tradebridge_*`
- Dedicated DB names: `tradebridge_app`, `tradebridge_audit`
