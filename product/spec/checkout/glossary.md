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
| `checkoutcompletion` | The steps that complete a checkout: buyer information, delivery, payment, confirmation | `resumecheckout`, `submitbuyerinfo`, `getshippingoptions`, `submitdelivery`, `getpaymentproviders`, `submitpayment`, `getcheckoutreview`, `confirmcheckout` |
| `cartsync` | Following changes of the underlying cart while the session is active | `synccheckoutwithcart` |
| `orderconfirmation` | Informing the buyer that the order went through | `sendorderconfirmation` |

`application/shared/` stays context-wide. The web adapters mirror `session` and `checkoutcompletion`
(`adapter/incoming/web/{feature}`); `CheckoutRoutes`, `CheckoutStepNotice` and `CartNotice` (the codes the cart page
knows) stay beside them, shared. `CheckoutStepPage` in `checkoutcompletion/` is the one place a step page is opened
or a refused submission translated. The event consumers sit in `adapter/incoming/event/cartsync/` and
`adapter/incoming/event/orderconfirmation/`.

## Aggregate Roots

### CheckoutSession

An explicit checkout action captures immutable positions, quantities and prices into a session. Cart edits do not
create or mutate sessions. A new action supersedes the previous OPEN/Active session; confirmed/completed orders remain.
Confirmation and replacement serialize through the same repository operation, including transaction completion.
Superseded confirmation has no completion effect. Abandonment/expiry closes only an open session and leaves cart contents.

Cart reconciliation intersects purchased unit intervals with the current stable position id. Later additions (also of
the same product), removed/re-added positions and other contents survive. Replay and overlapping completed snapshots
cannot remove a unit twice. JDBC/JPA cart persistence preserves the interval allocation watermark; in-memory persistence
retains the same domain state. Legacy CheckedOut/Completed cart statuses remain readable, but snapshot checkout leaves
an active cart editable and never completes the whole cart.

Confirmation retrieves current price/availability/stock facts before its local transaction. Pure domain services consume
immutable line/fact snapshots. The review shows the session's lines at today's prices (`reviewAt`, a
`CheckoutReview`) and changes nothing; "Place Order" carries the review fingerprint of what the customer was shown. A
shortage, or a review that is no longer the one shown, refuses the order and leaves state, totals and events
unchanged; the review then shows the order as it is today. Success orders the reviewed lines and stores and publishes
the reviewed total; there is no no-argument confirmation path. Before its transaction, confirmation reserves the
stock of every line in Inventory (reserve stock, through `StockReservationPort`), all lines or none; a refused
reservation is a shortage of that line, and a confirmation that then fails gives the reservation back (release stock).
Local in-memory repository serialization is not a
claim of durable distributed transactions or universal rollback of unenlisted resources.

**Type:** Aggregate Root

**Operations:**
- `start` — the session comes into existence over a snapshot of the cart's positions, quantities and prices
- resuming the checkout (`takeOverBy`, through `ResumeCheckout`) — the active session of the customer's active cart
  becomes that customer's: same id, cart, step and line items; no event; never starts a session; refused on a session
  that no longer takes changes. The buyer step's page resumes it when it finds no session of the customer (ADR-046)
- `supersede` — kept with status superseded when a new checkout action replaces it; no event; only an active session can be superseded
- `submitBuyerInfo`, `submitDelivery`, `submitPayment` — record the step's data and move on to the next step when
  standing on it; `submitPayment` records the chosen provider only, requests nothing from it and raises no event
- `assertReadyForPayment` — a question: the preconditions of `submitPayment` plus a positive total, checked without
  changing anything
- authorize the payment (`authorizePayment`) — at Place Order, records on a session at the review that its chosen
  provider authorized the total the review showed under a payment reference, and records that amount; publishes
  `PaymentSubmitted`; refused on a session not at the review or one that no longer takes changes
- `goBackTo` — moves the current step back; data already submitted stays
- work out the review (`reviewAt`, through `GetCheckoutReview`) — the session's lines at today's prices, quantities
  kept, a line without today's price at its own; totals follow, shipping kept; changes nothing and raises no event
- `assertReviewed` — a question: is the review at today's prices the one the customer was shown (its review
  fingerprint)? Answers with that review, refuses with `CheckoutChangedSinceReviewException`
