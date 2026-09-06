# Glossary — Bounded Context: Checkout

> **Bootstrap draft** — Automatically extracted from the current domain code.
> Terms reflect the as-is implementation, not necessarily the target Ubiquitous Language.
> Conflicts and polysemy are marked under "Notes" and should be resolved with the business
> stakeholder before this draft is adopted as authoritative.

Sources: `dev.domaincentric.sample.ecommerce.checkout.domain.{model,event,service,readmodel}`.

---

## Features (application layer)

The Checkout context groups its use cases into three features — domain-named navigation groups below
`application/`, not model boundaries; every feature works on the one `CheckoutSession` aggregate.

| Feature | Meaning | Use cases |
|---------|---------|-----------|
| `session` | Starting a checkout session from a cart and reading it back in its lifecycle states | `startcheckout`, `getactivecheckoutsession`, `getcheckoutsession`, `getconfirmedcheckoutsession` |
| `checkoutcompletion` | The steps that complete a checkout: buyer information, delivery, payment, confirmation | `submitbuyerinfo`, `getshippingoptions`, `submitdelivery`, `getpaymentproviders`, `submitpayment`, `confirmcheckout` |
| `cartsync` | Following changes of the underlying cart while the session is active | `synccheckoutwithcart` |

`application/shared/` stays context-wide. The web adapters mirror `session` and `checkoutcompletion`
(`adapter/incoming/web/{feature}`), the event consumer sits in `adapter/incoming/event/cartsync/`.

---

## Aggregate Roots

### CheckoutSession

**Definition:** Represents a customer's complete payment process via a 5-step flow
(buyer info → delivery → payment → review → confirmation) and encapsulates all captured data,
amounts, and the order status.

**Type:** Aggregate Root

**Identity:** `CheckoutSessionId`

**Related terms:** `CartId`, `CustomerId`, `CheckoutLineItem`, `CheckoutTotals`, `CheckoutStep`,
`CheckoutSessionStatus`, `BuyerInfo`, `DeliveryAddress`, `ShippingOption`, `PaymentSelection`,
`CheckoutArticlePriceResolver`, `CheckoutValidationResult`.

**Operations:** `start`, `syncLineItems`, `submitBuyerInfo`, `submitDelivery`, `submitPayment`,
`calculateOrderTotal`, `validateItems`, `confirm`, `complete`, `abandon`, `expire`, `goBackTo`,
`isStepCompleted`, `isActive`, `isCompleted`.

**Notes:** Holds step data in nullable fields until each step is fulfilled. Terminal statuses
(`COMPLETED`, `ABANDONED`, `EXPIRED`) prevent further mutations. `confirm()` without a resolver is
deprecated.

---

## Value Objects

### BuyerInfo

**Definition:** Contact details of the buyer (email, first and last name, phone) captured in the
first checkout step in order to process the order.

**Type:** Value Object

**Synonyms (avoid):** `Buyer`, `BuyerContact` — both appear in documentation/discussion but are
not modelled in code.

**Related terms:** `CheckoutSession`, `BuyerInfoSubmitted`.

**Notes:** Conflicts with `LineItemInfo` in the `CheckoutConfirmed` event — both names use the
`Info` suffix for different concepts (buyer vs. order line item in the event). Possible
consolidation: `Buyer` for the VO, `LineItem` (rename the event variant unambiguously).

### DeliveryAddress

**Definition:** Shipping address consisting of street, optional second street line, city,
postal code, country, and optional state. Captured in the delivery step.

**Type:** Value Object

**Related terms:** `CheckoutSession`, `ShippingOption`, `DeliverySubmitted`.

**Operations:** `formattedAddress`.

### ShippingOption

**Definition:** Selectable shipping method with identifier, display name, estimated delivery time,
and shipping cost.

**Type:** Value Object

**Related terms:** `DeliveryAddress`, `CheckoutTotals`, `DeliverySubmitted`.

**Operations:** `isFree`.

### PaymentSelection

**Definition:** Selected payment method consisting of `PaymentProviderId` and the provider
reference (e.g. payment intent ID) the provider returned when the payment was initiated.

**Type:** Value Object

**Related terms:** `PaymentProviderId`, `PaymentSubmitted`.

**Operations:** `withReference`, `hasReference`.

### CheckoutLineItem

**Definition:** A line item within the checkout session — contains product reference, label,
unit price (at the time of addition), quantity, and an optional product image.

**Type:** Value Object

**Identity:** `CheckoutLineItemId`

**Related terms:** `CheckoutSession`, `EnrichedCheckoutLineItem`, `LineItemSnapshot`.

**Operations:** `lineTotal`, `withQuantity`.

**Notes:** Deliberately modelled as a Value Object (no separate entity) because identity is only
relevant within the `CheckoutSession`. Conflict: `LineItemInfo` (in the `CheckoutConfirmed` event)
and `LineItemSnapshot` (read model) structurally duplicate parts of the same content.

### CheckoutArticle

**Definition:** The checkout context's view of an article from other contexts — contains current
price, availability, and stock data needed for validation and price display during checkout.

**Type:** Value Object

