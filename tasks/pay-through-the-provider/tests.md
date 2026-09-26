# Tests — pay-through-the-provider

Carried in-session. The profile names `carrier.test: e2e-testing`, and its craft was followed here:
page objects, `data-test` selectors, no sleeps, and the shop started by the suite. No knowledge
question came up that would have decided anything, so `dca-knowledge` was not asked.

<!-- gate:tests -->
| criterion | test |
| --- | --- |
| authorized-payment-moves-to-review | dev.domaincentric.sample.ecommerce.e2e.ProviderPaymentE2ETest#authorizedPaymentMovesTheCheckoutToReview |
| refused-payment-stays-at-payment | dev.domaincentric.sample.ecommerce.checkout.ProviderPaymentIntegrationTest#refusedPaymentKeepsTheCustomerAtThePaymentStep |
| slow-provider-counts-as-unavailable | dev.domaincentric.sample.ecommerce.checkout.ProviderPaymentIntegrationTest#slowProviderCountsAsUnavailable |

## Files
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ShopUnderTest.java
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ProviderPaymentE2ETest.java
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/ReviewPage.java
- src/test-integration/java/dev/domaincentric/sample/ecommerce/checkout/ProviderPaymentIntegrationTest.java

## Notes
- `ProviderPaymentE2ETest#authorizedPaymentMovesTheCheckoutToReview`: fails on "the payment
  provider received one payment request ==> expected: <1> but was: <0>". The shop still pays
  through the stand-in, which calls nothing. After the count, the test reads the total through
  `ReviewPage#total()`, which uses `data-test="review-total"`. That attribute does not exist yet:
  the build stage puts it on the total amount span in `templates/checkout/review.pug`
  (`.order-summary__total`, the span holding `#{total} #{currencyCode}`). The page object splits
  the text on whitespace into an amount and a currency code. It compares these with the request
  body's `amount` (as a decimal, string or number) and `currency`.
- `ProviderPaymentIntegrationTest#refusedPaymentKeepsTheCustomerAtThePaymentStep` and
  `#slowProviderCountsAsUnavailable`: both fail on `expected: "/checkout/payment" but was:
  "/checkout/review"` for the form post's redirect. The provider address is set, but nothing reads
  it yet, and the stand-in authorizes the payment.
- "Stays at the payment step" is asserted twice: the form post redirects to `/checkout/payment`,
  and `GET /checkout/review` redirects to `/checkout/payment` because the step is still closed. The
  message is read from `[data-test=payment-error-message]` on the payment page the redirect leads
  to, rendered with the redirect's flash attributes.
- The integration tests set the provider address as `checkout.payment-provider.base-url`, the
  plan's working key, through `@DynamicPropertySource`. The e2e suite sets it through
  `ShopUnderTest`'s start argument. If the build stage names the key differently, it must change
  both places.
- The stub for the authorized case answers `201` with `{"reference": "…"}`. The request body the
  test reads is `{"amount": …, "currency": "…"}`, the shape the plan assumes.
- `ShopUnderTest` (a changed test backed by decision pay-through-the-provider-01) now starts a
  WireMock provider on a free port before the shop. It passes that address to the shop and
  arranges an authorizing default answer. It exposes the stub through `paymentProvider()`, which is
  empty when `-De2e.baseUrl` points at a shop started elsewhere. The happy path then skips through
  an assumption. The whole browser suite ran with the change: 23 tests, 1 failed (the new one), 1
  skipped (the embedded-only test, as before).
- Two more integration tests, not in the table, cover adapter cases that no scenario names:
  - `ProviderPaymentIntegrationTest#providerServerErrorCountsAsUnavailable`: the provider answers
    `500`.
  - `ProviderPaymentIntegrationTest#droppedConnectionCountsAsUnavailable`: the provider resets the
    connection.

  Both expect the unavailable message. Both currently fail on the same redirect assertion.
- unit tests: none. The plan names no aggregate or value-object invariant (no aggregate changes).
  The use case's rule "an unavailable initiation raises `PaymentProviderUnavailableException`"
  depends on a `PaymentResult` shape that the build stage chooses. The slow-provider and the two
  extra integration tests cover it through the wired application.
- No production code, build file or runner configuration was changed. No stub was needed to
  compile.
