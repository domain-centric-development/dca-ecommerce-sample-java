> **Status:** Bootstrap Draft — automatically extracted from source code.
> Must be ratified by a domain expert before terms become binding. In
> particular, lifecycle terms (Checked Out vs. Completed) and Item-vs.-Product
> usage need sharpening.

# Bounded Context: Cart (Shopping Cart)

The Cart context manages a customer's shopping cart from adding individual
items to triggering checkout. It references products from the Product context
via `ProductId` and retrieves current prices/availability through an
`ArticlePriceResolver` (anti-corruption layer to Pricing/Inventory).

## Features (application layer)

The Cart context groups its use cases into four features — domain-named navigation groups below
`application/`, not model boundaries; every feature works on the one `ShoppingCart` aggregate.

| Feature | Meaning | Use cases |
|---------|---------|-----------|
| `shopping` | Filling and reading the shopping cart while the customer shops | `createcart`, `getorcreateactivecart`, `getcartbyid`, `additemtocart`, `removeitemfromcart` |
| `cartrecovery` | Recovering a guest cart on login — offering and applying a merge | `recovercart`, `getcartmergeoptions`, `mergecarts` |
| `cartcheckout` | Handing the cart over to Checkout (`checkout`) and closing it once the order is confirmed (`complete`) | `checkoutcart`, `completecart` |
| `operations` | Operating the shop — staff-only views across all carts | `getallcarts` |

`application/shared/` stays context-wide (`ShoppingCartRepository`, `ArticleDataPort`). The web
adapters mirror `shopping` and `cartrecovery`; the REST resource serves every feature and stays under
`adapter/incoming/api/`.

## Aggregates

### ShoppingCart

An editable collection of stable positions. Checkout copies a snapshot; completion reconciles captured unit identities without completing the current cart. Abandoned carts reject edits. Legacy CheckedOut/Completed states remain readable.

### CartItem

**Definition:** A single item in the shopping cart with a reference to the
product, the desired quantity, and the price captured at the time it was added
(price snapshot).

**Type:** Entity

**Identity:** `CartItemId`

**Synonyms (avoid):** "Line Item", "Position" — prefer `CartItem`.

**Related terms:** `Quantity`, `ProductId`, `Price` (Shared Kernel),
`priceAtAddition`

**Operations:** Package-internal only, via `ShoppingCart`: `updateQuantity`,
`increaseQuantity`, `decreaseQuantity`

**Notes:** Exists only within a `ShoppingCart` aggregate; no dedicated
repository. A repository restoring a stored cart passes `ShoppingCart.StoredItem`
values to `ShoppingCart.reconstitute` rather than constructing `CartItem` itself.

## Value Objects

### CartId

**Definition:** Unique identifier of a `ShoppingCart`.

**Type:** Value Object (Id)

### CartItemId

**Definition:** Unique identifier of a cart item within a `ShoppingCart`.

**Type:** Value Object (Id)

### CustomerId

**Definition:** Identifier of the customer who owns the cart. Reference into
the Customer/Account context.

**Type:** Value Object (Id)

**Notes:** Open question — should this type move to the Shared Kernel if
further contexts reference it?

### Quantity

**Definition:** Positive item count of a cart item (`> 0`). Carries
arithmetic operations (`increase`, `decrease`, `add`, `subtract`) with
protection against values ≤ 0.

**Type:** Value Object

### CartStatus

ACTIVE/Active permits editing before, during and after snapshot checkout. ABANDONED/Abandoned rejects edits. CHECKED_OUT/CheckedOut and COMPLETED/Completed are legacy whole-cart states, not transitions caused by the current checkout flow.

### ArticlePrice

**Definition:** Snapshot of price, availability, and stock level of an article
— delivered by the `ArticlePriceResolver`.

**Type:** Value Object

### CartArticle

**Definition:** The Cart context's view of an article with name, current
price, available stock, availability status, and image URL. Required for
enriched read models.

**Type:** Value Object

**Synonyms (avoid):** Not to be confused with `ProductArticle` from the
Product context — both are modeled context-locally.

### CartValidationResult

**Definition:** Result of a checkout validation. Optionally contains a list
of validation errors per item (unavailable product, insufficient stock).

**Type:** Value Object

**Related terms:** `CartValidationResult.ValidationError`,
`CartValidationResult.ErrorType`

### EnrichedCart

**Definition:** Enriched read model of the shopping cart with current article
data. Knows cross-context rules such as price-change detection, checkout
eligibility, and subtotal calculation using current vs. original prices.

