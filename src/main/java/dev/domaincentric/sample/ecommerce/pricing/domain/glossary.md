# Pricing Context — Ubiquitous Language Glossary

> **Bootstrap note:** This glossary was initially derived from the existing
> domain code (`pricing/domain/model` and `pricing/domain/event`). It reflects
> the **current state**, including known language and modeling conflicts
> (see the "Open Issues" section). Terms are intended to be aligned
> iteratively with the business. Update via `/ubiquitous-language`.

Language: definitions in English, class names unchanged (English).

---

## Aggregates

### ProductPrice

**Definition:** The currently valid sales price of a product in the Pricing
context, including the point in time from which this price is effective.
Encapsulates the business rules around price formation and price changes.

**Type:** Aggregate Root

**Identity:** `PriceId` _(see Open Issues — suitability of the identity is open)_

**Related terms:** `Money`, `ProductId`, `PriceCreated`, `PriceChanged`

**Operations:**
- `create(productId, price)` — Initial price determination for a product
- `updatePrice(newPrice)` — Price change (new effective point in time)
- `currentPrice()` — Currently valid price
- `effectiveFrom()` — Point in time from which `currentPrice` applies

**Notes:** Invariant: `price > 0`. History is currently kept only implicitly
via `PriceChanged` events; an explicit history is not modeled.

---

## Entities

_No standalone entities besides the aggregate root._

---

## Value Objects

### PriceId

**Definition:** Technical identity of a `ProductPrice` aggregate.

**Type:** Value Object (Identity)

**Identity:** UUID-based

**Synonyms (avoid):** _see Open Issues_ — `PriceId` lacks business meaning;
in a pure pricing model, `ProductId` suffices as a natural key.

**Related terms:** `ProductPrice`

**Operations:** `generate()`, `of(uuid)`

---

### Money (borrowed from the Shared Kernel)

**Definition:** Monetary amount with currency. Universal value object from
the Shared Kernel.

**Type:** Value Object

**Related terms:** `ProductPrice.currentPrice`

**Notes:** Used here to value products; for the full definition see the
Shared Kernel.

---

## Domain Events

### PriceCreated

**Definition:** A price has been recorded for a product for the first time in
the Pricing context.

**Type:** Domain Event

**Related terms:** `ProductPrice.create`

**Notes:** Payload: `priceId`, `productId`, `price`, `effectiveFrom`.

---

### PriceChanged

**Definition:** The valid price of a product was changed from an old value to
a new one; a new effective point in time was set.

**Type:** Domain Event

**Related terms:** `ProductPrice.updatePrice`

**Notes:** Payload: `priceId`, `productId`, `oldPrice`, `newPrice`,
`effectiveFrom`.

---

## Domain Services

_None._

---

## Specifications

_None._

---

## Factories

_No separate factories — construction via the static factory method
`ProductPrice.create(...)`._

---

## Integration Contracts

### PriceInitializationTrigger

**Definition:** The shape Pricing asks of any event that means "this product must have a price now":
the product and the price it starts with. Pricing owns the contract and listens to it; the publishing
context (the Product Catalog) implements it on its `ProductCreatedEvent`, so Pricing never depends on
whoever creates products (Interface Inversion).

**Type:** Consumer-defined trigger contract (Published Language, owned here)

**Related terms:** `ProductPrice`, **Initial Price Determination**

**Notes:** Consumed by `pricing.adapter.incoming.event.PriceInitializationEventConsumer`, which calls
the `SetProductPrice` use case. Idempotent: a redelivery updates the existing price instead of failing.

---

## Concepts (not in code, but in conversation)

### Current price

**Definition:** The price that applies to a product at the present point in
time.

**Type:** Concept

**Related terms:** `ProductPrice.currentPrice`

---

### Effective from

**Definition:** Point in time from which a price is valid. Today always "now"
(`Instant.now()`); future price scheduling is not modeled.

**Type:** Concept

**Related terms:** `ProductPrice.effectiveFrom`

---

### Initial price

**Definition:** The price recorded for a product for the first time — the
transition from the "unpriced" to the "priced" state.

**Type:** Concept

**Related terms:** `ProductPrice.create`, _see Open Issues:
`provisionInitialPrice`_

---

## Open Issues (from DCA review)

These points are intentionally left open and should be clarified with the
business:

1. **`PriceId` identity without business meaning** — per product there is
   exactly one currently valid `ProductPrice`. A dedicated `PriceId` in
   addition to the `ProductId` introduces unnecessary complexity.

   Options:
   - Use `ProductId` as the natural identity of the aggregate (`ProductPrice`
     is then unique per product), or
   - Extend the model into a **price history** in which each historical price
     entry has its own identity (then `PriceId` makes sense).

2. **CRUD language `setInitialPrice`** in the use-case layer — should be
   renamed to **`provisionInitialPrice`** (or similar business-meaningful name)
   to linguistically express the transition character ("a product is being
   priced for the first time"). In the aggregate this already corresponds to
   `ProductPrice.create(...)`.

3. **Missing price history** — the current modeling overwrites `currentPrice`
   and emits `PriceChanged`. A real price history (e.g. for retrospective
   analyses or future-dated prices) would be a separate modeling topic.

4. **Future-dated prices** — `effectiveFrom` is currently fixed to
   `Instant.now()`. Scheduled price changes (e.g. "effective June 1st") are
   not modeled.

### Shared contract revision (2026-09-09)

Price wraps strictly positive Money; Money is ISO 4217, non-negative, two decimals half-up, maximum 999999999999.99.
Default quantities must be rejected before mutation/reconstitution. ProductCreated is raised by aggregate creation;
product-created v1 exposes only eventId, occurredOn, productId, amount, currency and initialStock.
