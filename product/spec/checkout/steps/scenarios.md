# checkout — steps

### Rule: Back links lead one step back, with the form filled in

#### back-from-delivery-opens-the-buyer-step
- Given the guest is on the delivery step
- When the guest follows the delivery step's "Back" link
- Then the buyer step opens at `/checkout/buyer`
- And its fields hold "jane@example.com", "Jane", "Doe" and "+49 30 1234567"
Test: dev.domaincentric.sample.ecommerce.e2e.GoBackToAnEarlierStepE2ETest#backFromTheDeliveryStepOpensTheBuyerStepFilledIn

#### back-from-payment-opens-the-delivery-step
- Given the guest submitted the delivery step with "Hauptstrasse 1", "Berlin", "10115", "Germany" and
  "Standard Shipping" and is on the payment step
- When the guest follows the payment step's "Back" link
- Then the delivery step opens with these address fields filled in and "Standard Shipping" chosen
Test: dev.domaincentric.sample.ecommerce.checkout.GoBackToAnEarlierStepIntegrationTest#backFromThePaymentStepOpensTheDeliveryStepFilledIn

#### back-from-review-opens-the-payment-step
- Given the guest submitted the delivery step, chose "Mock Payment (Test)" and is on the review step
- When the guest follows the review's "Back" link
- Then the payment step opens with "Mock Payment (Test)" chosen
Test: dev.domaincentric.sample.ecommerce.checkout.GoBackToAnEarlierStepIntegrationTest#backFromTheReviewOpensThePaymentStepWithTheProviderChosen

### Rule: Each section of the review has an Edit link to its step

#### edit-links-on-the-review
- Given the guest submitted the delivery step, chose "Mock Payment (Test)" and is on the review step
- When the guest follows the "Edit" link of the "Delivery" section
- Then the delivery step opens with the address filled in and "Standard Shipping" chosen
- And the review's "Buyer Information" and "Payment Method" sections carry "Edit" links to the buyer step and
  the payment step
Test: dev.domaincentric.sample.ecommerce.checkout.GoBackToAnEarlierStepIntegrationTest#eachSectionOfTheReviewLinksToItsStep

### Rule: Changing an earlier step replaces its data and keeps the later progress

#### changed-shipping-recalculates-the-totals
- Given the guest submitted the delivery step with "Standard Shipping", chose "Mock Payment (Test)" and has
  reopened the delivery step from the review
- When the guest submits the delivery step again with "Express Shipping"
- Then the payment step opens showing Shipping "9.99 EUR" and Total "64.98 EUR"
Test: dev.domaincentric.sample.ecommerce.checkout.GoBackToAnEarlierStepIntegrationTest#submittingTheDeliveryAgainWithAnotherMethodRecalculatesTheTotals

#### changed-step-keeps-the-later-progress
- Given the guest reached the review with "Standard Shipping" and "Mock Payment (Test)", then submitted the
  delivery step again with "Express Shipping"
- When the guest opens `/checkout/review` directly
- Then the review opens, showing "Express Shipping", "Estimated Delivery: 2-3 business days" and
  "Cost: 9.99 EUR"
- And Subtotal "54.99 EUR", Shipping "9.99 EUR", Tax "10.37 EUR" and Total "64.98 EUR"
- And the "Payment Method" section still names the chosen payment provider
Test: dev.domaincentric.sample.ecommerce.checkout.GoBackToAnEarlierStepIntegrationTest#theReviewStillOpensAfterAnEarlierStepWasChanged

#### changed-buyer-details-reach-the-review
- Given the guest reached the review with "Standard Shipping" and "Mock Payment (Test)" and has reopened the
  buyer step from the review
- When the guest submits the buyer step again with Email Address "jane.doe@example.com"
- Then the delivery step opens with the address still filled in
- And the review, opened directly, shows "jane.doe@example.com"
Test: dev.domaincentric.sample.ecommerce.checkout.GoBackToAnEarlierStepIntegrationTest#changedBuyerDetailsKeepTheAddressAndReachTheReview

### Rule: Every step page shows a four-step progress bar

#### progress-bar-on-the-buyer-step
- Given a guest who has just started the checkout
- When the guest opens the buyer step
- Then a progress bar shows the steps "Buyer Info", "Delivery", "Payment" and "Review" in this order, numbered
  1 to 4
- And "Buyer Info" is marked as the current step and none as done
Test: dev.domaincentric.sample.ecommerce.checkout.StepsShowTheProgressIntegrationTest#theBuyerStepShowsFourNumberedStepsWithTheFirstAsCurrent

#### progress-bar-on-the-review
- Given a guest who has submitted the buyer, delivery and payment steps
- When the guest opens the review step
- Then the progress bar marks "Buyer Info", "Delivery" and "Payment" as done and "Review" as the current step
Test: dev.domaincentric.sample.ecommerce.e2e.StepsShowTheProgressE2ETest#theReviewMarksTheEarlierStepsAsDoneAndItselfAsCurrent

