# Architecture Decision Records (ADRs)

Architecture Decision Records capture important architectural decisions with context and consequences.

## ADR Index

### Accepted ✅

| ADR | Title | Status |
|-----|-------|--------|
| [ADR-001](adr-001-api-web-package-separation.md) | Separate REST API and Web MVC Controllers into Different Packages | ✅ Accepted |
| [ADR-002](adr-002-framework-independent-domain.md) | Framework-Independent Domain Layer | ✅ Accepted |
| [ADR-003](adr-003-aggregate-reference-by-id.md) | Aggregate Reference by Identity Only | ✅ Accepted |
| [ADR-004](adr-004-persistence-oriented-repository.md) | Persistence-Oriented Repository Pattern | ✅ Accepted |
| [ADR-005](adr-005-domain-events-publishing.md) | Domain Events Publishing Strategy | ✅ Accepted |
| [ADR-006](adr-006-domain-events-immutable-records.md) | Domain Events as Immutable Records | ✅ Accepted |
| [ADR-007](adr-007-hexagonal-architecture.md) | Hexagonal Architecture with Explicit Port/Adapter Separation | ✅ Accepted |
| [ADR-008](adr-008-repository-interfaces-as-output-ports.md) | Repository Interfaces as Output Ports in Application Layer | ✅ Accepted |
| [ADR-009](adr-009-value-objects-as-records.md) | Value Objects as Java Records | ✅ Accepted |
| [ADR-010](adr-010-domain-services-multi-aggregate.md) | Domain Services Only for Multi-Aggregate Operations | ✅ Accepted |
| [ADR-011](adr-011-bounded-context-isolation.md) | Bounded Context Isolation via Package Structure | ✅ Accepted |
| [ADR-013](adr-013-specification-pattern.md) | Specification Pattern for Business Rules | ✅ Accepted |
| [ADR-014](adr-014-factory-pattern.md) | Factory Pattern for Complex Aggregate Creation | ✅ Accepted |
| [ADR-015](adr-015-archunit-governance.md) | ArchUnit for Architecture Governance | ✅ Accepted |
| [ADR-016](adr-016-shared-kernel-pattern.md) | Shared Kernel Pattern for Cross-Context Value Objects | ✅ Accepted |
| [ADR-017](adr-017-e2e-data-test-attributes.md) | Data-Test Attributes for E2E Test Selectors | ✅ Accepted |
| [ADR-018](adr-018-page-object-pattern-e2e.md) | Page Object Pattern for E2E Tests | ✅ Accepted |
| [ADR-019](adr-019-open-host-service-pattern.md) | Open Host Service Pattern for Cross-Context Communication | ✅ Accepted |
| [ADR-020](adr-020-use-case-result-naming.md) | Use Case Output Naming Convention (*Result instead of *Response) | ✅ Accepted |
| [ADR-021](adr-021-enriched-domain-model-pattern.md) | Enriched Domain Model Pattern | ✅ Accepted |
| [ADR-022](adr-022-viewmodel-pattern.md) | ViewModel Pattern for Web Adapters | ✅ Accepted |
| [ADR-023](adr-023-optional-results-not-found.md) | Optional Results for Not-Found Cases | ✅ Accepted |
| [ADR-024](adr-024-interface-inversion-spring-modulith.md) | Interface Inversion Pattern for Spring Modulith Event Listeners | ✅ Accepted |
| [ADR-025](adr-025-pattern-selection-per-subdomain.md) | Pattern Selection per Subdomain Type | ✅ Accepted |
| [ADR-026](adr-026-transactional-outbox-integration-events.md) | Transactional Outbox for Integration Events | ✅ Accepted |
| [ADR-027](adr-027-integration-event-contract-identity.md) | Integration-Event Contract Identity via @IntegrationEventType | ✅ Accepted |
| [ADR-028](adr-028-immutable-owner-name.md) | The Account Owner's Name Is Immutable by Type, Not by Rule | ✅ Accepted |
| [ADR-029](adr-029-expiry-is-not-logout.md) | Session Expiry Ends the Session, Not the Identity | ✅ Accepted |
| [ADR-030](adr-030-three-cookie-session-design.md) | Separate Cookies for Identity, Session and Renewal | ✅ Accepted (partially implemented) |
| [ADR-031](adr-031-persistence-adapters-as-the-default.md) | A Repository Hands Out Copies — Real Persistence Is the Default | ✅ Accepted (account and cart converted) |
| [ADR-032](adr-032-executable-context-map.md) | The Context Map Is Declared in Code and Enforced | ✅ Accepted |
| [ADR-033](adr-033-adopt-dca-java-libraries.md) | Adopt the dca-java Libraries for Markers and Architecture Rules | ✅ Accepted |
| [ADR-034](adr-034-transaction-boundary-and-remote-ports.md) | Transaction boundary — `@Transactional` for local use cases, `TransactionBoundary` when remote ports are involved | ✅ Accepted |
| [ADR-035](adr-035-csrf-protection-and-bearer-only-api.md) | CSRF Protection for Web Forms, Bearer-Only Authentication for the API | ✅ Accepted |
| [ADR-036](adr-036-api-authorization-at-the-adapter.md) | A guard goes where its inputs are — and `authenticated()` is not one | ✅ Accepted |
| [ADR-037](adr-037-adopt-dca-spring-and-dca-archunit-spring-modulith.md) | Adopt dca-spring and dca-archunit-spring-modulith | ✅ Accepted |

| [ADR-038](adr-038-product-created-payload.md) | Product-created notification payload | Accepted |

| [ADR-039](adr-039-aggregate-owned-event-registration.md) | Aggregate-owned event registration | Accepted |

| [ADR-040](adr-040-checkout-snapshots-and-reconciliation.md) | Checkout snapshots and reconciliation | Accepted |

### Proposed 🟡

| ADR | Title | Status |
|-----|-------|--------|
| [ADR-012](adr-012-use-case-input-output-models.md) | Use Case Input/Output Models (Command/Query Pattern) | 🟡 Proposed |

---

## ADR Format

Each ADR includes:
- **Context**: Issue motivating the decision
- **Decision**: The decision made
- **Rationale**: Why this decision
- **Consequences**: Outcomes (positive, neutral, negative)
- **Alternatives Considered**: Other options evaluated
- **Implementation**: How implemented
- **References**: Related patterns and resources

## Creating a New ADR

1. Name it `adr-XXX-short-title.md` (next available number)
2. Fill in all sections
3. Update this README index
4. Get reviewed by Architecture Team

## References

- [Architecture Decision Records](https://adr.github.io/)
- [Documenting Architecture Decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
