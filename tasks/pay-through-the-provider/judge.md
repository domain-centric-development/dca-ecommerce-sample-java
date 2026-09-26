# Judge — pay-through-the-provider

## Verdict
verdict: pass

## Perspectives covered
- ddd: `review-ddd` (skill, named by `review.ddd:`)
- hexagonal: `review-hexagonal` (skill, named by `review.hexagonal:`)
- clean-code: `review-clean-code` (skill, named by `review.clean-code:`)
- dca (added by `reviews: dca`): `dca-review` (skill, named by `review.dca:`)

`dca-knowledge` is named in the profile, but no finding needed a source to settle it. The plan had
already settled the two pattern questions (where the ACL sits, one type per outcome) and cited
their nodes, so no node was asked here.

This round read only the story, `plan.md`, `tests.md`, `build.md`, `tidy.md`, `.verify/story.diff`,
`project/product.md`, `project/tech.md` and the epic. It opened only these source lines: the
checkout glossary, `SubmitPaymentUseCase.java`, `PaymentInitiationFailedException.java`, and a
search for `confirmPayment`, `cancelPayment` and `isAvailable()`.

## Confirmed defects
| Perspective | File:line | Severity | Defect | Fix |
| --- | --- | --- | --- | --- |
| clean-code / ddd | `src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/outgoing/payment/RestPaymentProvider.java:117-124`, `src/main/java/dev/domaincentric/sample/ecommerce/checkout/application/shared/PaymentProvider.java:88,97` | minor | `failure(..)` now always produces `Outcome.REFUSED`, which the enum describes as "The provider answered and refused the operation". The REST adapter uses it for confirmation and cancellation, which the contract does not have, so the provider was never asked. The outcome says something the adapter did not observe. Nothing reads this outcome today: `confirmPayment` has no caller, and `cancelQuietly` only logs. | Rename `failure(..)` to `refused(..)` so the name says the outcome. For the two operations outside the contract, either word `REFUSED`'s JavaDoc to cover "declined, or not offered by the provider" or give them their own outcome. |
| clean-code | `src/main/java/dev/domaincentric/sample/ecommerce/checkout/application/shared/PaymentInitiationFailedException.java:9-10` | minor | The JavaDoc says "the customer needs to see what the provider said". Since `PaymentPageController` (diff lines 128-130), the customer sees a fixed message instead. The comment now describes behaviour that no longer exists. `build.md` and `tidy.md` both passed it on. | Document stage: say that the reason is kept for logs and callers, and that the page shows its own message. Align the glossary entry "PaymentProviderNotFound · PaymentProviderUnavailable · PaymentInitiationFailed" (glossary.md:494-495) with it. |
| ddd | `src/main/java/dev/domaincentric/sample/ecommerce/checkout/domain/glossary.md:264-265` | minor | `PaymentProviderId` says "The sample registers a single one, `mock`". With a provider address configured, the single provider is `provider` (`RestPaymentProvider.java:43`). | Document stage: name both ids and say which configuration selects each. |

No blocker and no major finding.

## Considered and dropped
- **Adapter mechanics.** Translation is in the outgoing adapter only: `402`→refused, `201` with a
  reference→success, and everything else, a timeout or a transport failure→unavailable. The wire
  records `PaymentRequest` and `PaymentAuthorization` are package-private in the adapter, and the
  port sees only `PaymentResult`. This follows the ACL as designed in `project/domain.md` and
  `project/tech.md` `## Integrations`.
- **Transactions.** The remote call stays outside the transaction (`SubmitPaymentUseCase.java:65-91`),
  and the transaction wraps only load/submit/save/publish (`:99-110`). No finding.
- **Failure translation in `PaymentPageController`.** It catches the two named failures
  (diff lines 128-133) before the existing base-type catch. The page controller is where this page
  already translates failures; there is no second site and no blanket catch.
- **`CheckoutPaymentConfiguration` (infrastructure) imports `PaymentProviderProperties` (adapter).**
  This is the same pairing as `SecurityConfiguration`/`JwtProperties`, and the architecture suite
  passes. The rule suite owns this question.
- **Both adapters repeat the key `checkout.payment-provider.base-url` in `@ConditionalOnExpression`.**
  Tidy considered it and kept it for readability. There are two occurrences, so the Rule of Three
  does not apply.
- **The REST adapter's `isAvailable()` always returns `true`.** The contract has no availability
  query, and a timeout surfaces on the request instead. The plan states this.
- **Real payment against `project/product.md` `## Not part of the product` ("Real payment").** The
  shop ships no provider address (`PaymentProviderProperties` is not set in `application.yml`), so
  the product still pays through the stand-in, and `project/tech.md` lists the integration. The
  product sentence "a stand-in in the sample" becomes inexact. The plan already gave that to the
  document stage.
- **Out of scope.** No webhook, refund or cancellation call is delivered. `cancelPayment` calls
  nothing.
- **`BasePage.java`, `BaseE2ETest.java`, `README.md`, `gradle/plugins/test-e2e.gradle` in the diff.**
  These come from commit 33236c6 (decision pay-through-the-provider-01), which was made by hand
  after the baseline, not by a stage. `BaseE2ETest` is backed in `## Changed tests`. `BasePage` is a
  page object with the same one-line change, backed by the same decision. `ShopUnderTest` changes
  only as its backing line says: the stub starts, and the start argument gains the address.
- **Wildcard import `e2e.pages.*` in `ProviderPaymentE2ETest`.** Five existing e2e tests do the same.
  This is style.
- **Twin checkpoint.** `project/tech.md` makes this sample the reference that the .NET sample
  follows. The twin lives in another repository and is not part of this story's scope.
- **Journey test up to the outcome event.** The epic's journey is still `open`, so no journey test
  exists for this story to stop short of. The happy path reaches the review step, which opens only
  after `CheckoutSession.submitPayment` has published `PaymentSubmitted`.

## Criteria re-checked
- **authorized-payment-moves-to-review: met.**
  - The e2e test drives the browser to the payment step. It arranges `201` on the suite's stub,
    clears the recorded requests, and pays.
  - It asserts the review page. The `ReviewPage` constructor waits for `/checkout/review`, and the
    test checks the buyer's email.
  - It asserts exactly one `POST /payments`, with `amount` and `currency` equal to the total under
    `data-test="review-total"`.
  - The level is right: the happy path runs in the browser, and the adapter is not mocked.
- **refused-payment-stays-at-payment: met.**
  - The integration test runs through the real `RestPaymentProvider`, against WireMock answering
    `402`.
  - It asserts the redirect to `/checkout/payment`, and that `/checkout/review` still redirects
    back, so the step is unchanged.
  - It asserts the exact message in `[data-test=payment-error-message]`.
- **slow-provider-counts-as-unavailable: met.**
  - The stub answers after 5 seconds, and the 2-second read timeout (`RestPaymentProvider.java:45,60`)
    turns that into unavailable.
  - The test makes the same stay-at-step assertions and checks the exact unavailable message.
  - Two further tests cover `500` and a dropped connection.