**Related terms:** `EnrichedCheckoutLineItem`, `CheckoutCart`, `CheckoutArticlePriceResolver`.

**Operations:** `hasStockFor`.

**Notes:** Anti-Corruption Layer representation; no direct coupling to Product Catalog, Inventory,
or Pricing.

### EnrichedCheckoutLineItem

**Definition:** Combination of a historical `CheckoutLineItem` line and current `CheckoutArticle`
data; enables business comparisons such as price change since addition and sufficient stock.

**Type:** Value Object

**Related terms:** `CheckoutLineItem`, `CheckoutArticle`, `CheckoutCart`.

**Operations:** `currentLineTotal`, `originalLineTotal`, `hasPriceChanged`, `priceDifference`,
`hasSufficientStock`, `isValidForCheckout`.

### CheckoutCart

**Definition:** "Smart Shopping Cart" — enriched view of all line items in a checkout session
including current prices, price-change detection, and validity checking. Serves as a
read-optimized view for the summary and review screens.

**Type:** Value Object

**Related terms:** `CartId`, `CustomerId`, `EnrichedCheckoutLineItem`, `CheckoutCartFactory`,
`CheckoutCartSnapshot`.

**Operations:** `calculateCurrentSubtotal`, `calculateOriginalSubtotal`, `totalPriceDifference`,
`hasAnyPriceChanges`, `itemsWithPriceChanges`, `isValidForCheckout`, `invalidItems`,
`unavailableItems`, `itemsWithInsufficientStock`, `itemCount`, `totalQuantity`.

**Notes:** Conflicts with `CheckoutSession.lineItems` — both represent "cart inside checkout",
but `CheckoutSession` holds raw snapshots while `CheckoutCart` holds the enriched view. Also
structurally overlaps with `CheckoutCartSnapshot` (read model). Possible cleanup: rebrand
`CheckoutCart` as a pure review/display construct or unify it with the snapshot. External DTOs
`CartData`/`CartItemData` should be renamed in favor of `CartSnapshot`.

### CheckoutTotals

**Definition:** Calculated amounts of a checkout session: subtotal, shipping, tax, and grand
total.

**Type:** Value Object

**Related terms:** `CheckoutSession`, `Money`, `ShippingOption`.

**Operations:** `of`, `calculate`, `zero`, `withShipping`, `withTax`.

### CheckoutValidationResult

**Definition:** Result of validating the line items against current price and availability data;
contains a list of `ValidationError` entries.

**Type:** Value Object

**Related terms:** `ValidationError`, `CheckoutArticlePriceResolver`, `CheckoutSession`.

**Operations:** `isValid`, `valid`, `withErrors`, `withError`.

### ValidationError

**Definition:** A specific violation of checkout preconditions for a line item (e.g. product
unavailable, insufficient stock).

**Type:** Value Object

**Related terms:** `CheckoutValidationResult`, `ErrorType`, `ProductId`.

**Operations:** `productUnavailable`, `insufficientStock`.

### CheckoutStep

**Definition:** Step in the checkout flow (`BUYER_INFO`, `DELIVERY`, `PAYMENT`, `REVIEW`,
`CONFIRMATION`) with a defined order and navigation rules.

**Type:** Value Object (Enum)

**Operations:** `isBefore`, `isAfter`, `isTerminal`, `next`, `previous`.

### CheckoutSessionStatus

**Definition:** Lifecycle status of a checkout session: `ACTIVE`, `CONFIRMED`, `COMPLETED`,
`ABANDONED`, `EXPIRED`.

**Type:** Value Object (Enum)

**Operations:** `isModifiable`, `isTerminal`, `canConfirm`, `canComplete`.

### ErrorType

**Definition:** Classification of a validation error (`PRODUCT_UNAVAILABLE`,
`INSUFFICIENT_STOCK`).

**Type:** Value Object (Enum)

### CheckoutSessionId

**Definition:** Unique identifier of a checkout session.

**Type:** Value Object (ID)

**Operations:** `generate`, `of`.

### CheckoutLineItemId

**Definition:** Unique identifier of a line item within a checkout session.

**Type:** Value Object (ID)

### CartId

**Definition:** Context-local identifier of the underlying shopping cart.

**Type:** Value Object (ID)

**Notes:** Deliberate copy of the external cart identity; avoids direct coupling to the Cart
bounded context.

### CustomerId

**Definition:** Context-local identifier of the customer (may be a guest session).

**Type:** Value Object (ID)

**Notes:** Polysemy — the same name also exists in the Cart context and is semantically related
to `UserId`/`AccountId` in the Account context. To be resolved whether `UserId` from the Shared
Kernel should be used here.

### PaymentProviderId

**Definition:** Identifier of an external payment provider. The sample registers a single one,
`mock`, standing in for a real provider such as Stripe, PayPal or invoice.

**Type:** Value Object (ID)

### ArticlePrice

**Definition:** Bundle of current article price, availability, and stock level returned by the
`CheckoutArticlePriceResolver`.

**Type:** Value Object

**Related terms:** `CheckoutArticlePriceResolver`.

---

## Domain Events

### CheckoutSessionStarted

**Definition:** A new checkout session has been started from a shopping cart.

