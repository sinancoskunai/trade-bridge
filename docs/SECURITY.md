# SECURITY

## Baseline

- JWT access + refresh tokens
- RBAC with BUYER/SELLER/BROKER/ADMIN
- Input validation on all mutable endpoints
- Audit logs for critical events

## Data Protection

- Sensitive config only via env/secrets
- Signed URL strategy for document access (later phase)
- Tenant-scoped query constraints mandatory