- `assertReadyForConfirmation` — a question: the preconditions of `confirm`, the pricing verdict included, checked without changing anything — asked before the order's stock is reserved
- `confirm` — status confirmed, ordering the reviewed lines at the reviewed total; refused on a shortage, when the
  review is no longer the one shown, or when the payment was authorized for an amount other than the reviewed total
  (`AuthorizedAmountDiffersFromReviewException`)
- `complete` — after payment: status completed, with the order reference
- `isOrderPlaced` — a question: was an order placed with this session? True once it is confirmed, and after it was
  completed
- `abandon` — kept with status abandoned; the cart's contents stay
- `expire` — kept with status expired, for inactivity; the cart's contents stay
- `syncLineItems` — exists only to refuse: a session's positions are immutable, a changed cart needs a new session


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

### Shipping option (ShippingOption)

**Definition:** A way of shipping the shop offers at checkout — its title (for example "Standard",
"Express"), the estimated delivery and the cost the shop sets, never one the buyer sends. The buyer
selects one. Agent commerce protocols call it a fulfillment option of type shipping (UCP, ACP) or a
shipping option (W3C Payment Request); a carrier, where one is named, is a field of its own.

**Type:** Value Object

**Related terms:** `DeliveryAddress`, `CheckoutTotals`, `DeliverySubmitted`.

**Operations:** `isFree`.

### PaymentSelection

**Definition:** Selected payment method consisting of `PaymentProviderId` — required — and, once the provider
authorized the payment at Place Order, the provider reference it returned together with its **authorized amount**
(`authorizedAmount`): the total the provider authorized the payment for. Chosen on the payment step, it carries the
provider alone.

**Type:** Value Object

**Related terms:** `PaymentProviderId`, `PaymentSubmitted`, `PaymentResult`.

**Operations:** `of`, `authorized`, `hasReference`.

### Authorized amount (AuthorizedAmount)

**Definition:** The total the review showed, authorized by the payment provider at Place Order. The order is
confirmed only when it equals the total the review showed (`AuthorizedAmountDiffersFromReviewException` otherwise).

**Type:** Value Object (`Money`, part of `PaymentSelection`)

**Related terms:** `PaymentSelection`, `CheckoutTotals`, Payment cancellation.

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

### Review (CheckoutReview)

**Definition:** The order as the review shows it: the session's lines at today's prices, the buyer, the delivery
address, the shipping option, the payment method and the totals. Worked out by the session (`reviewAt`), stored
nowhere.

**Type:** Value Object

**Related terms:** `CheckoutSession`, `ReviewFingerprint`, `CheckoutChangedSinceReviewException`,
`CheckoutCartSnapshot`.

**Operations:** `fingerprint`; `refuseUnlessShown` — refuses unless the given fingerprint is its own, naming the
line's product when only that line's price differs.

### Review fingerprint (ReviewFingerprint)

**Definition:** What the review showed — lines with product, quantity and unit price, buyer, delivery address,
shipping option and price, payment method, total — sent with "Place Order" (the form field `reviewFingerprint`). A
blank or malformed value matches no review.

**Type:** Value Object

**Related terms:** `CheckoutReview`, `CheckoutChangedSinceReviewException`.

**Operations:** `of`; `firstChangedLine` — the line whose price alone differs from the one shown, if any.

### CheckoutValidationResult

**Definition:** Result of validating the line items against current availability and stock data;
contains a list of `ValidationError` entries. A changed price is no validation error: the review
fingerprint decides it.

**Type:** Value Object

**Related terms:** `ValidationError`, `CheckoutArticlePriceResolver`, `CheckoutSession`.

**Operations:** `isValid`, `valid`, `withErrors`, `withError`; `shortOfStock` — the line a refusal for stock
names: the first, in line order, that is unavailable or short of stock.

### ValidationError

**Definition:** A specific violation of checkout preconditions for a line item (e.g. product
unavailable, insufficient stock), carrying the line's product name as the line shows it.

**Type:** Value Object

**Related terms:** `CheckoutValidationResult`, `ErrorType`, `ProductId`.

**Operations:** `productUnavailable`, `insufficientStock`.

### CheckoutStep

**Definition:** Step in the checkout flow (`BUYER_INFO`, `DELIVERY`, `PAYMENT`, `REVIEW`,
`CONFIRMATION`) with a defined order and navigation rules.

