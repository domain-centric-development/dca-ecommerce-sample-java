# checkout — orderconfirmation

### Rule: A placed order sends one order confirmation to the buyer's address

#### the-buyer-gets-the-order-confirmation
- Given a guest at the review whose buyer e-mail is "jane@example.com"
- When the guest presses "Place Order"
- Then shortly afterwards the mail provider has received one mail with the template "order-confirmation" to
  "jane@example.com"
- And the mail carries the order total the confirmation page shows
Test: dev.domaincentric.sample.ecommerce.e2e.OrderConfirmationMailE2ETest#placingTheOrderSendsTheOrderConfirmationToTheBuyer

#### one-order-is-confirmed-once
- Given a guest who placed an order with the buyer e-mail "jane@example.com"
- When the delivery of that order's confirmation is repeated
- Then the mail provider has received the order-confirmation mail for that order once, under the same key
Test: dev.domaincentric.sample.ecommerce.checkout.OrderConfirmationIntegrationTest#repeatedDeliveryReachesTheProviderUnderOneKey

### Rule: An unavailable mail provider delays the confirmation, it does not lose it

#### the-confirmation-follows-once-the-mail-provider-is-back
- Given the mail provider is unavailable
- And a guest with the buyer e-mail "jane@example.com" has placed an order
- When the mail provider answers again
- Then shortly afterwards exactly one order-confirmation mail to "jane@example.com" reaches it
Test: dev.domaincentric.sample.ecommerce.checkout.OrderConfirmationIntegrationTest#theConfirmationFollowsOnceTheMailProviderIsBack
