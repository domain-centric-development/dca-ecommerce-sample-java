---
id: the-payment-is-authorized-at-place-order-accept-1
story: the-payment-is-authorized-at-place-order
stage: document
kind: acceptance
asked: 2026-10-21T15:25:19Z
digest: e7b5bf2c7b01b37ef41195c153fddf2499229433868167e76c1f3dae4ddbea21
---

# Accept the-payment-is-authorized-at-place-order?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- authorized-payment-moves-to-review: Given a checkout session at the payment step; And the payment provider authorizes payments; When the customer chooses "Mock Payment (Test)" and presses "Continue to Review"; Then the checkout shows the review step; And the payment provider received no payment request — `dev.domaincentric.sample.ecommerce.checkout.PaymentAtPlaceOrderIntegrationTest#choosingHowToPayRequestsNoPayment`
- place-order-authorizes-the-reviewed-total: Given a guest at the review with "Domain-Driven Design" once and "Standard Shipping", Total "59.98 EUR"; And the payment provider authorizes payments; When the guest presses "Place Order"; Then the confirmation page opens at `/checkout/confirmation` with "Order Total" "59.98 EUR"; And the payment provider received one payment request, of "59.98" — `dev.domaincentric.sample.ecommerce.e2e.ProviderPaymentE2ETest#placeOrderAuthorizesTheReviewedTotal`
- the-payment-request-names-its-checkout: Given a checkout session at the review; And the payment provider authorizes payments; When the customer presses "Place Order"; Then the payment provider received one payment request for the checkout session's total; And that request carries the checkout reference, the checkout session's id as text, as `merchantReference` — `dev.domaincentric.sample.ecommerce.checkout.PaymentAtPlaceOrderIntegrationTest#placeOrderRequestsThePaymentWithTheCheckoutReference`
- two-checkouts-send-two-references: Given two customers, each with a checkout session at the review; When both press "Place Order"; Then the payment provider received two payment requests with two different `merchantReference` values — `dev.domaincentric.sample.ecommerce.checkout.CheckoutReferenceIntegrationTest#twoCheckoutsSendTwoDifferentReferences`
- refused-payment-stays-at-payment: Given a checkout session at the review; And the payment provider refuses payments; When the customer presses "Place Order"; Then the review stays open at `/checkout/review` with "The payment was refused. Please choose another way to pay."; And no confirmation is shown — `dev.domaincentric.sample.ecommerce.checkout.ProviderPaymentIntegrationTest#refusedPaymentKeepsTheCustomerAtTheReview`
- a-refused-payment-holds-no-stock: Given a guest at the review with "Wooden Hexagon Desk Model" twice, whose product page reads "8 units available"; And the payment provider refuses payments; When the guest presses "Place Order"; Then the review stays open at `/checkout/review` with "The payment was refused. Please choose another way to pay."; And the product page of "Wooden Hexagon Desk Model" still reads "8 units available" — `dev.domaincentric.sample.ecommerce.checkout.ProviderPaymentIntegrationTest#aRefusedPaymentGivesTheReservedStockBack`
- the-provider-answers-per-checkout: Given two customers, each with a checkout session at the review; And the payment provider refuses the payment for the first customer's checkout reference only; When both press "Place Order"; Then the first customer stays at the review with "The payment was refused. Please choose another way to pay."; And the second customer sees the confirmation — `dev.domaincentric.sample.ecommerce.checkout.CheckoutReferenceIntegrationTest#theProvidersAnswerForOneCheckoutTouchesNoOther`
- a-second-attempt-names-the-same-checkout: Given a checkout session at the review; And the customer's first "Place Order" was refused by the payment provider; When the customer presses "Place Order" again; Then the payment provider received two payment requests for that checkout session; And both carry the same `merchantReference` — `dev.domaincentric.sample.ecommerce.checkout.CheckoutReferenceIntegrationTest#aSecondAttemptCarriesTheSameReference`
- slow-provider-counts-as-unavailable: Given a checkout session at the review; And the payment provider answers only after 5 seconds; When the customer presses "Place Order"; Then the review stays open at `/checkout/review` with "The payment provider is not available right now. Please try again later."; And no confirmation is shown — `dev.domaincentric.sample.ecommerce.checkout.ProviderPaymentIntegrationTest#slowProviderCountsAsUnavailable`
- place-order-refused-for-stock-requests-no-payment: Given a guest at the review with "Wooden Hexagon Desk Model" five times; And another visitor has since bought it five times, leaving 3; When the guest presses "Place Order"; Then the review stays open at `/checkout/review` with "Not enough stock left for 'Wooden Hexagon Desk Model'. Please adjust your order."; And the payment provider received no payment request — `dev.domaincentric.sample.ecommerce.checkout.PaymentAtPlaceOrderIntegrationTest#anOrderRefusedForStockRequestsNoPayment`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-21T16:19:50Z