**Type:** Value Object (Enum)

**Operations:** `isBefore`, `isAfter`, `isTerminal`, `next`, `previous`.

### StepAccess

**Definition:** The domain's answer to "may this checkout step be opened?": granted, redirect to
another step, or back to the cart (no usable session).

**Type:** Value Object

**Related terms:** `CheckoutStep`, step access on `CheckoutCartSnapshot`.

**Operations:** `grant()`, `redirectTo(step)`, `backToCart()`, `granted()`, `isBackToCart()`.

**Notes:** Carries a step, never a URL — the incoming adapter turns it into a route.

### CheckoutSessionStatus

ACTIVE/Active is the open, modifiable session. CONFIRMED/Confirmed may complete; COMPLETED/Completed is final. SUPERSEDED/Superseded, ABANDONED/Abandoned and EXPIRED/Expired reject confirmation. Only an open session may be replaced, abandoned or expired.

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

**Definition:** Identifier of an external payment provider. The shop registers a single one:
`provider`, the payment provider behind its REST contract, where a provider address
(`checkout.payment-provider.base-url`) is configured; `mock`, the payment stand-in, otherwise.

**Type:** Value Object (ID)

**Related terms:** `PaymentResult`, `PaymentSelection`.

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

**Definition:** The chosen payment provider authorized the total the review showed when the customer placed the
order at the review (`authorizePayment`). Choosing how to pay on the payment step publishes none.

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

### Step access

**Definition:** Whether a checkout may be shown at a requested step, and where the customer goes
instead: terminal and confirmed sessions only reach the confirmation, and a step whose prerequisites are
unfulfilled sends them to the step they are actually on. Going back to a completed step is allowed. A
session that is not there at all sends them back to the cart — the one part the model cannot answer,
because there is no snapshot to ask; the `GetCheckoutSession` use case answers that.

**Type:** Behaviour of `CheckoutCartSnapshot`

**Notes:** Business logic, not presentation: it holds whether the user interface shows one page or five.
It decides on the snapshot alone, so a read does not load the session aggregate.

**Related terms:** `CheckoutSession`, `CheckoutCartSnapshot`, `CheckoutStep`, `StepAccess`.

**Operations:** `accessTo(targetStep)` — answers with a `StepAccess`.

**Notes:** Decides on the `CheckoutCartSnapshot` read model and knows no routes. The `GetCheckoutSession` query
use case invokes it when a step is requested and delivers the `StepAccess` in its result; the web
adapter (`CheckoutRoutes`) maps the decision to a redirect path. A "Place Order" is accepted only from the
review's own form (`sentFrom=review`); sent from anywhere else it is not placed and leads to the review, or,
when Step access denies the review, to the step still open. Opening the review changes no state, so the web
adapter (`ConfirmationPageController`) holds this rule, not the snapshot.

### CheckoutArticlePriceResolver

Legacy application/test lookup abstraction. CheckoutSession accepts supplied article facts; CheckoutPricing owns cross-article validation and calculation. No repository or remote callback enters the aggregate.

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

### Shared contract revision (2026-09-09)

Price wraps strictly positive Money; Money is ISO 4217, non-negative, two decimals half-up, maximum 999999999999.99.
Default quantities must be rejected before mutation/reconstitution. ProductCreated is raised by aggregate creation;
product-created v1 exposes only eventId, occurredOn, productId, amount, currency and initialStock.

---

## Failures

Named refusals. The session states its own rules; the use case states what it could not get hold of.

### CheckoutNotModifiable / CheckoutNotConfirmed

**Definition:** A session that is no longer open would be changed, or one that was never confirmed would be
completed.

**Type:** Domain failure (`DomainException`) · **Related terms:** `CheckoutSessionStatus`

---

### CheckoutStepNotCompleted / CheckoutStepOutOfOrder / CheckoutStepNotNavigable

**Definition:** A step is missing the data a later one needs; a step ahead of the current one was asked for; the
confirmation step was navigated to directly.

**Type:** Domain failure (`DomainException`) · **Related terms:** `CheckoutStep`

---

### EmptyCheckout / NothingToPay

**Definition:** A session would start without line items, or a payment would be arranged for a total of zero.

**Type:** Domain failure (`DomainException`) · **Related terms:** `CheckoutLineItem`, `CheckoutTotals`

---

### CheckoutValidation