**Type:** Value Object (Read Model)

### EnrichedCartItem

**Definition:** Enriched item from `EnrichedCart`, combining `CartItem` data
(quantity, original price) with current `CartArticle` data — enables price
comparison, stock checks, and line-total calculation.

**Type:** Value Object

## Domain Events

### CartItemAddedToCart

**Definition:** An item was added to the shopping cart, or the quantity of an
existing item was increased by adding it again.

**Type:** Domain Event

**Synonyms (avoid):** Former names `ItemAddedToCart`, `CartItemAdded`.

### ProductRemovedFromCart

**Definition:** A product was completely removed from the shopping cart (all
related items deleted).

**Type:** Domain Event

**Synonyms (avoid):** `CartItemRemoved` — do not use in parallel; flagged as
a duplicate in the DCA review. If item-level removal is to be modeled
separately, clarify with the domain expert.

### CartItemQuantityChanged

**Definition:** The quantity of an existing cart item was changed (increased
or decreased). Carries the old and new quantity.

**Type:** Domain Event

### CartCleared

**Definition:** All items were removed from the shopping cart at once (e.g.,
via "clear cart").

**Type:** Domain Event

### CartCheckedOut

A snapshot-submission fact. It does not lock an active cart. Explicit Checkout start creates the session; cart-change notifications do not.

### CartCompleted

Legacy event name retained for the cart reconciliation notification. The current completion handler removes only captured units; it does not change an active cart to Completed. Replays with no intersection emit nothing.

### CartAbandoned

**Definition:** The shopping cart was abandoned by the customer (e.g., after
an inactivity threshold). Final state.

**Type:** Domain Event

## Domain Services

### CartTotalCalculator

**Definition:** Extracts the value-added tax contained in a cart's gross amounts (default
19% VAT) and derives the net amount; the subtotal itself does not change.

**Type:** Domain Service

**Notes:** Invoked by the `GetCartById` use case, which puts the contained tax into its result; the cart page
adapter only formats that value.

## Specifications

### CartSpecification

**Definition:** Sealed marker for all cart-related specifications; enables
adapters (e.g., JPA) to translate them into persistence queries via the
Visitor pattern.

**Type:** Specification (Marker)

### ActiveCart

**Definition:** Shopping cart is in state `ACTIVE`.

**Type:** Specification

### LastUpdatedBefore

**Definition:** Shopping cart was last updated before a given timestamp (for
abandoned-cart detection).

**Type:** Specification

**Notes:** In-memory evaluation is currently neutral because the aggregate
does not carry an `updatedAt` — the persistence adapter evaluates the
predicate push-down. Open question: should `updatedAt` be added to the
aggregate?

### HasMinTotal

**Definition:** Cart total reaches or exceeds a minimum value (e.g., for a
minimum order amount or free shipping).

**Type:** Specification

### HasAnyAvailableItem

**Definition:** Shopping cart contains at least one sellable item (available,
not discontinued).

**Type:** Specification

**Notes:** In-memory neutral (true), since the domain model has no notion of
availability — the adapter evaluates this via joins.

### CustomerAllowsMarketing

**Definition:** The customer owning the cart has consented to marketing
communication (opt-in).

**Type:** Specification

**Notes:** In-memory neutral. Open question: does marketing consent belong
functionally in the Cart context at all, or is this a cross-context query to
Account/Customer?

## Factories

### EnrichedCartFactory

**Definition:** Builds an `EnrichedCart` from a `ShoppingCart` aggregate and a
map of current article data (`CartArticle` per `ProductId`). Validates
completeness of the article data.

**Type:** Factory

## Ports / Interfaces

### ArticlePriceResolver

Legacy application/test lookup abstraction. No aggregate accepts this callback. CartPricing receives immutable line and ArticlePrice snapshots retrieved by the use case.

### Price Snapshot (priceAtAddition)

**Definition:** The price of a cart item frozen at the time it was added.
Serves price-change detection and transparency toward the customer, but is
NOT the price used for settlement at checkout time (that one is fetched fresh
from the `ArticlePriceResolver`).

**Type:** Concept

**Notes:** Open question: which price is binding for the customer — the one
at the time of adding or the current one? Clarify the business policy.

### Shared contract revision (2026-09-09)

Price wraps strictly positive Money; Money is ISO 4217, non-negative, two decimals half-up, maximum 999999999999.99.
Default quantities must be rejected before mutation/reconstitution. ProductCreated is raised by aggregate creation;
product-created v1 exposes only eventId, occurredOn, productId, amount, currency and initialStock.
