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