**Type:** Domain Event

**Related terms:** `CheckoutSession`, `CartId`, `CustomerId`.

### BuyerInfoSubmitted

**Definition:** Buyer details have been submitted in the first step.

**Type:** Domain Event

**Related terms:** `BuyerInfo`, `CheckoutSession`.

### DeliverySubmitted

**Definition:** Delivery address and shipping option have been submitted.

**Type:** Domain Event

**Related terms:** `DeliveryAddress`, `ShippingOption`.

### PaymentSubmitted

**Definition:** Payment method has been selected and submitted in the payment step.

**Type:** Domain Event

**Related terms:** `PaymentSelection`, `PaymentProviderId`.

### CheckoutConfirmed

**Definition:** The customer has confirmed the order at the review step; the order is finally
assembled.

**Type:** Domain Event

**Related terms:** `LineItemInfo`, `CheckoutSession`, `CartId`, `CustomerId`.

**Notes:** Not published cross-context directly; an outgoing adapter creates the integration
event `CheckoutConfirmedEvent`. `LineItemInfo` (in the payload) conflicts with `CheckoutLineItem`
and `LineItemSnapshot` — all three structurally describe the same line item for different
purposes.

### CheckoutCompleted

**Definition:** Payment was processed successfully and the order has been finalized.

**Type:** Domain Event

**Related terms:** `Money`, `orderReference`.

### CheckoutAbandoned

**Definition:** The customer has actively abandoned the checkout.

**Type:** Domain Event

**Related terms:** `CheckoutStep`.

### CheckoutExpired

**Definition:** The checkout session has been invalidated by the system due to inactivity.

**Type:** Domain Event

**Related terms:** `CheckoutStep`.

---

## Domain Services

### CheckoutStepValidator

**Definition:** Enforces navigation rules between the checkout steps and returns the correct
redirect path when access is not permitted.

**Type:** Domain Service

**Related terms:** `CheckoutSession`, `CheckoutStep`, `CheckoutSessionStatus`.

**Operations:** `validateStepAccess`, `getCurrentStepPath`.

**Notes:** Returns paths (`/checkout/...`, `/cart`) — the boundary to the UI/adapter layer should
be reviewed; the service may need to return abstract step targets instead of URLs.

### CheckoutArticlePriceResolver

**Definition:** Domain port for resolving current price and availability data for articles
during checkout.

**Type:** Domain Service (functional interface)

**Related terms:** `ArticlePrice`, `CheckoutArticle`, `CheckoutSession`.

**Operations:** `resolve`.

**Notes:** Implementation lives in the adapter layer; conceptually an output port. To be
clarified whether to formally classify as `OutputPort` or keep as `DomainService`.

---

## Factories

### CheckoutCartFactory

**Definition:** Creates a `CheckoutCart` instance from checkout line items and corresponding
current article data, validating the completeness of the input.

**Type:** Factory

**Related terms:** `CheckoutCart`, `CheckoutLineItem`, `CheckoutArticle`, `EnrichedCheckoutLineItem`.

**Operations:** `create`, `fromSession`.

---

## Read Models

### CheckoutCartSnapshot

**Definition:** Immutable, query-optimized snapshot of a `CheckoutSession`'s state for display
and read purposes.

**Type:** Concept (Read Model / Value Object)

**Related terms:** `CheckoutSession`, `LineItemSnapshot`.

**Operations:** `from`, `itemCount`, `totalQuantity`, `hasBuyerInfo`, `hasDeliveryAddress`,
`hasShippingOption`, `hasPaymentSelection`, `hasTotals`, `hasOrderReference`, `isActive`,
`isCompleted`, `isConfirmed`.

### LineItemSnapshot

**Definition:** Immutable view of a line item from the `CheckoutCartSnapshot`.

**Type:** Concept (Read Model / Value Object)

**Related terms:** `CheckoutLineItem`, `CheckoutCartSnapshot`.

**Operations:** `lineTotal`.

**Notes:** External adapter DTOs `CartData` and `CartItemData` should be renamed to follow this
snapshot naming scheme (`CartSnapshot` / `CartItemSnapshot`).

---

## Open issues from DCA review

- **`BuyerInfo` vs `Buyer`/`BuyerContact`** — Unify the naming scheme; avoid the `Info` suffix.
- **`LineItemInfo` (in `CheckoutConfirmed`) vs `CheckoutLineItem` vs `LineItemSnapshot`** —
  three near-identical structures; define clear roles (event payload vs. aggregate content
  vs. read model).
- **`CheckoutCart` vs `CheckoutSession.lineItems`** — Resolve duplicate "cart" representations;
  consider turning `CheckoutCart` into a pure review/display VO or merging it with the snapshot.
- **`CartData` / `CartItemData` (adapter DTOs) → `CartSnapshot` / `CartItemSnapshot`** —
  Align with the naming convention.
- **`CustomerId` polysemy** — the same name exists in `cart`, with semantic overlap to `UserId`/
  `AccountId` from `sharedkernel`/`account`. Clarify which identity is authoritative.
- **`CheckoutStepValidator` returns URL paths** — possible boundary violation
  domain → adapter; to be reviewed.
