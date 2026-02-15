# TRADE BRIDGE REVIEW SKILL SPEC

## Purpose

Evaluate code changes against Trade Bridge standards, principles, tenant safety, and phase alignment.

## Inputs

- Diff or changed files
- `docs/STANDARDS.md`
- `docs/PRINCIPLES.md`
- `docs/MISSED_PRINCIPLES.md`
- Phase scope from `docs/ROADMAP.md`

## Output Sections

1. Findings by severity
2. Violated standards/principles
3. Security and tenant-isolation risks
4. Missing tests
5. Suggested remediation

## Enforcement Rules

- Flag cross-tenant data risks as high severity.
- Flag undocumented API changes as medium+.
- Require updates to docs when contract changes.
