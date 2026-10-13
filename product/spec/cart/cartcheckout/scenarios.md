# cart — cartcheckout

### Rule: The order removes from the cart exactly the units it bought

#### only-the-later-addition-remains
- Given a guest who started the checkout over `"Bounded Context" Enamel Pin` twice and
  `"Domain over Framework" Hoodie` once
- And who then added `"Ports & Adapters" T-Shirt` once, so that the cart holds three lines
- When the guest places the order from the review
- Then the order is confirmed
- And shortly afterwards the cart page shows one line only: `"Ports & Adapters" T-Shirt` with quantity 1
Test: dev.domaincentric.sample.ecommerce.e2e.CartAfterTheOrderE2ETest#theCartKeepsOnlyWhatWasAddedAfterTheCheckoutStarted

#### a-bought-line-leaves-the-cart-completely
- Given a guest whose cart holds `"Bounded Context" Enamel Pin` three times and who started the checkout from it
- When the guest places the order from the review
- Then shortly afterwards the cart page reads "Your cart is empty"
Test: dev.domaincentric.sample.ecommerce.cart.CartAfterTheOrderIntegrationTest#aLineTheOrderBoughtWhollyLeavesTheCartEmpty

#### units-added-after-the-start-stay
- Given a guest who started the checkout over `"Bounded Context" Enamel Pin` three times
- And who then added the pin twice more, so that the cart shows it five times
- When the guest places the order from the review
- Then shortly afterwards the cart page shows `"Bounded Context" Enamel Pin` with quantity 2
Test: dev.domaincentric.sample.ecommerce.cart.CartAfterTheOrderIntegrationTest#unitsAddedAfterTheCheckoutStartedStayInTheCart

### Rule: The cart stays in use after the order

#### the-cart-accepts-new-items-again
- Given a guest whose order has left `"Ports & Adapters" T-Shirt` alone in the cart
- When the guest adds `"Domain over Framework" Hoodie` from its product page
- Then the cart page shows two lines, `"Ports & Adapters" T-Shirt` and `"Domain over Framework" Hoodie`
- And it offers "Proceed to Checkout"
Test: dev.domaincentric.sample.ecommerce.cart.CartAfterTheOrderIntegrationTest#theCartTakesANewItemAfterTheOrderAndOffersTheCheckout
