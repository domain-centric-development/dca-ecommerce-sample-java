# ADR-023: Optional Results for Not-Found Cases

**Date**: February 7, 2026
**Status**: Superseded by [ADR-047](adr-047-expected-refusals-are-sealed-use-case-results.md) (2026-11-07)
**Deciders**: Architecture Team

> **Superseded.** "Not found" is no longer an empty `Optional` in a result record: it is a refusal case of a
> closed result (`GetCartByIdResult.CartNotFound`, `GetProductByIdResult.ProductNotFound`, …), marked `Refusal`,
> beside the `Found` case. The reasoning below — not-found is not exceptional — still holds and is what ADR-047
> builds on. Kept as the record of how the shop got there.

---

## Context

Several use cases query for a single entity by ID (e.g., `GetCartByIdUseCase`, `GetProductByIdUseCase`). The entity may not exist, and the application layer needs a clean way to communicate this to adapters without throwing exceptions for expected business cases.

### The Problem

Throwing exceptions for "not found" cases has drawbacks:

1. **Not-found is not exceptional** -- it's a normal business case (e.g., user enters wrong URL)
2. **Exception-driven control flow** is harder to reason about
3. **Adapter must catch exceptions** and translate to HTTP 404, which is error-prone

---

## Decision

**Use `Optional<T>` in Result records for use cases where the queried entity may not exist.**

### Implementation

```java
// Application layer: Result wraps Optional
public record GetCartByIdResult(Optional<EnrichedCart> cart) {
  public static GetCartByIdResult of(EnrichedCart cart) {
    return new GetCartByIdResult(Optional.ofNullable(cart));
  }
  public static GetCartByIdResult empty() {
    return new GetCartByIdResult(Optional.empty());
  }
}

// Use case returns Result with Optional
public GetCartByIdResult execute(GetCartByIdQuery query) {
  return cartRepository.findById(query.cartId())
      .map(cart -> GetCartByIdResult.of(enrichCart(cart)))
      .orElse(GetCartByIdResult.empty());
}

// Adapter handles presence/absence cleanly
GetCartByIdResult result = useCase.execute(query);
if (result.cart().isEmpty()) {
  return ResponseEntity.notFound().build();
}
```

### When to Use

- `GetXxxByIdUseCase` -- single entity lookup by ID
- Any query where the entity may legitimately not exist

### When NOT to Use

- Commands that require the entity to exist (throw domain exception instead)
- Collection queries (return empty list, not Optional)

---

## Consequences

### Positive

- Not-found is handled as a normal return value, not an exception
- Adapters can map `Optional.empty()` to HTTP 404 or a "not found" page cleanly
- Type-safe: compiler enforces handling of the empty case

### Negative

- Result records are slightly more complex with Optional wrapping
- Callers must unwrap Optional before using the value

---

## Related ADRs

- [ADR-020: Use Case Result Naming](adr-020-use-case-result-naming.md) -- Result naming convention
- [ADR-012: Use Case Input/Output Models](adr-012-use-case-input-output-models.md) -- Command/Query pattern
