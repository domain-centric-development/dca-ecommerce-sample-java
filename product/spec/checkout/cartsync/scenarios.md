# checkout — cartsync

### Rule: Cart changes leave the open checkout as it was started

#### open-checkout-keeps-its-summary
- Given a guest whose cart holds `"Bounded Context" Enamel Pin` twice and who started the checkout from it
- And who then added `"Bounded Context" Enamel Pin` three more times and `"Domain over Framework" Hoodie` once,
  so that the cart shows two lines with the pin five times
- When the guest opens `/checkout/buyer`
- Then the order summary still shows only `"Bounded Context" Enamel Pin x2`
Test: dev.domaincentric.sample.ecommerce.e2e.OpenCheckoutKeepsItsOrderE2ETest#theBuyerStepStillShowsTheOrderTheCheckoutWasStartedWith

### Rule: Starting the checkout again replaces the open one with the current cart

#### restart-takes-the-current-cart
- Given a guest with an open checkout over `"Bounded Context" Enamel Pin` twice whose cart now holds the pin
  five times and `"Domain over Framework" Hoodie` once
- When the guest presses "Proceed to Checkout" on the cart page again
- Then the buyer step's order summary shows `"Bounded Context" Enamel Pin x5` and
  `"Domain over Framework" Hoodie x1`
- And the Subtotal reads "124.94 EUR"
Test: dev.domaincentric.sample.ecommerce.checkout.RestartTheCheckoutIntegrationTest#startingAgainShowsTheCartAsItIsNowWithItsSubtotal

#### restart-begins-with-empty-forms
- Given a guest who submitted the buyer step with "jane@example.com", "Jane", "Doe", "+49 30 1234567" and then
  returned to the cart
- When the guest presses "Proceed to Checkout" on the cart page again
- Then the buyer step opens with all fields empty
Test: dev.domaincentric.sample.ecommerce.checkout.RestartTheCheckoutIntegrationTest#startingAgainAfterTheBuyerStepOpensItWithAllFieldsEmpty
