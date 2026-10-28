---
id: the-buyer-gets-an-order-confirmation-accept-1
story: the-buyer-gets-an-order-confirmation
stage: document
kind: acceptance
asked: 2026-10-28T20:14:42Z
digest: 2fb0ebd65ff3903f037a8e5616029759a35eabb58c6bd5b35d9adb546d227240
---

# Accept the-buyer-gets-an-order-confirmation?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- the-buyer-gets-the-order-confirmation: Given a guest at the review whose buyer e-mail is "jane@example.com"; When the guest presses "Place Order"; Then shortly afterwards the mail provider has received one mail with the template "order-confirmation" to "jane@example.com"; And the mail carries the order total the confirmation page shows — `dev.domaincentric.sample.ecommerce.e2e.OrderConfirmationMailE2ETest#placingTheOrderSendsTheOrderConfirmationToTheBuyer`
- one-order-is-confirmed-once: Given a guest who placed an order with the buyer e-mail "jane@example.com"; When the delivery of that order's confirmation is repeated; Then the mail provider has received the order-confirmation mail for that order once, under the same key — `dev.domaincentric.sample.ecommerce.checkout.OrderConfirmationIntegrationTest#repeatedDeliveryReachesTheProviderUnderOneKey`
- the-confirmation-follows-once-the-mail-provider-is-back: Given the mail provider is unavailable; And a guest with the buyer e-mail "jane@example.com" has placed an order; When the mail provider answers again; Then shortly afterwards exactly one order-confirmation mail to "jane@example.com" reaches it — `dev.domaincentric.sample.ecommerce.checkout.OrderConfirmationIntegrationTest#theConfirmationFollowsOnceTheMailProviderIsBack`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Blömer
at: 2026-10-28T20:36:18Z
rationale: ok abgenommen — open minor findings follow as stories.
