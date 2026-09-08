# ADR-025: Pattern Selection per Subdomain Type

**Date**: June 10, 2026
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐

---

## Context

DCA describes a full tactical pattern set (rich domain model, aggregates, domain events, ports & adapters). Applying it uniformly to every bounded context over-engineers simple contexts: a CRUD-style supporting context gains nothing from aggregate design and event publishing, but pays the full complexity cost.

Established DDD guidance (Vernon, Khononov, Millett/Tune) agrees: tactical DDD belongs where complexity warrants it. Khononov's heuristic maps subdomain types to patterns; Millett/Tune state explicitly that CRUD is not an anti-pattern for supporting subdomains.

## Decision

**Each bounded context declares its pattern style, chosen by subdomain type. The ArchUnit suite activates the matching rule subset per context.**

| Subdomain | Business Logic Pattern | Architecture | ArchUnit Rule Set |
|---|---|---|---|
| **Core** | Rich domain model | Ports & Adapters, optionally CQRS/ES | Full tactical + structural rules |
| **Supporting** | Transaction script / active record | Simple layering | Structural baseline only (layer dependencies, no cycles, context isolation) |
| **Generic** | Buy / adopt | Integrate via ACL | Boundary rules only |

### Classification of This Sample's Contexts

All eight contexts in this sample use the rich domain-model style — deliberately, because the sample's purpose is to *demonstrate* the full pattern set. In a real system, contexts like `backoffice` or `portal` would be supporting-subdomain candidates for a simpler style.

| Context | Subdomain Type (see [context map](../../context-map.md)) | Pattern Style |
|---|---|---|
| product, cart, checkout | Core | Domain model (full rule set) |
| pricing, inventory, account | Supporting | Domain model here (didactic); transaction script legitimate in production |
| portal, backoffice | Generic (UI/Ops) | Domain model here (didactic); thin contexts in production — one would buy an operator console rather than build it |

## Rationale

1. **Complexity must pay for itself** — aggregate design, event hygiene, and port indirection are investments justified by core-domain complexity, not defaults.
2. **Progressive Complexity at the strategic level** — the existing principle (start simple, add structure when needed) applies to pattern choice per context, not just package depth.
3. **Honest governance** — forcing full tactical rules on a transaction-script context produces fake aggregates (anemic wrappers) just to satisfy tests.

## Consequences

### Positive
✅ Simple contexts stay simple; effort concentrates on the core domain
✅ Architecture tests check what each context actually promises
✅ Reclassification (supporting → core) has a defined upgrade path: change the ADR, activate the full rule set, refactor

### Negative
❌ Two pattern styles in one codebase — mitigated by per-context consistency and the declaring ADR

## Implementation

- Pattern style is declared per context in an ADR (this one covers the sample's contexts)
- ArchUnit: scope tactical rule sets to declared domain-model contexts, e.g. a `DOMAIN_MODEL_CONTEXTS` list in `BaseArchUnitTest` used by tactical tests; structural rules (cycles, isolation, layer dependencies) always run for all contexts
- See [ArchUnit Governance](https://github.com/domain-centric-development/dca-guide/blob/main/archunit-governance.md) for context-specific rule sets

## References

- Vlad Khononov, *Learning Domain-Driven Design* — subdomain/pattern decision heuristics
- Scott Millett & Nick Tune, *Patterns, Principles, and Practices of DDD* — "CRUD is not an anti-pattern"
- Vaughn Vernon, *Implementing Domain-Driven Design* — architecture chosen against real risk

### Related ADRs

- [ADR-011: Bounded Context Isolation](adr-011-bounded-context-isolation.md)
- [ADR-015: ArchUnit for Architecture Governance](adr-015-archunit-governance.md)
