# ADR-031: A Repository Hands Out Copies — Real Persistence Is the Default

**Date**: August 13, 2026
**Status**: ✅ Accepted — *Scope* and the `inmemory` profile superseded by [ADR-048](adr-048-postgresql-for-every-store.md) (2026-11-09)
**Deciders**: Architecture Team

---

## Context

Most bounded contexts stored their aggregates in a `ConcurrentHashMap` and returned the stored
instance:

```java
public Optional<Account> findById(final AccountId id) {
  return Optional.ofNullable(accounts.get(id));   // the very instance the map holds
}
```

A caller therefore mutates the store by mutating the aggregate. `save` becomes decoration — the
change is already visible to the next reader without it. Against any real database the same code
loses the change silently, because loading a row constructs a *new* object.

This is not a hypothetical: [ADR-005](adr-005-domain-events-publishing.md) requires
`publishAndClearEvents` after `save`, and eight use cases were found saving without publishing.
The forgotten-`save` failure mode is the same class of bug and the in-memory store cannot detect it.

The reference implementation also under-demonstrated the boundary it exists to teach. `cart` had
JPA and JDBC adapters; five contexts had neither, so a reader could not see what mapping an
aggregate to rows actually costs.

---

## Decision

**A repository hands out copies. An adapter that cannot honour that is not the default.**

Two rules follow:

1. **Real persistence is the default adapter.** `account` persists via JDBC
   (`JdbcAccountRepository`, `@Profile("!inmemory")`), `cart` via JPA. Loading maps rows back
   through the aggregate's `reconstitute` factory, so every read is a fresh instance.

2. **An in-memory adapter behaves as though a database were behind it.** It is kept — running the
   sample without a database is worth having — but it copies on write and on read
   (`InMemoryAccountRepository.copyOf`, a round trip through `reconstitute`). Registered-but-
   unpublished domain events are not carried over: a stored aggregate is a fact, and re-reading it
   must not replay what the writer already published.

Both adapters run the same `AccountRepositoryContractTest`. The contract is the port's, not an
implementation's — an adapter that cannot pass it does not implement the port.

### Uniqueness lives in the schema *and* in the use case

`accounts.email` and `accounts.linked_user_id` are `UNIQUE`. The use cases already guard both
(`existsByEmail` in `RegisterAccountUseCase` and `ChangeProfileUseCase`,
`findByLinkedUserId` in `RegisterAccountUseCase`), so the constraint is the backstop against a
race, not the primary check. Dropping the guard in favour of the constraint would move a domain
rule into the schema and surface its violation as a `DataAccessException` in the adapter layer.

### Scope

**Converted:** `account` (JDBC, default), `cart` (JPA default, JDBC and in-memory alternatives).

The `inmemory` profile is **partial**: it swaps the account adapter, but `JpaShoppingCartRepository`
is `@Primary` without a profile, so the cart persists via JPA regardless and the application still
needs its datasource. Fixing that means changing cart's bean selection, which this ADR does not.

**Not converted:** `product`, `checkout`, `inventory`, `pricing` — still in-memory only, and still
handing out live references. They are staged behind `checkout` (mutating, multi-step, so the
highest risk) rather than done in one sweep: each conversion may expose a forgotten `save`, and
those are worth seeing one context at a time.

**Deliberately still `jdbc:h2:mem:`, not `file:`.** File-mode H2 would make accounts and carts
survive a restart while `product`, `inventory` and `pricing` are reseeded by
`SampleDataInitializer` on every boot — a persisted `cart_items` row would then reference a product
id that no longer exists. Restart durability is a separate decision and needs all contexts
converted first.

---

## Consequences

### Positive

✅ **A forgotten `save` fails** instead of silently working
✅ **The mapping boundary is visible** — a reader sees what an aggregate costs in rows
✅ **The port has a contract test** that every adapter must pass
✅ **Two persistence styles are demonstrated** — JPA (`cart`, with child entities) and JDBC
(`account`, without)

### Neutral

⚠️ **Two adapters per converted context** to keep in step — the contract test is what keeps them honest
⚠️ **`schema.sql` is hand-written** (`ddl-auto: none`), so a new field is a schema change, not a
side effect of a field declaration. Deliberate: the same discipline a migration tool would impose.

### Negative

❌ **Copying costs** on every read of the in-memory adapter — irrelevant at sample scale, and the
alternative is an adapter that lies
❌ **Nothing survives a restart yet** — accounts are still lost on restart, see *Scope*
❌ **Four contexts still hand out live references** and could still hide a forgotten `save`

---

## Alternatives Considered

### Alternative 1: Keep in-memory everywhere, copy at the boundary

Would fix the copy semantics without any database. **Rejected as insufficient**: it leaves the
mapping boundary undemonstrated, and a reference implementation for Hexagonal Architecture that
never maps an aggregate to a row is not showing the interesting half of the port.

### Alternative 2: Convert every context at once

**Rejected.** Each conversion can turn a passing test red by exposing a forgotten `save`. Five at
once produces a pile of failures with no way to attribute them.

### Alternative 3: `ddl-auto: update` instead of a hand-written schema

**Rejected.** It would put the schema under the domain model's control, which is exactly the
coupling `reconstitute` and the explicit mapper exist to avoid — and it hides the cost this ADR
wants visible.

### Alternative 4: Make the aggregate immutable so copying is unnecessary

Would remove the problem at the root. **Out of scope here** — it is a much larger change to the
tactical pattern set, and it interacts with the open question in
[ADR-028](adr-028-immutable-owner-name.md) about how persistence reaches into an aggregate.

---

## Related ADRs

- [ADR-004: Persistence-Oriented Repository Pattern](adr-004-persistence-oriented-repository.md) —
  the collection illusion this ADR bounds: a collection hands out references, a repository does not
- [ADR-005: Domain Events Publishing Strategy](adr-005-domain-events-publishing.md) — the
  save-then-publish rule that shares this failure mode
- [ADR-008: Repository Interfaces as Output Ports](adr-008-repository-interfaces-as-output-ports.md)
- [ADR-028: The Account Owner's Name Is Immutable by Type](adr-028-immutable-owner-name.md) — the
  `reconstitute` visibility question this decision leans on

---

## Validation

- [x] Both account adapters pass `AccountRepositoryContractTest`
- [x] A mutation that is never saved is invisible to the next reader, in both adapters
- [x] An account that has never logged in round-trips with a null `lastLoginAt`
- [ ] `checkout`, `product`, `inventory` and `pricing` converted

---

**Accepted by**: Architecture Team
**Date**: August 13, 2026
**Version**: 1.0
