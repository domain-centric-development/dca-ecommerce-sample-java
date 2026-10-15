# checkout — payment

### Rule: A payment the provider authorizes moves the checkout on

#### authorized-payment-moves-to-review
- Given a checkout session at the payment step
- And the payment provider authorizes payments
- When the customer pays through the payment provider
- Then the checkout shows the review step
- And the payment provider received one payment request for the checkout session's total
Test: dev.domaincentric.sample.ecommerce.e2e.ProviderPaymentE2ETest#authorizedPaymentMovesTheCheckoutToReview

### Rule: A refused payment keeps the customer at the payment step

#### refused-payment-stays-at-payment
- Given a checkout session at the payment step
- And the payment provider refuses payments
- When the customer pays through the payment provider
- Then the checkout stays at the payment step
- And the customer sees "The payment was refused. Please choose another way to pay."
Test: dev.domaincentric.sample.ecommerce.checkout.ProviderPaymentIntegrationTest#refusedPaymentKeepsTheCustomerAtThePaymentStep

### Rule: A provider that does not answer in time counts as unavailable

#### slow-provider-counts-as-unavailable
- Given a checkout session at the payment step
- And the payment provider answers only after 5 seconds
- When the customer pays through the payment provider
- Then the checkout stays at the payment step
- And the customer sees "The payment provider is not available right now. Please try again later."
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

### Rule: A payment request carries the reference of its checkout

#### the-payment-request-names-its-checkout
- Given a checkout session at the payment step
- And the payment provider authorizes payments
- When the customer pays through the payment provider
- Then the payment provider received one payment request for the checkout session's total
- And that request carries the checkout reference, the checkout session's id as text, as `merchantReference`
Test: dev.domaincentric.sample.ecommerce.e2e.ProviderPaymentE2ETest#thePaymentRequestCarriesTheCheckoutReference

#### two-checkouts-send-two-references
- Given two customers, each with a checkout session at the payment step
- When both pay through the payment provider
- Then the payment provider received two payment requests with two different `merchantReference` values
Test: dev.domaincentric.sample.ecommerce.checkout.CheckoutReferenceIntegrationTest#twoCheckoutsSendTwoDifferentReferences

#### the-provider-answers-per-checkout
- Given two customers, each with a checkout session at the payment step
- And the payment provider refuses the payment for the first customer's checkout reference only
- When both pay through the payment provider
- Then the first customer stays at the payment step with "The payment was refused. Please choose another way to pay."
- And the second customer's checkout shows the review step
Test: dev.domaincentric.sample.ecommerce.checkout.CheckoutReferenceIntegrationTest#theProvidersAnswerForOneCheckoutTouchesNoOther

#### a-second-attempt-names-the-same-checkout
- Given a checkout session at the payment step
- And the customer's first payment for it was refused by the payment provider
- When the customer pays through the payment provider again
- Then the payment provider received two payment requests for that checkout session
- And both carry the same `merchantReference`
Test: dev.domaincentric.sample.ecommerce.checkout.CheckoutReferenceIntegrationTest#aSecondAttemptCarriesTheSameReference

### Rule: A checkout whose authorized payment covers its total keeps that payment

#### paying-again-for-the-same-amount-keeps-the-payment
- Given a checkout session whose payment of its total the payment provider authorized
- And the customer went back from the review to the payment step
- When the customer pays through the payment provider again
- Then the checkout shows the review step and the message "Your payment is already authorized."
- And the payment provider received one payment request for that checkout session
Test: dev.domaincentric.sample.ecommerce.e2e.ProviderPaymentE2ETest#payingAgainForTheSameAmountKeepsThePayment

### Rule: A checkout whose total changed after its payment was authorized cancels that payment and requests a new one

#### paying-again-for-a-changed-amount-replaces-the-payment
- Given a checkout session with "Domain-Driven Design" once and "Standard Shipping", whose payment of "59.98" the
  payment provider authorized
- And the customer went back from the review to the delivery step and chose "Express Shipping"
- When the customer pays through the payment provider again
- Then the payment provider received the cancellation of the first payment
- And a second payment request of "64.98" with the same `merchantReference` as the first
- And the checkout shows the review step
Test: dev.domaincentric.sample.ecommerce.checkout.PayingAgainIntegrationTest#payingAgainForAChangedAmountCancelsTheFirstPaymentAndRequestsTheNewTotal
