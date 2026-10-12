---
id: stock-goes-down-by-what-was-bought
epic: guest-checkout
status: adopted
context: inventory
feature: stock
title: Stock goes down by what was bought
depends_on: [place-the-order-from-the-review-and-see, availability-shown]
---

# Stock goes down by what was bought

## Story

As a shop owner I want every placed order to lower the stock of each product by the quantity bought, so
that customers see what is really left and the shop does not offer what it no longer has.

## Acceptance criteria

<!-- happy-path: stock-goes-down-by-the-quantity-bought -->

The stock changes shortly after the order is placed, not at the same moment: within 15 seconds.

### Rule: Each bought line lowers its product's stock by its quantity

#### stock-goes-down-by-the-quantity-bought
- Given the seeded "Wooden Hexagon Desk Model" with a stock of 8
- And a guest who put it in the cart twice and went through the checkout to the review
- When the guest presses "Place Order"
- Then shortly afterwards the product page of "Wooden Hexagon Desk Model" reads "6 units available"

#### every-bought-line-lowers-its-own-stock
- Given the seeded "Domain-Driven Design" with a stock of 20 and "Clean Architecture" with a stock of 35
- And a guest at the review with "Domain-Driven Design" once and "Clean Architecture" twice
- When the guest presses "Place Order"
- Then shortly afterwards the product page of "Domain-Driven Design" reads "19 units available"
- And the product page of "Clean Architecture" reads "33 units available"

#### orders-add-up
- Given "Wooden Hexagon Desk Model" at a stock of 6 after an earlier order of two
- And a second guest at the review with it three times
- When the second guest presses "Place Order"
- Then shortly afterwards the product page of "Wooden Hexagon Desk Model" reads "3 units available"

### Rule: Buying the last units sells the product out

#### buying-the-last-units-sells-out
- Given the seeded "Wooden Hexagon Desk Model" with a stock of 8
- And a guest at the review with it eight times
- When the guest presses "Place Order"
- Then shortly afterwards its product page reads "Currently out of stock" and offers no "Add to Cart"
- And its catalogue card shows the badge "Out of Stock"

## Out of scope

- What the cart keeps after the order — `after-the-order-the-cart-keeps-only`.
- The checkout refusing a start or an order when stock has run short — `starting-and-placing-check-the-cart`.
- Stock set aside when a checkout starts: the shop reserves nothing today.

## Notes

- Replay story: `CHK-11` (guest-checkout).
- Inventory: checkout 47; inventory I4.
