# checkout — payment

### Rule: The payment step chooses how to pay; it requests no payment

#### authorized-payment-moves-to-review
- Given a checkout session at the payment step
- And the payment provider authorizes payments
- When the customer chooses "Mock Payment (Test)" and presses "Continue to Review"
- Then the checkout shows the review step
- And the payment provider received no payment request
Test: dev.domaincentric.sample.ecommerce.checkout.PaymentAtPlaceOrderIntegrationTest#choosingHowToPayRequestsNoPayment

### Rule: A refused payment keeps the customer at the review and places no order

#### refused-payment-stays-at-payment
Title: A payment the provider refuses at Place Order keeps the customer at the review
- Given a checkout session at the review
- And the payment provider refuses payments
- When the customer presses "Place Order"
- Then the review stays open at `/checkout/review` with "The payment was refused. Please choose another way to pay."
- And no confirmation is shown
Test: dev.domaincentric.sample.ecommerce.checkout.ProviderPaymentIntegrationTest#refusedPaymentKeepsTheCustomerAtThePaymentStep

#### a-refused-payment-holds-no-stock
- Given a guest at the review with "Wooden Hexagon Desk Model" twice, whose product page reads "8 units available"
- And the payment provider refuses payments
- When the guest presses "Place Order"
- Then the review stays open at `/checkout/review` with "The payment was refused. Please choose another way to pay."
- And the product page of "Wooden Hexagon Desk Model" still reads "8 units available"
Test: dev.domaincentric.sample.ecommerce.checkout.ProviderPaymentIntegrationTest#aRefusedPaymentGivesTheReservedStockBack

#### the-provider-answers-per-checkout
- Given two customers, each with a checkout session at the review
- And the payment provider refuses the payment for the first customer's checkout reference only
- When both press "Place Order"
- Then the first customer stays at the review with "The payment was refused. Please choose another way to pay."
- And the second customer sees the confirmation
Test: dev.domaincentric.sample.ecommerce.checkout.CheckoutReferenceIntegrationTest#theProvidersAnswerForOneCheckoutTouchesNoOther

#### a-second-attempt-names-the-same-checkout
- Given a checkout session at the review
- And the customer's first "Place Order" was refused by the payment provider
- When the customer presses "Place Order" again
- Then the payment provider received two payment requests for that checkout session
- And both carry the same `merchantReference`
Test: dev.domaincentric.sample.ecommerce.checkout.CheckoutReferenceIntegrationTest#aSecondAttemptCarriesTheSameReference

### Rule: A provider that does not answer in time counts as unavailable

#### slow-provider-counts-as-unavailable
Title: A provider that answers Place Order only after 5 seconds counts as unavailable
- Given a checkout session at the review
- And the payment provider answers only after 5 seconds
- When the customer presses "Place Order"
- Then the review stays open at `/checkout/review` with "The payment provider is not available right now. Please try
  again later."
- And no confirmation is shown
Test: dev.domaincentric.sample.ecommerce.checkout.ProviderPaymentIntegrationTest#slowProviderCountsAsUnavailable

### Rule: The payment step offers the payment providers that are available

#### payment-step-offers-the-provider
- Given a guest who submitted the delivery step with "Standard Shipping"
- When the guest opens the payment step
- Then it shows under "Select Payment Method" exactly one choice, "Mock Payment (Test)"
- And a "Back" link and a "Continue to Review" button
Test: dev.domaincentric.sample.ecommerce.checkout.ChooseHowToPayIntegrationTest#thePaymentStepOffersMockPaymentWithABackLinkAndAButton

### Rule: The chosen shipping price is added to the order total

#### payment-step-totals-with-standard-shipping
- Given a guest who started the checkout with "Domain-Driven Design" once and chose "Standard Shipping"
- When the guest opens the payment step
- Then its "Order Summary" shows "Domain-Driven Design x1 54.99 EUR"
- And Subtotal "54.99 EUR", Shipping "4.99 EUR" and Total "59.98 EUR"
Test: dev.domaincentric.sample.ecommerce.checkout.ChooseHowToPayIntegrationTest#standardShippingIsAddedToTheOrderTotal

#### payment-step-totals-with-express-shipping
- Given a guest who started the checkout with "Domain-Driven Design" once and chose "Express Shipping"
- When the guest opens the payment step
- Then it shows Subtotal "54.99 EUR", Shipping "9.99 EUR" and Total "64.98 EUR"
Test: dev.domaincentric.sample.ecommerce.checkout.ChooseHowToPayIntegrationTest#expressShippingIsAddedToTheOrderTotal

### Rule: Choosing a provider starts the payment and moves on to the review

#### choosing-the-provider-opens-the-review
- Given a guest on the payment step
- When the guest chooses "Mock Payment (Test)" and presses "Continue to Review"
- Then the review step opens at `/checkout/review`
Test: dev.domaincentric.sample.ecommerce.e2e.ChooseHowToPayE2ETest#choosingTheOfferedProviderOpensTheReviewStep

#### unknown-provider-is-refused
- Given a guest on the payment step
- When the payment form is sent with the provider "paypal"
- Then the payment step opens again with the error "This way to pay is not offered. Please choose another one."
Test: dev.domaincentric.sample.ecommerce.checkout.ChooseHowToPayIntegrationTest#aProviderTheShopDoesNotOfferOpensThePaymentStepAgainWithTheRefusal

### Rule: Place Order has the provider authorize the total the review showed, then confirms the order

#### place-order-authorizes-the-reviewed-total
Title: Place Order has the payment provider authorize the total the review showed and opens the confirmation
- Given a guest at the review with "Domain-Driven Design" once and "Standard Shipping", Total "59.98 EUR"
- And the payment provider authorizes payments
- When the guest presses "Place Order"
- Then the confirmation page opens at `/checkout/confirmation` with "Order Total" "59.98 EUR"
- And the payment provider received one payment request, of "59.98"
Test: dev.domaincentric.sample.ecommerce.e2e.ProviderPaymentE2ETest#placeOrderAuthorizesTheReviewedTotal

#### the-payment-request-names-its-checkout
- Given a checkout session at the review
- And the payment provider authorizes payments
- When the customer presses "Place Order"
- Then the payment provider received one payment request for the checkout session's total
- And that request carries the checkout reference, the checkout session's id as text, as `merchantReference`
Test: dev.domaincentric.sample.ecommerce.checkout.PaymentAtPlaceOrderIntegrationTest#placeOrderRequestsThePaymentWithTheCheckoutReference

#### two-checkouts-send-two-references
- Given two customers, each with a checkout session at the review
- When both press "Place Order"
- Then the payment provider received two payment requests with two different `merchantReference` values
Test: dev.domaincentric.sample.ecommerce.checkout.CheckoutReferenceIntegrationTest#twoCheckoutsSendTwoDifferentReferences

### Rule: An order Place Order refuses before the payment requests no payment

#### place-order-refused-for-stock-requests-no-payment
- Given a guest at the review with "Wooden Hexagon Desk Model" five times
- And another visitor has since bought it five times, leaving 3
- When the guest presses "Place Order"
- Then the review stays open at `/checkout/review` with "Not enough stock left for 'Wooden Hexagon Desk Model'.
  Please adjust your order."
- And the payment provider received no payment request
Test: dev.domaincentric.sample.ecommerce.checkout.PaymentAtPlaceOrderIntegrationTest#anOrderRefusedForStockRequestsNoPayment
