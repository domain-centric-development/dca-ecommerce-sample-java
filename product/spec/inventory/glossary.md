# Inventory Context — Ubiquitous Language Glossary

> **Bootstrap note:** This glossary was initially derived from the existing
> domain code (`inventory/domain/model` and `inventory/domain/event`). It
> reflects the **current state**, including known language and modeling
> conflicts (see the "Open Issues" section). Terms are intended to be aligned
> and consolidated iteratively with the business. Update via
> `/ubiquitous-language`.

Language: definitions in English, class names unchanged (English).

---

## Aggregates

### StockLevel

**Definition:** Stock-keeping unit for exactly one product. Manages the
available quantity in the warehouse as well as the portion of it reserved for
open orders, and ensures that reservations never exceed the available quantity.

**Type:** Aggregate Root

**Identity:** `StockLevelId`

**Related terms:** `StockQuantity`, `ProductId`, `StockLevelCreated`,
`StockIncreased`, `StockDecreased`, `StockReserved`, `StockReleased`,
`StockChanged`

**Operations:**
- `create(productId, initialQuantity)` — Create a new stock-keeping unit: the stock level comes into existence for one product, nothing reserved
- `increaseStock(amount)` — Record incoming goods: the available quantity grows by the amount
- `decreaseStock(amount)` — Record outgoing goods (e.g. shipment): the available quantity shrinks by the amount. It does not consume a reservation — a reservation above the new figure is only capped to it; there is no separate operation that turns reserved stock into shipped stock
- `reserve(amount)` — Reserve stock for an order: the amount is earmarked out of the unreserved stock and stays in the available quantity
- `release(amount)` — Release previously reserved stock: the reservation shrinks, the available quantity is unchanged
- `adjustStockTo(quantity)` — Manual correction / stocktake (see Open Issues): the available quantity is set to the counted figure, a reservation above it is capped
- `isAvailable()` — Checks whether unreserved stock is available (a question; changes nothing)

**Notes:** Invariant: `reservedQuantity <= availableQuantity`. If the available
stock drops below the reserved amount, the reservation is automatically
adjusted downward.

---

## Entities

_No standalone entities besides the aggregate root._

---

## Value Objects

### StockQuantity

**Definition:** Non-negative count of items in the warehouse. Serves as a
type-safe wrapper around a quantity and encapsulates the invariant
"quantity >= 0".

**Type:** Value Object

**Identity:** _value-based (`value: int`)_

**Related terms:** `StockLevel`

**Operations:**
- `of(value)` — Factory
- `value()` — Access to the raw value

**Notes:** Currently `int`-based. Arithmetic is currently performed outside the
value object in the aggregate — a candidate for relocation into the value
object.

---

### StockLevelId

**Definition:** Stable, context-local identity of a stock-keeping unit.

**Type:** Value Object (Identity)

**Identity:** UUID-based

**Related terms:** `StockLevel`

**Operations:** `generate()`, `of(value)`

---

## Domain Events

### StockLevelCreated

**Definition:** A new stock-keeping unit has been created for a product.

**Type:** Domain Event

**Related terms:** `StockLevel.create`

**Notes:** Payload: `stockLevelId`, `productId`, `initialQuantity`.

---

### StockIncreased

**Definition:** Available stock was increased by a specific amount
(typical trigger: incoming goods).

**Type:** Domain Event

**Related terms:** `StockLevel.increaseStock`

---

### StockDecreased

**Definition:** Available stock was reduced by a specific amount
(typical trigger: shipment / outgoing goods).

**Type:** Domain Event

**Related terms:** `StockLevel.decreaseStock`

---

### StockReserved

**Definition:** A portion of the available stock has been reserved for an open
order and is no longer available for further reservations.

**Type:** Domain Event

**Related terms:** `StockLevel.reserve`

---

### StockReleased

**Definition:** A previously made reservation has been cancelled; the quantity
is reservable again.

**Type:** Domain Event

**Related terms:** `StockLevel.release`

---

### StockChanged

**Definition:** Stock was set to a target value as part of a manual correction
or stocktake (delta-agnostic).

**Type:** Domain Event

**Synonyms (avoid):** _generic catch-all term_ — per the DCA review, should be
replaced by more business-precise events (`StockReconciled` for stocktake), or
reduced entirely to `StockIncreased`/`StockDecreased`.

**Related terms:** `StockLevel.adjustStockTo`

**Notes:** See Open Issues — "too generic".

---

## Domain Services

_None._

---

## Specifications

### StockLevelSpecification

**Definition:** Common interface of all stock-related specifications; enables
persistence adapters to translate them into queries via the Visitor pattern
(`StockLevelSpecificationVisitor`).

**Type:** Specification (Marker)

### AvailableQuantityBelow

**Definition:** The available quantity of a stock level is strictly below a
given threshold — the stock level is *low*. The threshold itself is not low.

**Type:** Specification

**Related terms:** `StockQuantity`, Available quantity, Low stock, Low stock
threshold

**Notes:** Compares the quantity **held**; reservations do not enter the
comparison (see Open Issues). Evaluated in memory by the default
`StockLevelRepository.findBy`; the visitor exists so an adapter can adopt
predicate push-down without query technology entering the domain.

---

## Factories

_No separate factories — construction via the static factory method
`StockLevel.create(...)`._

---

## Integration Contracts

### StockInitializationTrigger

**Definition:** The shape Inventory asks of any event that means "this product must have a stock level
now": the product and the quantity it starts with. Inventory owns the contract; the Product Catalog
implements it on its `ProductCreatedEvent` (Interface Inversion), so Inventory never depends on the
catalog.

**Type:** Consumer-defined trigger contract (Published Language, owned here)

