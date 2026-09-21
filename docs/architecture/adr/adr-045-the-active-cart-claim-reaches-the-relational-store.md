# ADR-045: The Active-Cart Claim Reaches the Relational Store

**Date**: 2026-09-21 · **Status**: Accepted

## Context

ADR-042 moved three uniqueness rules into the store, where a claim holds even when two requests arrive together.
It closed two of them everywhere and left one half open, and said so: *"the active-cart rule is still unguarded in
the relational adapters"*. `accounts.email` and `accounts.linked_user_id` are `UNIQUE` in `schema.sql`, but "at
most one **active** cart per customer" is uniqueness over a subset of the rows, and H2 has no partial index. The
in-memory adapter claimed it with `putIfAbsent`; the JPA and JDBC adapters wrote whatever they were given.

That gap mattered more than the wording suggested. `JpaShoppingCartRepository` is `@Primary` and carries no
profile, so it is the store of every run that is not explicitly `inmemory` — the shop as it is deployed. The
adapter that had the rule was the one only tests use.

WP-22 also gave the failure a type, `ActiveCartAlreadyExistsException`, declared beside the port. A contract that
one implementation honours and two ignore is not a contract.

## Decision

**The database computes the value the index guards, and the adapters translate its refusal.**

- `carts` gains a generated column: `active_customer_id VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN status =
  'ACTIVE' THEN customer_id END)`, with `uq_carts_active_customer` unique over it. A unique index counts NULLs as
  distinct, so a customer keeps any number of finished carts and at most one open one. No trigger, no second
  table, and the same expression is a partial index on a database that has them.
- `JpaShoppingCartRepository` and `JdbcShoppingCartRepository` catch the integrity violation and raise
  `ActiveCartAlreadyExistsException` — the failure the port declares and the in-memory adapter already raised.
- **Only that constraint is translated.** `ActiveCartClaim.wasRefused` looks for the index name in the failure
  chain; anything else keeps travelling as what it is. A foreign-key violation reported as "this customer already
  shops in another cart" would send the caller looking in the wrong place.
- The checks before the save stay, as in ADR-042: they give the ordinary case a clear answer, the claim makes the
  rule true under a race.

## Consequences

- `GetOrCreateActiveCartUseCase` now behaves the same in every profile: the request that loses the race takes the
  cart that won. Before this, that recovery only worked with the in-memory store.
- `JdbcActiveCartUniquenessTest` holds the relational claim directly, including eight simultaneous requests over
  twenty-five rounds, and `ActiveCartClaimIntegrationTest` holds it for the adapter the application context
  actually wires. Together with `ActiveCartUniquenessTest` all three stores are held to one contract.
- The generated column is H2 syntax that PostgreSQL also accepts; a move to a database with partial indexes
  (`TODO #71`) can replace it with `CREATE UNIQUE INDEX ... WHERE status = 'ACTIVE'` and drop the column.
- A translation that matches on the index name is string matching against a database message. It is narrow on
  purpose — the alternative, translating every integrity violation, is what would actually mislead — but it is a
  place to check when the schema is renamed. The name is a constant in `ActiveCartClaim`.
- **The recovery had to move out of the transaction, and that is not a detail.** A relational store that
  refuses a write mid-transaction leaves it unusable: with JPA the failed insert stays in the persistence
  context, so a catch that carried on would read the winner, answer, and then fail at commit with the original
  violation. `GetOrCreateActiveCartUseCase` therefore carries no transactional annotation any more; it claims
  inside `TransactionBoundary.inTransaction` and reads again after that boundary has rolled back.
  `ActiveCartRaceIntegrationTest` drives eight simultaneous callers through the wired store and is what proved
  it — the sequential tests were all green while this was broken.
- **The in-memory adapter stores the cart before it publishes the claim**, and withdraws it when the claim
  fails. A relational store makes both visible at one commit; two separate map writes do not, and the request
  that lost the race could read the winner's id out of the index with nothing behind it yet.
- This closes the open half ADR-042 recorded. ADR-042 itself stands as written.
