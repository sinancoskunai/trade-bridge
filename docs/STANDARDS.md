# STANDARDS

## General

- Keep modules small and explicit.
- Prefer deterministic APIs over implicit behavior.
- Validate all external inputs.

## Backend

- Layering: controller -> service -> repository.
- Repository access is allowed only inside `@Service` classes.
- Orchestration classes (runner/parser/bootstrap/controller) must call services, not repositories.
- Packaging: package-by-feature with explicit subpackages.
- Backend feature template:
  - `<feature>/controller`
  - `<feature>/service`
  - `<feature>/service/impl`
  - `<feature>/persistence/entity`
  - `<feature>/persistence/repository`
  - `<feature>/model`
- DTOs at API boundary; entities are internal.
- RBAC checks at endpoint and service level for critical flows.

## Frontend

- Feature-first folder organization.
- Keep view and data access separated.
- All API calls via typed client wrappers.

## Git & PR

- Trunk-based, short-lived branches.
- Squash merge unless release branch policy says otherwise.
- PR must pass strict quality gates.
