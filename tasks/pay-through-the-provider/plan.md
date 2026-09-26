# Plan — pay-through-the-provider: Pay through the payment provider

Carried in-session: the profile names no `carrier.plan`. The profile's knowledge skill
(`knowledge: dca-knowledge`) was asked two pattern questions. Its answers come from its vendored
catalog under `.claude/skills/dca-knowledge/catalog/`, and both nodes are `review: draft`:

- `/recipe/add-an-anti-corruption-layer.md`: the port is declared in `application/shared/` in the
  context's own terms. The outgoing adapter calls the foreign contract, and "all translation lives
  here". Foreign types stay inside the adapter. This project puts outgoing adapters in
  `adapter/outgoing/<topic>/` (for example `adapter/outgoing/payment/`), not in an `acl` package, so
  the plan follows the project.
- `/decision/failure-channel-exception-or-result.md`: "one type per outcome — the outcomes are
  distinguishable types, not one type carrying a string". It also says an incoming adapter decides
  the answer the caller sees, and no layer further in does.

Decision pay-through-the-provider-01 answered this: the end-user suite starts the shop itself, in
the test process and on a free port. The payment provider's stub belongs to the suite: it starts
with the shop, and the shop's provider address points at it. Each test arranges the stub's answers.
The story no longer names 20.00 EUR. The happy path asserts one payment request for the checkout
session's total (story diff: the "Given" and "And … received" lines of
`authorized-payment-moves-to-review`). The plan changes `ShopUnderTest` and the happy-path e2e test
accordingly.

## Context

The story belongs to **`checkout`**, a Core subdomain. `project/domain.md` gives this context
"Checkout process, payment orchestration, order confirmation" and designs the relationship
*Payment Service Provider → `checkout`* as "ACL / REST (outbound)". It is realised "behind the
caller-owned `PaymentProvider` port, with a mock adapter in place of a real gateway".

The code declares the same relationship:

- `checkout/package-info.java` declares
  `@ExternalUpstream(name = "Payment Service Provider", translation = ANTI_CORRUPTION_LAYER,
  interaction = OUTBOUND, protocol = "REST")`.
- `docs/architecture/context-map.md` shows `checkout -->|"ACL / REST"| ext_payment_service_provider`
  with status *implemented*.

The designed map and the generated one agree on this relationship. The story adds no context and
no relationship. It puts a real outgoing adapter behind the existing port, beside the stand-in
`MockPaymentProvider`.

**Findings:** these do not block the story, but the build and document stages must follow them up.

- Both maps describe the relationship as "a mock adapter in place of a real gateway":
  - `project/domain.md`, table row "Payment Service Provider".
  - The `@ExternalUpstream` `rationale` in `checkout/package-info.java`, from which
    `docs/architecture/context-map.md` is generated.
  - The same annotation's `exchanges = "payment operations (initiate, confirm, refund)"`.

  After this story, the stand-in is only the fallback. The declaration's `rationale` changes in the
  build stage, because the declaration is code. The generated map is regenerated with it:
  `ContextMapDocumentationTest` fails on a stale file. The designed map in `project/domain.md` is
  aligned in the document stage.
- `project/product.md` `## Not part of the product` lists "Real payment", and `## Qualities` says
  "payment is handed to a provider (a stand-in in the sample)". `project/tech.md` `## Integrations`
  names the provider contract and the stand-in fallback. The story stays within that: the shop
  ships no provider address, so a local run still uses the stand-in (story assumption 2). Only the
  address makes the shop call a provider. There is no contradiction to stop on. The product
  sentence becomes inexact, though, and the document stage should word it as "a stand-in unless a
  provider address is configured".

**Surface:** the actor is the customer (shopper), and the way in exists already: the payment step
page `GET/POST /checkout/payment` (`PaymentPageController.java`, `showPaymentForm` and
`submitPayment`). The page shows errors in `.alert.alert--error(data-test="payment-error-message")`
(`templates/checkout/payment.pug` line 20). Checkout has no REST API resource: `SubmitPaymentInputPort`
is used only by `PaymentPageController`. So no new surface is needed.

**Decisions from the project description** (`project/tech.md` `## Integrations`):

