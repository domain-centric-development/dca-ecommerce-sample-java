---
id: choose-the-delivery-address
epic: guest-checkout
status: adopted
context: checkout
feature: delivery
title: Choose the delivery address and the shipping method
depends_on: [enter-the-buyer-details]
---

# Choose the delivery address and the shipping method

## Story

As a guest in the checkout I want to say where the order goes and how fast, and see what each way of
shipping costs, so that I get the order where and when I need it.

## Acceptance criteria

<!-- happy-path: address-and-shipping-open-the-payment-step -->

### Rule: The delivery step asks for an address and a shipping method

#### delivery-step-shows-the-form
- Given a guest who submitted the buyer step
- When the guest opens the delivery step
- Then it shows under "Shipping Address" the fields "Street Address", "Address Line 2 (Optional)", "City",
  "State/Province (Optional)", "Postal Code" and "Country"
- And Street Address, City, Postal Code and Country are required, Address Line 2 and State/Province are not
- And a "Back" link and a "Continue to Payment" button

#### four-shipping-methods-are-offered
- Given a guest who submitted the buyer step
- When the guest opens the delivery step
- Then it shows under "Shipping Method" four choices, each with name, estimate and price:
  "Standard Shipping", "5-7 business days", "4.99 EUR"; "Express Shipping", "2-3 business days", "9.99 EUR";
  "Overnight Shipping", "Next business day", "19.99 EUR"; "Free Shipping", "7-10 business days", "0.00 EUR"
- And one of them must be chosen before the form can be sent

#### delivery-step-summarises-the-order
- Given a guest who started the checkout with "Domain-Driven Design" once and submitted the buyer step
- When the guest opens the delivery step
- Then its "Order Summary" shows "Domain-Driven Design x1 54.99 EUR" and Subtotal "54.99 EUR"

#### address-and-shipping-open-the-payment-step
- Given a guest on the delivery step
- When the guest submits Street Address "Hauptstrasse 1", City "Berlin", Postal Code "10115", Country
  "Germany", leaves Address Line 2 and State/Province empty and chooses "Standard Shipping"
- Then the payment step opens at `/checkout/payment`

### Rule: A blank required address field keeps the guest on the delivery step with a message

#### blank-street-is-refused
- Given a guest on the delivery step
- When the guest submits Street Address "   " (spaces only), City "Berlin", Postal Code "10115", Country
  "Germany" and "Standard Shipping"
- Then the delivery step opens again with the error "Street cannot be null or blank"

#### blank-city-is-refused
- Given a guest on the delivery step
- When the guest submits Street Address "Hauptstrasse 1", City "   " (spaces only), Postal Code "10115",
  Country "Germany" and "Standard Shipping"
- Then the delivery step opens again with the error "City cannot be null or blank"

#### blank-postal-code-is-refused
- Given a guest on the delivery step
- When the guest submits Street Address "Hauptstrasse 1", City "Berlin", Postal Code "   " (spaces only),
  Country "Germany" and "Standard Shipping"
- Then the delivery step opens again with the error "Postal code cannot be null or blank"

#### blank-country-is-refused
- Given a guest on the delivery step
- When the guest submits Street Address "Hauptstrasse 1", City "Berlin", Postal Code "10115", Country "   "
  (spaces only) and "Standard Shipping"
- Then the delivery step opens again with the error "Country cannot be null or blank"

## Out of scope

- The payment step and the shipping price in the totals — `choose-how-to-pay`.
- The "Back" link's target and coming back with the address filled in — `go-back-to-an-earlier-step-and-change-it`.
- Sending the delivery form before the buyer step is done — `the-steps-show-the-progress`.

## Notes

- Replay story: `CHK-03` (guest-checkout).
- Inventory: checkout 16 (delivery step), 27, 28, 29.
