# CICD

## Pipelines

1. PR pipeline
- Backend test
- Admin lint+test
- Mobile lint+test
- SAST + dependency scan + secret scan
- Dockerfile lint

2. Main pipeline
- Build images
- Push to GHCR

3. Release pipeline
- Deploy via SSH to VPS
- Smoke test
- Rollback on failure

## Environments

- dev
- staging
- prod