**Definition:** At confirmation time a line item no longer passes validation — unavailable, stock ran out. Carries
the per-item result.

**Type:** Domain failure (`DomainException`) · **Related terms:** `CheckoutValidationResult`

---

### refuse an order changed since the review (CheckoutChangedSinceReviewException)

**Definition:** "Place Order" works the review out again; when its review fingerprint is not the one the customer
was shown, nothing is ordered or paid and the review shows the order as it is today. A price change names the
product (`priceChanged`, "The price of '…' has changed."); any other change — one made in another tab included —
names none (`orderChanged`, "Your order has changed since you reviewed it.").

**Type:** Domain failure (`DomainException`) · **Related terms:** `CheckoutReview`, `ReviewFingerprint`

---

### refuse an order whose payment differs from the review (AuthorizedAmountDiffersFromReviewException)

**Definition:** A checkout would be confirmed at a total other than the amount its payment was authorized for. The
order is placed at the total the review showed and the payment covers exactly that, so an order and a payment that
disagree are never confirmed; the page answers "Your order could not be placed. Please try again."

**Type:** Domain failure (`DomainException`) · **Related terms:** `CheckoutSession`, `PaymentSelection`, `CheckoutReview`

---

### CheckoutSessionNotFound · CartNotAvailable · CartNotActive · EmptyCart · ArticleNotAvailable · CheckoutItemsUnavailable

**Definition:** What the use case could not get hold of before a session exists, or could not address afterwards.
Session and cart lookups are scoped to the caller, so "not yours" reads as "not found" (ADR-036).
`CheckoutItemsUnavailable` is the start's refusal of a cart whose lines are unavailable or short of stock; it
carries their product ids, in cart order.

**Type:** Use-case failure (`UseCaseException`) · **Related terms:** `CartDataPort`, `CheckoutArticleDataPort`

---

### PaymentProviderNotFound · PaymentProviderUnavailable · PaymentInitiationFailed

**Definition:** The selected provider is unknown to the shop; it is unavailable (it says it is not taking payments,
cannot be reached, does not answer in time or answers outside its contract); or it refused to open a payment. The
provider's reason travels on the failure and into the log; each carries the `PaymentProviderId` it met, not the raw
input. A refusal or an unavailable provider at Place Order keeps the customer at the review, places no order and
gives the reserved stock back. The customer sees the shop's own text: "The payment was
refused. Please choose another way to pay." for a refusal, "The payment provider is not available right now. Please
try again later." when the provider is unavailable.

**Type:** Use-case failure (`UseCaseException`) · **Related terms:** `PaymentProviderRegistry`, `PaymentProvider`,
`PaymentResult`

---

### MailProviderUnavailable (MailProviderUnavailableException)

**Definition:** The mail provider did not accept a mail: it answered other than `202`, did not answer in time or
could not be reached. The failure carries the mail's key; the delivery that asked for the mail is tried again later,
under the same key. No customer sees it.

**Type:** Use-case failure (`UseCaseException`) · **Related terms:** `OrderConfirmationMailer`, Order confirmation

---

### ShippingOptionNotOffered (ShippingOptionNotOfferedException)

**Definition:** The shipping option the delivery form names is not one the shop offers. Carries the shipping option
id it met. The delivery step opens again with "This shipping method is not offered. Please choose another one."

**Type:** Use-case failure (`UseCaseException`) · **Related terms:** `ShippingOptionRegistry`, `ShippingOption`

---

### PaymentResult

**Definition:** What the payment provider made of a payment operation, as checkout understands it. A payment
request — sent at Place Order, after the review's checks and the stock reservation — carries the checkout session's
total amount and currency and the checkout reference to the provider; its outcome is one of:

- `SUCCEEDED` — an **authorized payment**: the provider accepted it and gave a payment reference. Only then
  does the session authorize the payment (`authorizePayment`), publish `PaymentSubmitted` and confirm.
- `REFUSED` — a **refused payment**: the provider answered and declined. The checkout stays at the review
  (`PaymentInitiationFailed`).
- `UNAVAILABLE` — an **unavailable provider**: no answer within 2 seconds, no connection, or an answer outside
  the provider's contract. The checkout stays at the review (`PaymentProviderUnavailable`).

**Type:** Concept (result of the `PaymentProvider` output port)

