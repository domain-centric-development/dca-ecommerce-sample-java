# ADR-034: Transaction Boundary — `@Transactional` for Local Use Cases, `TransactionBoundary` When Remote Ports Are Involved

**Date**: 2026-08-30 · **Status**: Accepted

## Context

Use cases were uniformly annotated `@Transactional` (class level). Eleven of them also call output ports that
reach other bounded contexts or external systems — `ArticleDataPort`, `CartDataPort`, `PricingDataPort`,
`PaymentProviderRegistry`. In this monolith those are in-process calls; in the distributed deployment the
architecture is designed for, they are HTTP. A remote call inside `@Transactional` holds the JDBC connection for
the whole round trip (under load: pool exhaustion), and a rollback after a successful remote call cannot undo the
remote effect.

## Decision

- **Local use cases** (repositories, stores, event publishers only) keep class-level `@Transactional`.
- **Use cases with remote-capable ports** drop the annotation and draw the boundary by hand with the
  `TransactionBoundary` from `dca-building-blocks` — an application-layer execution abstraction, deliberately not an
  output port (a transaction is no interaction with the outside world; it defines the execution semantics of several
  such interactions) — implemented in infrastructure by `SpringTransactionBoundary` over Spring's `TransactionTemplate`: remote reads first, then `transactionBoundary.inTransaction(() -> { load; mutate; save; publish; })`.
  Aggregates are (re)loaded inside `run`, so the transaction sees fresh state.
- **Read-only use cases** that call remote ports run without a transaction; the repositories they read from
  manage their own.
- Two rules of the DCA catalog enforce this: `DCA-USE-012` (a use case that saves or deletes an aggregate or publishes domain events is
  `@Transactional` or uses `TransactionBoundary.inTransaction`) and `DCA-USE-013` (a `@Transactional` use case calls no output port
  other than `Repository`, `Store`, `DomainEventPublisher`, `IntegrationEventPublisher`; `TransactionBoundary` is not a
  port — a use case with remote reads uses it instead of the annotation).

## Consequences

- Positive: connections are held for the write, not for the remote call; Spring Modulith's after-commit
  listeners and the event publication registry still work — `TransactionTemplate` is a real transaction.
- Negative: two shapes of use case exist; a reader has to know why one is annotated and the other is not. The
  rules make the choice a compile-time fact rather than a convention.