**Related terms:** `StockLevel`, `StockReductionTrigger`

**Notes:** Consumed by `inventory.adapter.incoming.event.StockInitializationEventConsumer`, which calls
the `ProvisionInitialStock` use case with the event's `eventId()` (the contract extends `IntegrationEvent`). The use case
skips an event `ProcessedEvents` has already recorded for its effect, `set-stock-level` (the stored key keeps its earlier name), and records it in the
same transaction — a redelivery cannot reset a stock level that changed since.

---

### StockReductionTrigger

**Definition:** The shape Inventory asks of any event that means "stock has been sold": the line items
whose stock must go down. Owned here and implemented by Checkout's `CheckoutConfirmedEvent`.

**Type:** Consumer-defined trigger contract (Published Language, owned here)

**Related terms:** `StockLevel`, `StockInitializationTrigger`

**Notes:** Consumed by `inventory.adapter.incoming.event.StockReductionEventConsumer`, which hands all
lines and the event's `eventId()` to the `ReduceStock` use case in one command (the contract extends
`IntegrationEvent`). The use case reduces the lines in one transaction, skips an event `ProcessedEvents` has
already recorded for its effect, `reduce-stock`, and records it there — a redelivered order never reduces stock
twice, while `set-stock-level` for the same event id is a different effect and still happens. The trigger
names its lines in Inventory's words — `lineItems()`, each a `StockReductionLineItem` (product and quantity
whose stock goes down) — not in Checkout's: the data comes from the order, the names do not.

---

## Concepts (not in code, but in conversation)

### Available quantity

**Definition:** Quantity of a product physically held in the warehouse,
regardless of whether parts of it are already reserved.

**Type:** Concept

**Related terms:** `StockQuantity`, `StockLevel.availableQuantity`

---

### Reserved quantity

**Definition:** Portion of the available stock that is blocked for orders not
yet shipped and is no longer available for further reservations.

**Type:** Concept

**Related terms:** `StockQuantity`, `StockLevel.reservedQuantity`

---

### Unreserved quantity (available-to-promise)

**Definition:** `availableQuantity − reservedQuantity` — the quantity that can
still be reserved.

**Type:** Concept

**Notes:** Currently not modeled as a value object; candidate for extraction.

---

### Low stock

**Definition:** A product whose available quantity is strictly below a threshold
given with the question — the state an operator must react to before the
product runs out. A property of the answer to that question, not a state stored
on `StockLevel`.

**Type:** Concept

**Related terms:** `AvailableQuantityBelow`, `GetLowStockProductsQuery`,
`GetLowStockProductsResult.LowStockProduct`, Available quantity, Low stock
threshold

**Notes:** Realised by `AvailableQuantityBelow`; asked for through the
`GetLowStockProducts` use case, which answers a `LowStockProduct` per stock
level with the quantity it holds. The comparison is strict — a product at
exactly the threshold is not low. Reserved quantity does not enter the answer,
so a product with everything on hand reserved is not reported as low (see Open
Issues).

---

### Low stock threshold

**Definition:** The quantity asked about when looking for low stock; a product
below it counts as low, a product at or above it does not.

**Type:** Concept

**Related terms:** `GetLowStockProductsQuery.threshold`, `AvailableQuantityBelow`,
Low stock

**Notes:** Per question, not per product — no stock level carries a reorder
level of its own (see Open Issues). Negative thresholds are rejected.

---

## Open Issues (from DCA review)

These points are intentionally left open and should be clarified with the
business before the model is consolidated:

1. **Use-case split for `adjustStockTo`** (formerly `setAvailableQuantity`) — the method conflates
   three business-distinct operations:
   - **Set** (initial capture / master-data correction)
   - **Adjust** (correction posting with reason)
   - **Reconcile** (stocktake reconciliation)

   Recommendation: split the method and emit an appropriate event per trigger.

2. **`StockChanged` is too generic** — should be replaced by business-meaningful
   events such as:
   - `StockReconciled` (stocktake)
   - or a clean separation via `StockReserved` / `StockReleased` /
     `StockIncreased` / `StockDecreased`, which already exist anyway.

3. **Arithmetic on `StockQuantity`** is currently done in the aggregate.
   Candidate for moving addition and subtraction into the value object.

4. **Which quantity is "low"?** `AvailableQuantityBelow` compares the
   available quantity, so a stock level of 4 with 4 units reserved is not low
   at a threshold of 4. Whether the operator means available quantity or
   unreserved quantity (available-to-promise) is unanswered; both readings are
   in this glossary.

5. **Whose threshold?** One threshold is supplied per question. Whether a stock
   level should instead carry its own reorder level is unanswered.

---

## Failures

Named refusals of the model. Each carries the figures the rule compared; an incoming adapter maps the type to its
own answer. A negative quantity is not among them — that is a malformed call and stays an argument guard.

### InsufficientStock

**Definition:** More stock would be taken out than the stock keeping unit holds.

**Type:** Domain failure (`DomainException`)

**Related terms:** Available quantity, `StockLevel.decreaseStock`

---

### InsufficientUnreservedStock

**Definition:** More stock would be reserved than is still promisable.

**Type:** Domain failure (`DomainException`)

**Related terms:** Unreserved quantity (available-to-promise), `StockLevel.reserve`

---

### InsufficientReservedStock

**Definition:** More stock would be released than was ever reserved.

**Type:** Domain failure (`DomainException`)

**Related terms:** Reserved quantity, `StockLevel.release`

## Features

The parts of the context's behaviour its people name; each groups what holds in a folder of the living spec.

### Availability (availability)

**Definition:** Whether a product can be had, as the shop shows it.

**Type:** Feature

### Stock (stock)

**Definition:** Stock going down by what was bought.

**Type:** Feature
