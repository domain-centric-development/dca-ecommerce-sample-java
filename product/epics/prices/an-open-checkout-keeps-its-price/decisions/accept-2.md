---
id: an-open-checkout-keeps-its-price-accept-2
story: an-open-checkout-keeps-its-price
stage: document
kind: acceptance
asked: 2026-10-21T20:43:26Z
digest: 0151b006a2a6f2b4a89ad0166db4043a79e65b633644b80f4a6d4cc4cdebee30
---

# Accept an-open-checkout-keeps-its-price?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- the-review-opened-again-shows-todays-price: Given a guest at the review with "Domain-Driven Design" once and "Standard Shipping", shown at 54.99 EUR; And the price of "Domain-Driven Design" has since been set to 49.99 EUR through the staff API; When the guest opens the review again; Then the review shows "Domain-Driven Design x1 49.99 EUR" and Total "54.98 EUR" — `dev.domaincentric.sample.ecommerce.checkout.ReviewTakesTodaysPricesIntegrationTest#theReviewOpenedAgainShowsTodaysPrice`
- place-order-after-a-price-change-since-the-review-is-refused: Given a guest at the review with "Domain-Driven Design" once and "Standard Shipping", shown at 54.99 EUR; And the price of "Domain-Driven Design" has since been set to 49.99 EUR through the staff API; When the guest presses "Place Order"; Then the review stays open at `/checkout/review` with "The price of 'Domain-Driven Design' has changed. Please check your order and place it again."; And it shows "Domain-Driven Design x1 49.99 EUR" and Total "54.98 EUR"; And no confirmation is shown; And the payment provider received no payment request — `dev.domaincentric.sample.ecommerce.e2e.OrderAtTheReviewedPriceE2ETest#placeOrderAfterAPriceChangeSinceTheReviewIsRefusedAndShowsTodaysPrice`
- place-order-after-a-change-in-another-tab-is-refused: Given a guest at the review with "Domain-Driven Design" once and "Standard Shipping", shown at 54.99 EUR; And in a second tab of the same browser the guest has since chosen "Express Shipping" on the delivery step; When the guest presses "Place Order" on the first tab's review; Then the review stays open at `/checkout/review` with "Your order has changed since you reviewed it. Please check it and place it again."; And it shows "Express Shipping"; And no confirmation is shown — `dev.domaincentric.sample.ecommerce.checkout.OrderChangedSinceTheReviewIntegrationTest#placeOrderAfterAChangeInAnotherTabIsRefused, dev.domaincentric.sample.ecommerce.checkout.OrderAtTodaysPriceAfterReopeningTheReviewIntegrationTest#placeOrderAfterAChangeInAnotherTabIsRefused`
- place-order-after-the-refusal-orders-what-the-review-now-shows: Given a guest whose "Place Order" was refused because the price of "Domain-Driven Design" changed from 54.99 EUR to 49.99 EUR, and the review now shows 49.99 EUR; When the guest presses "Place Order" again; Then the confirmation page shows under "Items Ordered" "Domain-Driven Design x1 49.99 EUR"; And the payment provider received one payment request, of "54.98" — `dev.domaincentric.sample.ecommerce.checkout.OrderAtTodaysPriceAfterReopeningTheReviewIntegrationTest#placeOrderAfterTheRefusalOrdersWhatTheReviewNowShows, dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionAuthorizePaymentTest#theAuthorizedPaymentIsForTheAmountAuthorized`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-21T20:50:19Z
