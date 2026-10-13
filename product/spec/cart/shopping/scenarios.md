# cart — shopping

### Rule: "Add to Cart" puts one unit of the product into the visitor's cart and opens the cart page

#### add-to-cart-lands-on-the-cart
- Given a guest with an empty cart on the product page of "Domain-Driven Design"
- When they press "Add to Cart"
- Then they are on the cart page at `/cart`
- And the page shows the message "Product added to cart!"
- And the cart shows one line "Domain-Driven Design" with quantity 1
Test: dev.domaincentric.sample.ecommerce.e2e.AddToCartE2ETest#addToCartOpensTheCartWithOneUnitOfTheProduct

#### cart-page-heading
- Given a guest who added "Domain-Driven Design" to the cart
- When they open `/cart`
- Then the page title and the heading read "Shopping Cart"
Test: dev.domaincentric.sample.ecommerce.cart.CartPageIntegrationTest#cartPageIsTitledAndHeadedShoppingCart

### Rule: A product appears on one line only; adding it again raises that line's quantity

#### adding-again-raises-the-quantity
- Given a guest whose cart holds one line "Domain-Driven Design" with quantity 1
- When they press "Add to Cart" on the product page of "Domain-Driven Design" again
- Then the cart shows one line "Domain-Driven Design" with quantity 2
- And no second line for "Domain-Driven Design"
Test: dev.domaincentric.sample.ecommerce.cart.AddItemToCartIntegrationTest#addingTheSameProductAgainRaisesItsLinesQuantity

#### another-product-gets-its-own-line
- Given a guest whose cart holds one line "Domain-Driven Design" with quantity 1
- When they press "Add to Cart" on the product page of "Clean Architecture"
- Then the cart shows two lines: "Domain-Driven Design" with quantity 1 and "Clean Architecture" with quantity 1
Test: dev.domaincentric.sample.ecommerce.cart.AddItemToCartIntegrationTest#anotherProductGetsALineOfItsOwn

### Rule: A guest's cart belongs to their browser and is kept across visits for 30 days

#### cart-kept-across-visits
- Given a guest who added `"Ubiquitous Language" Mug` to the cart and then closed the browser
- When they open the cart page in the same browser the next day
- Then the cart shows the line `"Ubiquitous Language" Mug` with quantity 1
Test: dev.domaincentric.sample.ecommerce.e2e.GuestCartE2ETest#theCartIsThereAgainWhenTheGuestComesBackTheNextDay

### Rule: Every browser has its own cart

#### another-browser-has-its-own-cart
- Given a guest whose cart holds `"Ubiquitous Language" Mug`
- When a second visitor opens the cart page in another browser
- Then the second visitor's cart is empty
Test: dev.domaincentric.sample.ecommerce.cart.GuestCartIntegrationTest#anotherBrowserOpensAnEmptyCart

#### carts-of-two-guests-stay-apart
- Given a guest whose cart holds `"Ubiquitous Language" Mug` and a second guest in another browser
- When the second guest adds "Team Topologies" to their cart
- Then the second guest's cart shows only "Team Topologies"
- And the first guest's cart still shows only `"Ubiquitous Language" Mug`
Test: dev.domaincentric.sample.ecommerce.cart.GuestCartIntegrationTest#cartsOfTwoGuestsStayApart

### Rule: A guest has one cart, whichever page or tab they add from

#### one-cart-across-tabs
- Given a guest who added "Team Topologies" in one tab of their browser
- When they add `"Ubiquitous Language" Mug` from the product page in a second tab of the same browser
- Then the cart page in either tab shows both lines, "Team Topologies" and `"Ubiquitous Language" Mug`
Test: dev.domaincentric.sample.ecommerce.cart.GuestCartIntegrationTest#tabsOfOneBrowserShareTheCart

### Rule: Each line shows its image, name, quantity, today's unit price and the line total