#### progress-bar-on-the-confirmation
- Given a guest on the review step
- When the guest presses "Place Order"
- Then the confirmation page's progress bar shows "Buyer Info", "Delivery", "Payment" and "Confirmed", all
  four marked as done
Test: dev.domaincentric.sample.ecommerce.checkout.StepsShowTheProgressIntegrationTest#theConfirmationMarksAllFourStepsAsDone

### Rule: A step ahead of the progress opens the current step instead

#### skipping-ahead-opens-the-current-step
- Given a guest who has submitted the buyer step and is on the delivery step
- When the guest opens `/checkout/review` directly
- Then the delivery step opens at `/checkout/delivery`
Test: dev.domaincentric.sample.ecommerce.checkout.StepsShowTheProgressIntegrationTest#openingTheReviewFromTheDeliveryStepOpensTheDeliveryStep

#### payment-from-the-buyer-step-opens-the-buyer-step
- Given a guest who has just started the checkout
- When the guest opens `/checkout/payment` directly
- Then the buyer step opens at `/checkout/buyer`
Test: dev.domaincentric.sample.ecommerce.checkout.StepsShowTheProgressIntegrationTest#openingThePaymentStepFromTheBuyerStepOpensTheBuyerStep

### Rule: A step's form sent before the earlier steps are done changes nothing

#### delivery-sent-before-the-buyer-step
- Given a guest who has just started the checkout and has not submitted the buyer step
- When a delivery form with Street Address "Hauptstrasse 1", City "Berlin", Postal Code "10115", Country
  "Germany" and "Standard Shipping" is sent to the shop
- Then the guest ends on the buyer step at `/checkout/buyer`
- And the review cannot be opened afterwards: `/checkout/review` opens the buyer step
Test: dev.domaincentric.sample.ecommerce.checkout.StepsShowTheProgressIntegrationTest#aDeliveryFormSentBeforeTheBuyerStepEndsOnTheBuyerStep

#### place-order-before-the-review
- Given a guest who has submitted the buyer and delivery steps and is on the payment step
- When a "Place Order" is sent to the shop
- Then the guest ends on the payment step at `/checkout/payment`
- And no confirmation is shown: the order is not placed
Test: dev.domaincentric.sample.ecommerce.checkout.StepsShowTheProgressIntegrationTest#placeOrderSentFromThePaymentStepEndsOnThePaymentStep

### Rule: A step page without an open checkout leads to the cart with a message

#### buyer-step-without-a-checkout-leads-to-the-cart
- Given a visitor who has not started a checkout
- When the visitor opens `/checkout/buyer` directly
- Then the cart page opens at `/cart`
- And it shows the error "No active checkout session found"
Test: dev.domaincentric.sample.ecommerce.e2e.NoOpenCheckoutE2ETest#theBuyerStepWithoutACheckoutLeadsToTheCartWithAMessage

#### later-step-without-a-checkout-leads-to-the-cart
- Given a visitor who has not started a checkout
- When the visitor opens `/checkout/review` directly
- Then the cart page opens with the error "No active checkout session found"
Test: dev.domaincentric.sample.ecommerce.checkout.NoOpenCheckoutIntegrationTest#theReviewWithoutACheckoutLeadsToTheCartWithAMessage

#### steps-after-the-order-lead-to-the-cart
- Given a guest who has placed an order and seen its confirmation
- When the guest opens `/checkout/buyer`
- Then the cart page opens with the error "No active checkout session found"
Test: dev.domaincentric.sample.ecommerce.checkout.NoOpenCheckoutIntegrationTest#theBuyerStepAfterTheOrderLeadsToTheCartWithAMessage

#### restart-of-the-shop-ends-an-open-checkout
- Given a guest with an open checkout on the delivery step
- And the shop has been restarted since
- When the guest opens `/checkout/delivery`
- Then the cart page opens with the error "No active checkout session found"
Test: dev.domaincentric.sample.ecommerce.e2e.NoOpenCheckoutE2ETest#aRestartOfTheShopEndsTheOpenCheckout

### Rule: The confirmation stays available after the order, and only after one

#### confirmation-can-be-reopened
- Given a guest who has placed an order for "Domain-Driven Design" once and has since browsed the catalogue
- When the guest opens `/checkout/confirmation`
- Then it reads "Your order has been placed successfully"
- And it shows "Domain-Driven Design x1 54.99 EUR" under "Items Ordered"
Test: dev.domaincentric.sample.ecommerce.checkout.NoOpenCheckoutIntegrationTest#theConfirmationCanBeReopenedAfterBrowsingTheCatalogue

#### confirmation-without-an-order-leads-to-the-cart
- Given a visitor who has never placed an order
- When the visitor opens `/checkout/confirmation` directly
- Then the cart page opens with the error "No confirmed order found"
Test: dev.domaincentric.sample.ecommerce.checkout.NoOpenCheckoutIntegrationTest#theConfirmationWithoutAnOrderLeadsToTheCartWithAMessage
