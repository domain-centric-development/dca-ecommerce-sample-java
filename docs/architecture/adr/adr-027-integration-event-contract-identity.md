# ADR-027: Integration-Event Contract Identity via @IntegrationEventType

**Date**: July 3, 2026
**Status**: ✅ Accepted — amended 2026-09-16 (see the amendment at the end)
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐

---

## Context

Integration events are published contracts: remote consumers deserialize them long after the publishing code has moved on, so every event needs a stable identity and a schema version for backward-compatible evolution.

The original contract expressed this as data: `IntegrationEvent` declared an `int version()` method, forcing every event instance to carry the version as a payload field. That couples three things that should be independent:

- **The version is a property of the event *class*, not of an instance.** Every instance of `CartCheckedOutEvent` has the same schema version; carrying it per instance is redundant payload that can silently drift from the intended contract (a constructor passing the wrong constant).
- **The contract identity was implicitly the Java class name.** A breaking schema change could not ship as a new class without changing the logical event name consumers subscribe to.
- **Consumers need `(name, version)` from the message alone** — an outbox relay or broker consumer must pick its translator without loading the publisher's classes.

With the transactional outbox (ADR-026) the events also need a publishing port that use cases can call — and the outbox's own persistence interface must not be confused with it.

## Decision

**An integration event's contract identity — stable logical name plus schema version — is declared as a class property via the `@IntegrationEventType` annotation, never as instance data. Use cases publish through the new `IntegrationEventPublisher` output port. The outbox store deliberately carries no marker.**

The decision has four load-bearing parts:

1. **`IntegrationEvent` declares only `eventId()` and `occurredOn()`.** The `version()` method is removed; events carry no `version` data field.
2. **`@IntegrationEventType(name, version)` is the single source of truth** for the contract identity. The `name` is a stable logical type name decoupled from the Java class name, so a breaking change ships as a new V2 class keeping the old `name`. A serializer keys `(name, version)` to the class and stamps both onto the wire envelope; a remote consumer picks its translator from the message alone.
3. **`IntegrationEventPublisher` is an application output port** (`extends OutputPort`, in `marker/port/out`): use cases publish boundary-crossing facts through it, delivery mechanics stay in the adapter.
4. **The outbox store is intentionally NOT a marker port.** It is an internal port of the outbox adapter subsystem: no use case depends on it, only the relay/publisher adapters do, and its implementation lives alongside it. Marker interfaces designate an architectural role — "application depends on this" — not merely "is an interface".

## Rationale

- **Class property beats data field**: one declaration per class instead of one value per instance; impossible to construct an event with a wrong version; no payload bytes wasted on a constant.
- **Two-level port distinction**: `IntegrationEventPublisher` (marker — application-facing) vs. outbox store (unmarked — adapter-internal wiring) keeps the marker vocabulary meaningful. If everything adapter-internal were marked `OutputPort`, the marker would stop expressing "the application needs this".
- **Enforced mechanically**: ArchUnit rules require every concrete `IntegrationEvent` to be annotated with `@IntegrationEventType` and forbid `version` data fields on them.

## Consequences

**Positive:**

- Schema version cannot drift between instances of the same event class.
- Breaking changes have a defined shape: new class, same logical `name`, bumped `version`.
- Wire-level routing (`(name, version)` → translator) works without the publisher's classes.

**Negative:**

- Reading the version requires reflection on the annotation instead of a getter — acceptable, since only serializers/relays need it.
- Existing events required a one-time migration (drop the `version` component, add the annotation).

## Related Decisions

- [ADR-026: Transactional Outbox for Integration Events](adr-026-transactional-outbox-integration-events.md)
- [ADR-005: Domain Events Publishing Strategy](adr-005-domain-events-publishing.md)
- [ADR-006: Domain Events as Immutable Records](adr-006-domain-events-immutable-records.md) — its illustrative interface still shows an `int version()` method. That part is superseded here; the immutability and naming decisions remain in force.

## 2026-09-16 amendment: the example event

`CartCheckedOutEvent`, used above to illustrate the per-class version, no longer exists; the Cart context
publishes no checkout event. Read the examples against `CheckoutConfirmedEvent`
(`@IntegrationEventType(name = "checkout-confirmed", version = 2)`). The decision is unchanged.