#### cart-shows-lines-with-prices
- Given a guest who added "Domain-Driven Design" twice and "Clean Architecture" once
- When they open the cart page
- Then the line "Domain-Driven Design" shows its product image, "Quantity" 2, "Unit Price" "54.99 EUR" and "Line Total" "109.98 EUR"
- And the line "Clean Architecture" shows its product image, "Quantity" 1, "Unit Price" "39.99 EUR" and "Line Total" "39.99 EUR"
Test: dev.domaincentric.sample.ecommerce.e2e.CartPageE2ETest#eachLineShowsItsImageQuantityUnitPriceAndLineTotal

### Rule: Above the lines the cart counts its lines and its units

#### cart-counts-lines-and-units
- Given a guest who added "Domain-Driven Design" twice and "Clean Architecture" once
- When they open the cart page
- Then it shows "Total Items: 2" and "Total Quantity: 3"
Test: dev.domaincentric.sample.ecommerce.cart.CartPageIntegrationTest#cartCountsItsLinesAndItsUnits

### Rule: Below the lines the cart shows the subtotal and the VAT it contains, at 19 %

#### cart-shows-the-subtotal
- Given a guest who added "Domain-Driven Design" twice and "Clean Architecture" once
- When they open the cart page
- Then the "Subtotal" row shows "149.97 EUR"
Test: dev.domaincentric.sample.ecommerce.cart.CartPageIntegrationTest#cartShowsTheSubtotalBelowTheLines

#### cart-shows-the-contained-vat
- Given a guest who added "Domain-Driven Design" twice and "Clean Architecture" once
- When they open the cart page
- Then the "incl. VAT" row shows "23.94 EUR"
Test: dev.domaincentric.sample.ecommerce.cart.CartPageIntegrationTest#cartShowsTheVatItsSubtotalContains

#### vat-is-rounded-half-up
- Given a guest who added "Domain-Driven Design" once
- When they open the cart page
- Then the "Subtotal" row shows "54.99 EUR" and the "incl. VAT" row shows "8.78 EUR"
Test: dev.domaincentric.sample.ecommerce.cart.CartPageIntegrationTest#containedVatIsRoundedHalfUpToTheCent

### Rule: An empty cart invites the visitor to browse

#### empty-cart-invites-browsing
- Given a guest who has added nothing
- When they open the cart page
- Then it shows "Your cart is empty" and "Browse our products to find something you'll love."
- And a "Browse Products" button to `/products`
- And no lines, counts, subtotal or VAT row
Test: dev.domaincentric.sample.ecommerce.cart.CartPageIntegrationTest#emptyCartInvitesTheVisitorToBrowse

### Rule: Every shop page's header has a "Cart" entry that leads to the cart

#### header-cart-link
- Given a guest on the catalogue page
- When they follow "Cart" in the header
- Then they are on the cart page at `/cart`
Test: dev.domaincentric.sample.ecommerce.cart.HeaderBasketIntegrationTest#headerCartEntryLeadsToTheCartPage

### Rule: The header's cart entry carries a badge with the number of units in the cart

#### badge-counts-units
- Given a guest who added "Domain-Driven Design" twice and "Clean Architecture" once
- When they open the catalogue page
- Then the header's cart entry shows the badge "3"
Test: dev.domaincentric.sample.ecommerce.cart.HeaderBasketIntegrationTest#headerCartEntryCountsTheUnitsInTheCart

#### badge-follows-each-add
- Given a guest whose cart holds "Domain-Driven Design" once
- When they press "Add to Cart" on the product page of "Domain-Driven Design"
- Then the header's cart entry on the cart page shows the badge "2"
Test: dev.domaincentric.sample.ecommerce.cart.HeaderBasketIntegrationTest#badgeFollowsEachAddToTheCart

### Rule: Hovering the header basket shows the cart's lines, their total at today's prices and a way to the cart

#### basket-dropdown-lists-the-cart
- Given a guest who added "Domain-Driven Design" twice and "Clean Architecture" once
- When they hover the header's cart entry on the catalogue page
- Then a dropdown lists "Domain-Driven Design" with "x2" and "Clean Architecture" with "x1"
- And it shows "Total" with "149.97 EUR"
- And it offers a "View Cart" button to `/cart`
Test: dev.domaincentric.sample.ecommerce.e2e.HeaderBasketE2ETest#hoveringTheHeaderBasketListsTheLinesTheirTotalAndAWayToTheCart
