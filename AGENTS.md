# ShopSphere Agent Guide

This document provides execution guidance for agents working in this repository and captures the current production roadmap.

## Repository Context
- Project: **ShopSphere**
- Goal: evolve from current state to a **production-grade** e-commerce backend/service platform.
- Keep implementation aligned to the roadmap below and treat acceptance gates as required checkpoints.

## Working Norms for Agents
1. Prefer secure-by-default changes.
2. Add tests in the same milestone as feature work (do not defer quality).
3. Use quantified acceptance criteria whenever possible.
4. Keep runbooks and operational documentation updated alongside code.
5. Distinguish clearly between launch-blocking work and post-launch hardening.

---

## Updated Final Plan (Production-Grade)

### Week 11 — Security Foundation + Minimum CI Gates (Start Here)
**Objectives**
- Secure-by-default platform posture.
- Prevent unstable code from merging from day 1.

**Build**
- Implement auth module:
  - `POST /auth/login`
  - `POST /auth/refresh` (refresh rotation)
  - `POST /auth/logout` (revocation)
- Enforce authorization:
  - Roles: `USER`, `ADMIN`
  - Protect cart/order/admin endpoints with method- and route-level guards.
- Harden security defaults:
  - Remove global `permitAll`, use deny-by-default.
  - Add login throttling/lockout.
- Stand up minimum CI:
  - Build, lint/static checks, unit tests, artifact build.
  - Branch protection requiring green CI.

**Acceptance Gates (Quantitative)**
- 100% protected endpoints return `401/403` for unauthorized requests.
- 0 auth-critical test failures in CI.
- 100% PRs merged only via CI.

### Week 12 — Config & Secrets Hardening
**Objectives**
- Production-safe configuration and fail-fast startup.

**Build**
- Split profiles: `dev`, `test`, `prod`.
- Externalize secrets (DB creds, JWT keys) to env/secret manager.
- Add startup validation for required secrets/config.
- Align DB dialect/config per environment.
- Publish `.env.example` + config matrix docs.

**Acceptance Gates**
- 0 plaintext secrets in repo.
- App boots successfully in all three profiles.
- Missing required secrets causes clear startup failure.

### Week 13 — Observability Baseline (SLO-Driven)
**Objectives**
- Make system measurable and operable.

**Build**
- Structured JSON logs + correlation IDs.
- Actuator + Prometheus metrics.
- OpenTelemetry tracing + Jaeger.
- Define SLIs/SLOs for checkout/order paths.
- Alert rules + runbooks for critical incidents.

**Acceptance Gates**
- Request can be traced end-to-end (cart → order → inventory).
- Dashboards show p95 latency, error rate, order success/failure.
- Alert-to-runbook mapping exists for every P1/P2 alert.

### Week 14 — Reliable Async Processing (Outbox)
**Objectives**
- No lost events and safe retries.

**Build**
- Outbox schema with status/retry metadata.
- Transactional outbox writes in order flow.
- Publisher with exponential backoff + dead-letter handling.
- Idempotent consumers with processed-event tracking.
- Lag/retry/dead-letter metrics + alerts.

**Acceptance Gates**
- 0 lost events in failure simulation.
- Replays do not duplicate side effects.
- Outbox lag and DLQ thresholds observable and alerting.

### Week 15 — Caching with Consistency Boundaries
**Objectives**
- Improve performance without correctness regressions.

**Build**
- Redis caching for read-heavy catalog/product endpoints first.
- TTL + invalidation/versioning policy.
- Explicitly avoid caching strongly consistent checkout state initially.
- Graceful degradation when Redis unavailable.
- Before/after performance benchmarking.

**Acceptance Gates**
- ≥20–30% p95 latency improvement on targeted read endpoints.
- No critical stale-data incidents in transactional flows.
- Cache hit/miss/error metrics visible in dashboards.

### Week 16 — Advanced CI/CD + Release Safety
**Objectives**
- Safe deployment automation and fast rollback.

**Build**
- Expand pipeline: integration tests, security scan, artifact signing/SBOM.
- Flyway validation/migration checks against disposable DB.
- Staging smoke tests + controlled production rollout.
- Automated rollback triggers based on health/error budgets.
- Immutable container build hardening.

**Acceptance Gates**
- 100% deploys pass quality gates before production.
- Rollback can be triggered and completed within target window (e.g., <15 min).
- No unvalidated DB migration reaches production.

### Week 17 — E2E, Load, Concurrency, and Failure Testing
**Objectives**
- Validate real-world and degraded behavior.

**Build**
- Full E2E journeys (auth → cart → checkout → order confirmation).
- Load tests for critical APIs.
- Concurrency tests (inventory contention, duplicate checkout).
- Failure-mode tests (Redis outage, DB slowdown, delayed publisher).
- Fix bottlenecks (query/index/transaction tuning).

**Acceptance Gates**
- Checkout SLOs achieved under target load.
- Error budget not violated during stress/failure tests.
- No data integrity violations in concurrency scenarios.

### Week 18 — Launch Readiness + Governance
**Objectives**
- Formalize operational readiness and go-live control.

**Build**
- API governance (versioning, contract checks, deprecation policy).
- Backup/restore drill and retention policy.
- Security final pass (OWASP, dependency/vuln remediation, key rotation runbook).
- Incident response + on-call docs finalized.
- Go/No-Go review process.

**Acceptance Gates**
- Successful backup/restore drill within RTO/RPO targets.
- 0 open critical/high launch-blocking vulnerabilities.
- Go-live signoff completed across engineering + ops.

---

## Cross-Cutting Controls (Apply Every Week)

### 1) Ownership & Capacity
- Assign DRI + supporting roles (Backend/DevOps/QA) for each milestone.
- Estimate tasks (S/M/L or story points), track dependencies, cap WIP.

### 2) Quantified Definition of Done
- Every milestone must include numeric pass/fail thresholds (latency, errors, coverage, lag, vuln count).

### 3) Feature-Level Fallback Playbooks
- Auth incident fallback.
- Cache bypass/degraded mode toggle.
- Outbox replay + DLQ triage procedures.

### 4) Shift-Left Testing
- Add integration/contract/perf checks in the same week features are introduced.

---

## MVP Cutline

### Launch-Blocking (Must have before production)
- AuthN/AuthZ complete and tested.
- Secrets/config hardening complete.
- Core observability + alerts + runbooks.
- Outbox reliability + idempotency.
- Minimum CI/CD gates + rollback readiness.
- Critical E2E + load + concurrency pass.
- Backup/restore validated.

### Post-Launch Hardening (Can follow)
- Advanced chaos scenarios.
- Extended optimization and cost tuning.
- Non-critical dashboard/reporting enhancements.