- A payment is `POST /payments` with the amount and the currency.
- `201` with a payment reference means authorized, and `402` means refused.
- No answer within **2 seconds** counts as unavailable.
- Where the provider's address is not configured, the stand-in inside the sample takes payments.

`## Persistence` is unchanged: nothing about the payment is stored beyond the existing
`PaymentSelection`. `## Frontend approach` is unchanged too: server-rendered Pug, no client
framework.

## Changes

| Element | Kind | Location | New or changed |
|---|---|---|---|
| `PaymentProvider.PaymentResult` | output-port result | `checkout/application/shared/PaymentProvider.java` | changed. A third outcome, *unavailable*, beside success and failure (refused), as a distinguishable outcome and not an error string. The `success(..)` and `failure(..)` factories stay. |
| `SubmitPaymentUseCase` | use case (input port `SubmitPaymentInputPort`) | `checkout/application/checkoutcompletion/submitpayment/` | changed. An *unavailable* initiation result raises the existing `PaymentProviderUnavailableException`. A refused one still raises `PaymentInitiationFailedException`. Payment is still initiated outside the transaction. |
| `PaymentProviderUnavailableException`, `PaymentInitiationFailedException` | use-case failures (`UseCaseException`) | `checkout/application/shared/` | unchanged types, reused |
| HTTP payment provider adapter (working name `RestPaymentProvider`) | outgoing adapter implementing `PaymentProvider` (ACL to the provider's REST contract) | `checkout/adapter/outgoing/payment/` | **new**. It sends `POST {base-url}/payments` with amount and currency, and a 2-second timeout. It translates `201` + reference to success, `402` to refused, and a timeout, connection failure or any other answer to unavailable. The provider's wire types stay inside the adapter. It is active only when the provider address is configured. |
| Provider address setting (working key `checkout.payment-provider.base-url`) | configuration property of the adapter | `checkout/adapter/outgoing/payment/` (a `@ConfigurationProperties` record, as `account/adapter/outgoing/security/JwtProperties.java` does) | **new**. It is not set in `application.yml`. |
| `MockPaymentProvider` (the stand-in) | outgoing adapter | `checkout/adapter/outgoing/payment/MockPaymentProvider.java` | changed. It is active only when no provider address is configured (story assumption 2, `project/tech.md`). |
| `PaymentPageController.submitPayment` | incoming web adapter, the context's translation site for the payment page | `checkout/adapter/incoming/web/checkoutcompletion/` | changed. It maps `PaymentInitiationFailedException` to "The payment was refused. Please choose another way to pay." and `PaymentProviderUnavailableException` to "The payment provider is not available right now. Please try again later.", then redirects to the payment step as today. Other failures keep the current `e.getMessage()` path. |
| `@ExternalUpstream` Payment Service Provider (outbound) | strategic declaration | `checkout/package-info.java` | changed. The `rationale` (and `exchanges`) describe the REST adapter with the stand-in as fallback. `docs/architecture/context-map.md` is regenerated from it. |
| Review page total | template | `templates/checkout/review.pug`, `.order-summary__total` | changed. A `data-test` attribute on the total amount, so the happy path can read the session's total (`project/product.md` `## Look and feel`: "every element a test addresses carries a stable `data-test` attribute"). |
| `PaymentSubmitted` | domain event | `checkout/domain/event/PaymentSubmitted.java` | unchanged. It is the epic's outcome event and is still published by `CheckoutSession.submitPayment` once the provider authorized the payment. |

No aggregate changes: `CheckoutSession.submitPayment(PaymentSelection)` and `assertReadyForPayment()`
are reused as they are. One aggregate changes per transaction, as today
(`SubmitPaymentUseCase` lines 96–107).

## Acceptance criteria

- **authorized-payment-moves-to-review** (happy path):
  - Given a checkout session at the payment step
  - And the payment provider authorizes payments
  - When the customer pays through the payment provider
  - Then the checkout shows the review step
  - And the payment provider received one payment request for the checkout session's total

  The provider stub answers `POST /payments` with `201` and a reference. The customer selects the
  provider on the payment page and continues. The browser lands on `/checkout/review`. The stub
  recorded exactly one `POST /payments`, whose amount and currency equal the total the review page
  shows.

  → level: **e2e**. Playwright, `./gradlew test-e2e --rerun`, against the shop and the stub that
  `ShopUnderTest` starts (Decision pay-through-the-provider-01).

- **refused-payment-stays-at-payment**:
  - Given a checkout session at the payment step
  - And the payment provider refuses payments
  - When the customer pays through the payment provider
  - Then the checkout stays at the payment step
  - And the customer sees "The payment was refused. Please choose another way to pay."

  The stub answers `POST /payments` with `402`. The form post to `/checkout/payment` redirects back
  to `/checkout/payment`, and the session's current step is still PAYMENT. The payment page then
  shows exactly that text in `payment-error-message`.

  → level: **integration**. `src/test-integration` (`./gradlew test-integration --rerun`): a
  `@SpringBootTest` with MockMvc over the payment page. It uses a WireMock provider on a dynamic
  port and the provider address set to it.

- **slow-provider-counts-as-unavailable**:
  - Given a checkout session at the payment step
  - And the payment provider answers only after 5 seconds
  - When the customer pays through the payment provider
  - Then the checkout stays at the payment step
  - And the customer sees "The payment provider is not available right now. Please try again later."

  The stub answers `POST /payments` with a fixed delay of 5 seconds. The shop gives up after the
  2 seconds `project/tech.md` names, redirects back to `/checkout/payment`, and the session stays
  at PAYMENT. The payment page shows exactly that text in `payment-error-message`.

  → level: **integration**. Same source set and runner as above.

## Changed tests

| Test file | Backed by |
| --- | --- |
| `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ShopUnderTest.java` | Decision pay-through-the-provider-01. The suite's shop starts together with a payment-provider stub, and its provider address points at that stub (the `SpringApplication.run(…, "--server.port=0")` line gains the address). |
| `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/BaseE2ETest.java` | Decision pay-through-the-provider-01. The end-user suite starts the shop itself, in the test process and on a free port (commit 33236c6, after the plan gate's baseline): `BASE_URL` comes from `ShopUnderTest.baseUrl()` instead of the `e2e.baseUrl` default `http://localhost:8080`. |

No other existing test contradicts the story:

- Integration tests that pay with the stand-in run without a provider address, so the stand-in
  stays in place for them. That covers `CheckoutFlowIntegrationTest` line 159,
  `ArticleDataFlowIntegrationTest` line 665 and `CheckoutOwnershipIntegrationTest` line 98, all with
  `"mock"`.
- `SubmitPaymentUseCaseTest` uses only `PaymentResult.success(..)`, and that factory stays.
- The browser checkout flows (`CheckoutGuestE2ETest`, `CheckoutLoginE2ETest`,
  `CheckoutSnapshotE2ETest`) select the first provider and expect the review step. That still holds
  because the suite's stub authorizes by default.

## Files

- `src/main/java/dev/domaincentric/sample/ecommerce/checkout/application/shared/PaymentProvider.java` — changes: `PaymentResult` gains the *unavailable* outcome.
- `src/main/java/dev/domaincentric/sample/ecommerce/checkout/application/checkoutcompletion/submitpayment/SubmitPaymentUseCase.java` — changes: an unavailable initiation raises `PaymentProviderUnavailableException` (lines 85–88 today).
- `src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/outgoing/payment/RestPaymentProvider.java` (working name) — changes: new REST adapter.
- `src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/outgoing/payment/PaymentProviderProperties.java` (working name) — changes: new, the provider address.
- `src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/outgoing/payment/MockPaymentProvider.java` — changes: active only without a provider address.
- `src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/incoming/web/checkoutcompletion/PaymentPageController.java` — changes: the two customer messages (catch block at lines 152–158 today).
- `src/main/java/dev/domaincentric/sample/ecommerce/checkout/package-info.java` — changes: `@ExternalUpstream` outbound `rationale` and `exchanges`.
- `docs/architecture/context-map.md` — changes: regenerated from the declaration, never hand-edited.
- `src/main/resources/templates/checkout/review.pug` — changes: `data-test` on the total amount.
- `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ShopUnderTest.java` — changes: starts a WireMock stub (`org.wiremock:wiremock-standalone`, reachable through `testE2eImplementation.extendsFrom(testImplementation)` in `gradle/plugins/test-e2e.gradle` and `gradle/plugins/test-unit.gradle` line 8). It points the shop's provider address at the stub, arranges an authorizing default answer, and exposes the stub so a test can arrange answers and count requests.
- `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ProviderPaymentE2ETest.java` (working name) — changes: new, the happy path.
- `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/ReviewPage.java` — changes: reads the total.
- `src/test-integration/java/dev/domaincentric/sample/ecommerce/checkout/ProviderPaymentIntegrationTest.java` (working name) — changes: new, the refused and the slow scenarios.
- `src/test-integration/java/dev/domaincentric/sample/ecommerce/infrastructure/HttpStubSmokeTest.java` — read: how a WireMock stub is registered (`WireMockExtension`, `dynamicPort()`).
- `src/test-integration/java/dev/domaincentric/sample/ecommerce/account/infrastructure/CsrfProtectionIntegrationTest.java` — read: driving pages with MockMvc (cookies, `_csrf` field from the page, its own H2 URL because MockMvc requests commit).
- `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/CheckoutGuestE2ETest.java` — read: the page-object flow to the payment step.
- `src/test/java/dev/domaincentric/sample/ecommerce/checkout/application/submitpayment/SubmitPaymentUseCaseTest.java` — read: the recording provider and single-provider registry, which a unit test of the unavailable outcome can mirror.
- `src/main/java/dev/domaincentric/sample/ecommerce/account/adapter/outgoing/security/JwtProperties.java` — read: how an adapter declares its configuration properties.
- `src/main/java/dev/domaincentric/sample/ecommerce/checkout/domain/glossary.md` — read: `PaymentSelection`, `PaymentSubmitted`, and the failure entry "PaymentProviderNotFound · PaymentProviderUnavailable · PaymentInitiationFailed". That entry says "the provider's own reason travels through unchanged", which the page no longer shows. It is for the document stage.

## Glossary proposals

- Payment request: the shop's `POST /payments` to the payment provider, carrying the checkout session's total amount and currency.
- Authorized payment: a payment request the provider accepted with a payment reference (`201`). Only then does the session record its `PaymentSelection` and publish `PaymentSubmitted`.
- Refused payment: a payment request the provider declined (`402`). The checkout stays at the payment step (`PaymentInitiationFailed`).
- Unavailable provider: a provider that gives no answer within 2 seconds, or cannot be reached. The checkout stays at the payment step (`PaymentProviderUnavailable`).
- Payment stand-in: the provider inside the sample (`MockPaymentProvider`) that authorizes every payment and is used where no provider address is configured.

## Open assumptions

- answered (story): the provider contract is the one in `project/tech.md` `## Integrations`.
- answered (story): without a configured provider address, the stand-in stays, so a local run
  needs no provider.
- The request and response field names are not fixed by `project/tech.md`, which names only
  "the amount and currency" and "a payment reference". The plan assumes a JSON body
  `{"amount": "<decimal>", "currency": "<ISO code>"}` and a `201` body `{"reference": "<id>"}`.
  The tests arrange and assert exactly this shape.
- A connection failure, and any status other than `201` and `402`, count as *unavailable*. The
  contract names only the timeout. The customer cannot act differently on these cases, and no
  criterion covers them.
- `confirmPayment` and `cancelPayment` have no counterpart in the provider contract, and refunds and
  cancellations are out of scope. In the REST adapter they answer with a failure outcome and call
  nothing. As a result, `SubmitPaymentUseCase.cancelQuietly` only logs when a session rejects an
  authorized payment afterwards. A cancellation endpoint is a later story.
- `isAvailable()` of the REST adapter answers `true`, because the contract has no availability
  query. Unavailability shows up on the payment request itself.
- The REST adapter needs a provider id and a display name. The working values are `provider` and
  "Payment provider". The criteria name neither, and the tests select the first provider as the
  existing ones do.
- When the suite drives a shop started elsewhere (`-De2e.baseUrl`, the compose `e2e` service),
  there is no stub the test controls. The happy-path test then skips through a JUnit assumption,
  the way the `e2e.embedded` tests do. The gate's `test-e2e` run starts the shop itself.
- The existing browser checkout flows need the suite's stub to authorize by default. Without that,
  they would stop at the payment step.
