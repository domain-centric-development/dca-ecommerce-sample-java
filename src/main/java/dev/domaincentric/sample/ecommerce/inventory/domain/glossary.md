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
- `create(productId, initialQuantity)` — Create a new stock-keeping unit
- `increaseStock(amount)` — Record incoming goods
- `decreaseStock(amount)` — Record outgoing goods (e.g. shipment)
- `reserve(amount)` — Reserve stock for an order
- `release(amount)` — Release previously reserved stock
- `adjustStockTo(quantity)` — Manual correction / stocktake (see Open Issues)
- `isAvailable()` — Checks whether unreserved stock is available

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

**Notes:** Currently `int`-based. Arithmetic operations (`plus`, `minus`) are
currently performed outside the value object in the aggregate — a candidate
for relocation into the value object.

---

### StockLevelId

**Definition:** Stable, context-local identity of a stock-keeping unit.

**Type:** Value Object (Identity)

**Identity:** UUID-based

**Related terms:** `StockLevel`

**Operations:** `generate()`, `of(uuid)`

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

_None._

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
the `SetStockLevel` use case. Idempotent: setting the same figure twice has the same effect.

---

### StockReductionTrigger

**Definition:** The shape Inventory asks of any event that means "stock has been sold": the line items
whose stock must go down. Owned here and implemented by Checkout's `CheckoutConfirmedEvent`.

**Type:** Consumer-defined trigger contract (Published Language, owned here)

**Related terms:** `StockLevel`, `StockInitializationTrigger`

**Notes:** Consumed by `inventory.adapter.incoming.event.StockReductionEventConsumer`. Its
`OrderLineItem` wording is an open issue — see below.

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

**Definition:** A product whose available quantity is lower than a threshold
given with the question. A property of the answer to that question, not a state
stored on `StockLevel`.

**Type:** Concept

**Related terms:** `GetLowStockProductsQuery`,
`GetLowStockProductsResult.LowStockProduct`,
`StockLevelRepository.findWithAvailableQuantityBelow`, Available quantity

**Notes:** The comparison is strict — a product at exactly the threshold is not
low. Reserved quantity does not enter the answer; the measure is *Available
quantity* as defined above, so a product with everything on hand reserved is
not reported as low. Whether that is the measure the business wants is an open
question of `backlog/stock-oversight/STORY-P1.md`.

---

### Low stock threshold

**Definition:** The quantity asked about when looking for low stock; a product
below it counts as low.

**Type:** Concept

**Related terms:** `GetLowStockProductsQuery.threshold`, Low stock

**Notes:** Per question, not per product — no product carries a reorder level of
its own. Negative thresholds are rejected.

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

3. **`StockReductionTrigger.OrderLineItem`** is **Checkout language** and does
   not belong in the Inventory context. From an Inventory perspective, the
   trigger of a stock reduction should be generic (e.g. "shipment order",
   "reservation release") — correlation to the order ID happens via
   integration events, not via borrowed terms.

4. **Arithmetic on `StockQuantity`** is currently done in the aggregate.
   Candidate for moving `plus`/`minus` operations into the value object.