**Related terms:** `PaymentProviderId`, `PaymentSelection`, `PaymentSubmitted`.

**Notes:** The **payment stand-in** (`MockPaymentProvider`, id `mock`) authorizes every payment and takes
payments where no provider address is configured. The provider's contract has no confirmation; the REST adapter
answers it with `REFUSED` without asking the provider.

**Cancel a payment** (`cancelPayment`): releases an authorized payment at the provider by its payment reference
(`POST /payments/{payment reference}/cancel`, no body). `204` is `SUCCEEDED`, any other answer `REFUSED`, no answer
within 2 seconds `UNAVAILABLE`. The `PaymentProvider` port and its adapters offer it; no use case calls it.

---

### Payment cancellation (cancelPayment)

**Definition:** Releases an authorized payment at the payment provider by its payment reference, so the provider
no longer holds it for the checkout. The REST adapter sends `POST /payments/{payment reference}/cancel`; `204`
cancels the payment, any other answer, no answer in time or no connection counts as unavailable. No checkout step
asks for a cancellation.

**Type:** Concept (operation `cancelPayment` of the `PaymentProvider` output port)

**Related terms:** `PaymentResult`, `PaymentSelection`, Authorized amount.

### Checkout reference (merchantReference)

**Definition:** The checkout session's id as text, sent in every payment request of that checkout so the provider
and the shop can match a payment to it; a second attempt of the same checkout carries the same reference.

**Type:** Concept (field of the payment request to the provider)

**Related terms:** `CheckoutSessionId`, `PaymentResult`.

**Notes:** Kept apart from the **payment reference** the provider answers with (`PaymentSelection`) and from the
**order reference** of a completed session (`CheckoutSession.complete`). `merchantReference` is the provider's word;
it stays in the REST adapter's request, the `PaymentProvider` port passes the `CheckoutSessionId`.

## Features

The parts of the context's behaviour its people name; each groups what holds in a folder of the living spec.

### Payment (payment)

**Definition:** Paying for the checkout's order through a payment provider — authorized, refused or unanswered.

**Type:** Feature

### Checkout session (session)

**Definition:** Starting the checkout from the cart; nobody reaches another visitor's checkout.

**Type:** Feature

### Buyer details (buyerdetails)

**Definition:** Who is buying — entered by a guest, known for a signed-in customer.

**Type:** Feature

### Delivery (delivery)

**Definition:** The delivery address and the shipping option.

**Type:** Feature

### Ordering (ordering)

**Definition:** The review, placing the order and the confirmation — checked against what can still be sold.

**Type:** Feature

### Checkout steps (steps)

**Definition:** The steps' progress and order, going back to an earlier step, and the way to the cart without an open checkout.

**Type:** Feature

### Cart sync (cartsync)

**Definition:** The open checkout keeps its order while the cart changes.

**Type:** Feature

### Order confirmation (orderconfirmation)

**Definition:** Informing the buyer that the order went through: one mail with the template
`order-confirmation` to the buyer e-mail of a checkout with a placed order, carrying the order total.
Sent after the order's transaction committed, as a reaction to `CheckoutConfirmed`; an unavailable mail
provider delays it and never undoes the order.

**Type:** Feature

**Groups:** send the order confirmation (`sendorderconfirmation`).

**Operations:**
- send the order confirmation (`sendorderconfirmation`, through `SendOrderConfirmationInputPort`) — mails the placed
  checkout's total to its buyer e-mail through the mail provider, under one key per checkout
  (`order-confirmation-<checkout session id>`), so a repeated delivery reaches the buyer once; a session with no
  placed order is answered `NO_PLACED_ORDER`; a provider that does not accept the mail is
  `MailProviderUnavailableException`, and the confirmation is tried again

**Notes:** "Order confirmation" is the mail to the buyer — not the confirmation step (`CheckoutStep.CONFIRMATION`),
not `confirm` and not `CheckoutConfirmed`, which all mean placing the order.

**Related terms:** `CheckoutConfirmed`, `BuyerInfo`, Mail provider.

### Mail provider

**Definition:** The outside service that sends the shop's mails; it accepts a mail under a key and sends one
mail per key. Behind the output port `OrderConfirmationMailer`; with no provider address configured, a stand-in
accepts every mail and sends nothing.

**Type:** Concept

**Related terms:** Order confirmation, `MailProviderUnavailableException`.
