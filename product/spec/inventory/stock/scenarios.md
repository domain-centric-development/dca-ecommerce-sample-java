# inventory — stock

### Rule: Each bought line lowers its product's stock by its quantity

#### stock-goes-down-by-the-quantity-bought
- Given the seeded "Wooden Hexagon Desk Model" with a stock of 8
- And a guest who put it in the cart twice and went through the checkout to the review
- When the guest presses "Place Order"
- Then shortly afterwards the product page of "Wooden Hexagon Desk Model" reads "6 units available"
Test: dev.domaincentric.sample.ecommerce.e2e.StockGoesDownE2ETest#placingAnOrderLowersTheStockByTheQuantityBought

#### every-bought-line-lowers-its-own-stock
- Given the seeded "Domain-Driven Design" with a stock of 20 and "Clean Architecture" with a stock of 35
- And a guest at the review with "Domain-Driven Design" once and "Clean Architecture" twice
- When the guest presses "Place Order"
- Then shortly afterwards the product page of "Domain-Driven Design" reads "19 units available"
- And the product page of "Clean Architecture" reads "33 units available"
Test: dev.domaincentric.sample.ecommerce.inventory.StockGoesDownByWhatWasBoughtIntegrationTest#everyBoughtLineLowersTheStockOfItsOwnProduct

#### orders-add-up
- Given "Wooden Hexagon Desk Model" at a stock of 6 after an earlier order of two
- And a second guest at the review with it three times
- When the second guest presses "Place Order"
- Then shortly afterwards the product page of "Wooden Hexagon Desk Model" reads "3 units available"
Test: dev.domaincentric.sample.ecommerce.inventory.StockGoesDownByWhatWasBoughtIntegrationTest#aSecondOrderLowersTheStockTheFirstLeft

### Rule: Buying the last units sells the product out

#### buying-the-last-units-sells-out
- Given the seeded "Wooden Hexagon Desk Model" with a stock of 8
- And a guest at the review with it eight times
- When the guest presses "Place Order"
- Then shortly afterwards its product page reads "Currently out of stock" and offers no "Add to Cart"
- And its catalogue card shows the badge "Out of Stock"
Test: dev.domaincentric.sample.ecommerce.inventory.StockGoesDownByWhatWasBoughtIntegrationTest#buyingTheLastUnitsSellsTheProductOut
