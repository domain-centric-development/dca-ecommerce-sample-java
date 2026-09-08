# ADR-026: Transactional Outbox for Integration Events

**Date**: June 16, 2026
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐

---

## Context

A bounded context raises **domain events** internally and, as a side effect, sometimes must notify an external system over a message broker. Saving the aggregate and publishing to the broker touch two systems that do not share a transaction. The naive approach publishes from an after-commit listener, which runs **outside any transaction**: if the broker is unreachable, or the process dies between commit and send, the database records the business fact but the outside world never hears about it — no retry, no trace.

This is distinct from **in-process** delivery. A domain event consumed by a synchronous listener is already atomic with the aggregate change and needs nothing extra. A domain event consumed by an asynchronous in-process listener that must not be lost is made durable by the framework's event publication registry (see [ADR-024](adr-024-interface-inversion-spring-modulith.md)) — an *internal* transactional outbox that stores the domain event and never leaves the context. The gap this ADR closes is the **boundary-crossing** case: durable, reliable delivery to an external receiver.

## Decision

**Boundary-crossing events are captured as integration events in a transactional outbox written in the same transaction as the aggregate change, then relayed to the broker out of band with at-least-once delivery.**

The decision has four load-bearing parts:

1. **The outbox stores integration events, never raw domain events.** A `DomainEvent` never crosses the context boundary and may reference domain objects; only the versioned, serializable `IntegrationEvent` is a safe published contract. Persisting every `DomainEvent` would leak the internal model and make the relay own business policy.
2. **An Anti-Corruption Layer translator produces the integration event and writes the outbox row.** The application service never references the outbox or the broker; the `IntegrationEvent` lives in the outbound adapter package, which mechanically keeps the inner layers free of delivery concerns.
3. **The translator runs inside the transaction.** It is a synchronous (or `BEFORE_COMMIT`) in-process listener, never an after-commit one, so the outbox row and the aggregate change commit or roll back together. Immediate delivery is achieved by *also* triggering the relay after commit; durable capture is always in-transaction.
4. **The outbox stores our published language, not the foreign wire payload.** The external message is built at delivery time by the outbound adapter (the ACL to the foreign contract), keeping the outbox transport-neutral — a second transport is a second adapter over the same stored event.

The outbox persistence port is modelled as a `Store` (operational data, no aggregate lifecycle), not a `Repository`.

## Rationale

1. **Atomicity closes the durability hole** — capture in the business transaction means no committed business fact can exist without its outbound event captured.
2. **Layer purity is enforced, not trusted** — placing integration events and the translator in the outbound adapter makes it impossible for the application service to construct outbound messages.
3. **Transport independence** — storing the integration event (not the rendered external payload) lets transports be added or swapped as adapters without reshaping the outbox.
4. **Same pattern, two scopes** — this is the boundary-crossing twin of the internal publication registry ([ADR-024](adr-024-interface-inversion-spring-modulith.md)); both are transactional outboxes, differing only in consumer (broker vs in-process listener) and stored payload (integration vs domain event).

## Consequences

### Positive
✅ **No lost outbound messages** — capture is atomic with the business change
✅ **Transport independence** — adding or swapping a broker is an adapter change; the event contract is stable
✅ **Operational visibility** — pending, failed, and retried deliveries are inspectable rows
✅ **Layer purity** — domain and application layers stay free of transport and messaging concerns

### Negative
❌ **More moving parts** — table, store, translator, relay, cleanup — justified only for effects that genuinely must not be lost; best-effort effects stay on plain async publishing
❌ **At-least-once** — consumers must deduplicate; the outbox row id (the integration event id) is the idempotency key
❌ **Payload at rest** — PII in the stored integration event lives outside column-level encryption; bound exposure with prompt cleanup or encrypt the payload

## Implementation

- Outbox row carries the serialized **integration event** plus a status (`PENDING`/`PROCESSED`/`RETRY`/`FAILED`) and a `retryAfter`; the row id is the integration event id.
- Translator: a synchronous in-process listener on the domain event that maps to the integration event and saves the outbox row in the publishing transaction.
- Relay: claims pending rows with a concurrency-safe lock (e.g. `SELECT ... FOR UPDATE SKIP LOCKED`), sends via a transport-agnostic outbound port, marks processed; failures retry with backoff, then terminal `FAILED`. Triggered by a scheduled poll (safety net) and an after-commit hook (fast path). Stuck in-flight rows are reclaimed after a timeout; processed rows are cleaned up after a retention window.
- **Framework option**: Spring Modulith's event publication registry with event externalization is a transactional outbox with a payload-mapping hook and is the recommended default when its constraints fit; the hand-rolled table is warranted only for full control over payload shape and retry policy. Either way, what crosses the boundary is the integration event.
- **Status in this sample**: the internal publication registry is implemented (read-only view in the `backoffice` context); the external broker outbox described here is the documented target pattern, not yet built in the sample. See [Complete Cross-Context Event Flow](https://github.com/domain-centric-development/dca-guide/blob/main/README.md#complete-cross-context-event-flow) in the guide.

## References

- Chris Richardson, *Microservices Patterns* — Transactional Outbox, Polling Publisher, Transaction Log Tailing
- Vaughn Vernon, *Implementing Domain-Driven Design* — domain vs integration events, autonomy via messaging
- [Complete Cross-Context Event Flow](https://github.com/domain-centric-development/dca-guide/blob/main/README.md#complete-cross-context-event-flow) in the guide — domain event to integration event across contexts

### Related ADRs

- [ADR-005: Domain Events Publishing Strategy](adr-005-domain-events-publishing.md)
- [ADR-024: Interface Inversion Pattern for Spring Modulith Event Listeners](adr-024-interface-inversion-spring-modulith.md)
- [ADR-016: Shared Kernel Pattern for Cross-Context Value Objects](adr-016-shared-kernel-pattern.md)
