# ADR-042: Uniqueness That Spans Aggregates Is Claimed in the Store, Not Checked Before It

**Date**: 2026-09-20 · **Status**: Accepted

## Context

Three rules in this shop are about a *set* of aggregates rather than one: a customer has at most one active
cart, an email address belongs to one account, a SKU names one product. No aggregate can hold such a rule — it
would have to see its siblings — so the use cases asked first and wrote afterwards: `findActiveCartByCustomerId`
then `save`, `existsByEmail` then `save`, `existsBySku` then `save`.

Between those two calls nothing holds. Two requests for the same customer both find no active cart and both
create one; two registrations for the same address both pass the check. The relational adapters are covered for
two of the three — `accounts.email` and `accounts.linked_user_id` are `UNIQUE` in the schema — but the in-memory
adapters keep plain maps that the second writer silently overwrites, and for the active cart no adapter is
covered at all.

The in-memory adapters are not a detail here. They are what a reader copies when starting a context, so a
race that only they have still teaches the wrong shape.

## Decision

**The store claims the value; the use case reacts to the refusal.**

- `InMemoryShoppingCartRepository` keeps an active-cart-per-customer index and claims it in `save` with
  `putIfAbsent`. A second active cart for the same customer is refused with `IllegalStateException` — the answer
  a unique index gives. `findActiveCartByCustomerId` reads the index instead of scanning.
- `InMemoryProductRepository` and `InMemoryAccountRepository` claim the SKU, the email address and the linked
  user id the same way, rather than overwriting their index entries.
- `GetOrCreateActiveCart` catches that refusal and returns the cart that won the race. That is what a caller
  against a relational adapter does with a constraint violation, so the use case reads the same in both.
- The checks before the save stay. They give the caller a clear answer in the ordinary case; the claim is what
  makes the rule true when two callers arrive together.

Same decision, same shape in the .NET sample (its ADR-014).

## Consequences

- Positive: the in-memory adapter teaches the contract a relational one has, instead of a weaker one.
- Positive: `ActiveCartUniquenessTest`, `SkuUniquenessTest` and `AccountUniquenessTest` hold it in both samples,
  each with a concurrency case that runs eight simultaneous requests for twenty-five rounds. Removing the claim
  makes them fail on every run, not occasionally.
- Negative: **the active-cart rule is still unguarded in the relational adapters.** "At most one *active* cart"
  is a partial unique index, which H2 does not offer; expressing it would need a generated column or a trigger.
  The JPA and JDBC profiles therefore keep the race this ADR closes for the in-memory profile. Whoever moves the
  samples onto a database for good (`TODO #71`) inherits that as the open half.
- Negative: a repository that refuses a save is a repository with a rule in it. That is deliberate — the rule is
  the store's, not the aggregate's — but it is one more place a reader must look to know what can fail.
- Neutral: the exception type is the general-purpose one both samples already use for a refused write. A domain
  exception model is a separate open question (WP-22).
