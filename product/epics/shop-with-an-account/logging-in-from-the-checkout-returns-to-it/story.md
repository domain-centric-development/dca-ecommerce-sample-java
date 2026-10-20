---
id: logging-in-from-the-checkout-returns-to-it
epic: shop-with-an-account
status: approved
created: 2026-10-15T07:50:52Z
context: cart
feature: cartrecovery
title: Logging in from the checkout returns to the checkout
depends_on: []
---

# Logging in from the checkout returns to the checkout

## Story

As a guest who decides at the checkout's buyer step to log in I want to come back to the checkout with my order
after the login, so that I do not start the checkout again from the cart.

## Acceptance criteria

### Rule: Logging in from the checkout takes the guest's cart over and returns to the checkout, with the guest's order

#### login-from-the-checkout-keeps-the-cart-not-the-checkout (happy path)
Title: A guest who logs in from the buyer step returns to the checkout after the cart take-over, with the order
- Given the account "anna.schmidt@example.com", registered earlier, whose cart holds nothing
- And a browser with no cookies in which a guest added "Domain-Driven Design" and pressed "Proceed to Checkout"
- And the guest opened `/login?returnUrl=/checkout/buyer`
- When they log in with "anna.schmidt@example.com" and "Secret123"
- Then the buyer step `/checkout/buyer` opens
- And its order summary shows "Domain-Driven Design x1"

## Out of scope

- A return page outside the shop: stories `login-leads-only-to-shop-pages` and
  `only-the-browsers-own-guest-cart-is-taken-over`.
- Logging in from the checkout when the account's cart holds items too: the merge page asks first, as it does for
  every login.

## Changed expectations

- Logging in from the checkout's buyer step opens the cart page now, with the error "No active checkout session
  found", and the guest's cart in the account; after, the cart is taken over and the buyer step opens again with the
  guest's order, as a registration from the buyer step already does.
- The rule "Logging in from the checkout keeps the guest's cart, not the guest's open checkout" reads "Logging in from
  the checkout takes the guest's cart over and returns to the checkout, with the guest's order".

## Assumptions

- answered: a guest who logs in from the buyer step returns to the checkout through the cart take-over, with the
  order; adopted where it already holds, built where not (planning/questions-2026-10-15.md Q6)
- answered: the checkout session belongs to the cart, not to the person. Checkout finds the session through the
  current customer's active cart and asks Cart (the existing Checkout → Cart direction) whether that cart belongs to
  the current customer. The take-over keeps the guest cart itself (same cart id, same positions) as the account's when
  the account's cart holds nothing, so after the login the same session is found and the person lands on the delivery
  step (the buyer is known now). When the two carts are merged, Cart records that guest cart X was taken over into
  cart Y; after the merge page the person lands on the delivery step, where Checkout follows that record (read through
  Cart), moves the guest's open session to the account and cart Y and captures the merged cart anew. A reload changes
  nothing further. This is the one place where a step page's GET updates an existing session; it never creates one,
  so buyer-step-without-a-checkout-leads-to-the-cart and its three neighbours hold. An ADR names this exception. Cart
  never calls Checkout. Built the same in both shops — answered by Christoph Bloemer on 2026-10-15 in the .NET shop's
  decision logging-in-from-the-checkout-returns-to-it-01
- answered: the use case that resumes the open checkout of the customer's cart is `ResumeCheckout`, in the feature
  `checkoutcompletion` (`checkout/application/checkoutcompletion/resumecheckout`), not in `session`: the step pages
  are CheckoutCompletion's adapters, and an adapter drives one feature's commands (DCA-USE-018) — answered by
  Christoph Bloemer on 2026-10-20 in the .NET shop's decision logging-in-from-the-checkout-returns-to-it-02 (option a),
  for both shops

## Notes

- Checked 2026-10-15 in both shops' code and tests: neither holds it — both open the cart page with "No active
  checkout session found" and prove that with a test (`login-from-the-checkout-keeps-the-cart-not-the-checkout`), so
  the story is built in both.
- The scenario the spec holds is changed under its own key, as the backlog skill asks for a change to a held
  scenario; the key's words ("not the checkout") no longer describe it, but a key is never renamed. The `Title:`
  names the test after what it proves.
- Spec (`cart/cartrecovery`): changes `login-from-the-checkout-keeps-the-cart-not-the-checkout`; rule "Logging in from
  the checkout keeps the guest's cart, not the guest's open checkout" → "Logging in from the checkout takes the
  guest's cart over and returns to the checkout, with the guest's order"; holds `guest-cart-follows-into-the-account`
  and `continue-takes-the-guest-cart-over`. `account/signin` holds `registering-from-the-checkout-keeps-the-checkout`.
