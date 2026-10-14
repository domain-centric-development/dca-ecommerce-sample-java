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

### Rule: A line whose quantity is more than the product's stock says so

#### short-stock-line-warns
- Given the seeded "Wooden Hexagon Desk Model" with a stock of 8
- And a guest whose cart holds "Wooden Hexagon Desk Model" with quantity 2
- And another guest has placed an order for 7 "Wooden Hexagon Desk Model", so 1 is left
- When the first guest opens the cart page
- Then the line "Wooden Hexagon Desk Model" shows "Insufficient stock available"
Test: dev.domaincentric.sample.ecommerce.e2e.StockWarningsE2ETest#aLineHoldingMoreThanTheStockLeftWarnsOfInsufficientStock

### Rule: A line whose product can no longer be bought says so, and only that

#### sold-out-line-says-unavailable
- Given the seeded "Wooden Hexagon Desk Model" with a stock of 8
- And a guest whose cart holds "Wooden Hexagon Desk Model" with quantity 1
- And another guest has placed an order for all 8 "Wooden Hexagon Desk Model"
- When the first guest opens the cart page
- Then the line "Wooden Hexagon Desk Model" shows "Product no longer available"
- And it does not show "Insufficient stock available"
Test: dev.domaincentric.sample.ecommerce.cart.CartStockWarningsIntegrationTest#aLineWhoseProductSoldOutSaysOnlyThatItIsNoLongerAvailable

### Rule: Only the line that is short of stock warns

#### only-the-short-line-warns
- Given a guest whose cart holds "Team Topologies" with quantity 1 and "Wooden Hexagon Desk Model" with quantity 2
- And another guest has placed an order for 7 "Wooden Hexagon Desk Model", so 1 is left
- When the first guest opens the cart page
- Then the page shows exactly one stock warning, on the line "Wooden Hexagon Desk Model"
Test: dev.domaincentric.sample.ecommerce.cart.CartStockWarningsIntegrationTest#onlyTheLineShortOfStockShowsAWarning

### Rule: A cart is opened for the caller, who is always its customer

#### opening-a-cart-for-the-caller
- Given a client holding the token of the customer "ada@example.com", who has no cart yet
- When it sends `POST /api/carts`
- Then the shop answers 201
- And the body has a `cartId`, a `customerId`, `items` [], `status` "ACTIVE" and `itemCount` 0
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#openingACartAnswersCreatedWithAnEmptyActiveCart

#### a-second-open-cart-is-a-conflict
- Given the customer "ada@example.com" already opened a cart through the API
- When the client sends `POST /api/carts` with her token again
- Then the shop answers 409 with an `application/problem+json` body
- And its title is "Active cart already exists"
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#openingASecondCartIsAConflictStatedAsAProblem

#### the-callers-active-cart-by-customer-id
- Given the customer "ada@example.com", whose `customerId` the shop returned when she opened her cart
- When the client sends `GET /api/carts/customer/<her customerId>/active` with her token
- Then the shop answers 200 with that cart, same `cartId`, `status` "ACTIVE"
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#theActiveCartOfTheCallersCustomerIdIsTheCartTheyOpened

### Rule: Adding a product puts it in the cart; the same product stays on one line

#### adding-a-product-returns-the-cart
- Given Ada's open cart is empty
- When the client sends `POST /api/carts/<cartId>/items` with `{"productId":"<id of Domain-Driven Design>","quantity":2}`
- Then the shop answers 200 with the cart
- And it has one item with that `productId` and `quantity` 2, `total` 109.98, `currency` "EUR" and `itemCount` 1
Test: dev.domaincentric.sample.ecommerce.e2e.ShoppingCartApiE2ETest#addingAProductToTheOpenCartAnswersTheCartWithItsLine

#### adding-the-same-product-again-adds-up
- Given Ada's cart holds "Domain-Driven Design" × 2
- When the client adds "Domain-Driven Design" with quantity 1
- Then the cart still has one item, now with `quantity` 3, and `total` 164.97
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#addingAProductTheCartHoldsRaisesItsLinesQuantity

#### reading-the-own-cart
- Given Ada's cart holds "Domain-Driven Design" × 2 and "Team Topologies" × 1
- When the client sends `GET /api/carts/<cartId>` with her token
- Then the shop answers 200 with two items, `total` 139.97, `currency` "EUR" and `itemCount` 2
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#readingTheOwnCartAnswersItsLinesTotalAndItemCount

### Rule: Removing a product takes its whole line out

#### removing-a-product-removes-its-line
- Given Ada's cart holds "Domain-Driven Design" × 3 and "Team Topologies" × 1
- When the client sends `DELETE /api/carts/<cartId>/items/<id of Domain-Driven Design>`
- Then the shop answers 200 with the cart
- And it has one item, "Team Topologies" with `quantity` 1, and `itemCount` 1
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#removingAProductTakesItsWholeLineOut

### Rule: Another customer's cart looks exactly like a cart that does not exist

#### reading-another-customers-cart-finds-nothing
- Given Ada's cart holds "Domain-Driven Design" × 2 and Grace holds her own customer token
- When Grace's client sends `GET /api/carts/<Ada's cartId>`
- Then the shop answers 404
Test: dev.domaincentric.sample.ecommerce.api.ApiAuthorizationIntegrationTest#aCartRouteOnlyAnswersWithTheCallersOwnCart

#### adding-to-another-customers-cart-finds-nothing
- Given Ada's cart holds "Domain-Driven Design" × 2 and Grace holds her own customer token
- When Grace's client adds "Team Topologies" with quantity 1 to Ada's cart
- Then the shop answers 404
- And Ada still sees only "Domain-Driven Design" × 2 in her cart
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#addingToAnotherCustomersCartAnswersNotFoundAndLeavesItAsItWas

#### removing-from-another-customers-cart-finds-nothing
- Given Ada's cart holds "Domain-Driven Design" × 2 and Grace holds her own customer token
- When Grace's client sends `DELETE /api/carts/<Ada's cartId>/items/<id of Domain-Driven Design>`
- Then the shop answers 404
- And Ada still sees "Domain-Driven Design" × 2 in her cart
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#removingFromAnotherCustomersCartAnswersNotFoundAndLeavesItAsItWas

#### another-customers-active-cart-finds-nothing
- Given Ada's `customerId` and Grace holding her own customer token
- When Grace's client sends `GET /api/carts/customer/<Ada's customerId>/active`
- Then the shop answers 404
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#theActiveCartOfAnotherCustomersIdAnswersNotFound

### Rule: Only staff list every cart; a stranger is challenged, a customer is forbidden

#### staff-list-every-cart
- Given Ada's cart holds "Domain-Driven Design" × 2 and a client holding a staff token
- When it sends `GET /api/carts`
- Then the shop answers 200 with `carts`, one of which has Ada's `cartId`, her `customerId`, `status` "ACTIVE", `itemCount` 1, `total` 109.98 and `currency` "EUR"
Test: dev.domaincentric.sample.ecommerce.e2e.ShoppingCartApiE2ETest#staffSeeEveryCartWithItsOwnerStatusAndTotal

#### a-stranger-cannot-list-carts
- Given a client without a token
- When it sends `GET /api/carts`
- Then the shop answers 401 with the header `WWW-Authenticate: Bearer`
Test: dev.domaincentric.sample.ecommerce.api.ApiAuthorizationIntegrationTest#listingEveryCartNeedsStaff

#### a-customer-cannot-list-carts
- Given a client holding the customer token of Ada
- When it sends `GET /api/carts`
- Then the shop answers 403
Test: dev.domaincentric.sample.ecommerce.api.ApiAuthorizationIntegrationTest#listingEveryCartNeedsStaff

### Rule: A call without a token is a throwaway guest

#### tokenless-calls-are-strangers-to-each-other
- Given a client that sends no token
- When it sends `POST /api/carts` twice
- Then both answers are 201, with two different `customerId`s
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#eachCallWithoutATokenOpensACartForANewGuest

### Rule: A product the catalogue does not know cannot be added

#### an-unknown-product-is-not-available
- Given Ada's open, empty cart
- When her client sends `POST /api/carts/<cartId>/items` with `{"productId":"no-such-product","quantity":1}`
- Then the shop answers 404 with an `application/problem+json` body
- And its title is "Article not available" and its detail "There is no article for product no-such-product."
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#addingAProductTheCatalogueDoesNotKnowIsNotFoundStatedAsAProblem

### Rule: No more can be added than is in stock

#### more-than-in-stock-is-refused
- Given Ada's open, empty cart and "Wooden Hexagon Desk Model" with 8 in stock
- When her client adds "Wooden Hexagon Desk Model" with quantity 9
- Then the shop answers 409 with an `application/problem+json` body
- And its title is "Not enough stock" and its detail "Requested 9 of product <id of Wooden Hexagon Desk Model>, more than is in stock."
- And her cart is still empty
Test: dev.domaincentric.sample.ecommerce.e2e.ShoppingCartApiE2ETest#addingMoreThanIsInStockIsAConflictAndLeavesTheCartEmpty

#### exactly-the-stock-is-accepted
- Given Ada's open, empty cart and "Wooden Hexagon Desk Model" with 8 in stock
- When her client adds "Wooden Hexagon Desk Model" with quantity 8
- Then the shop answers 200 and the cart holds it with `quantity` 8
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#addingExactlyTheStockOfAProductPutsAllOfItIntoTheCart

### Rule: A quantity is at least 1

#### a-quantity-of-zero-is-refused
- Given Ada's open, empty cart
- When her client adds "Domain-Driven Design" with quantity 0
- Then the shop answers 400
- And her cart is still empty
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#addingAQuantityOfZeroIsABadRequestAndLeavesTheCartEmpty

### Rule: Removing a product the cart does not hold is refused

#### removing-a-product-not-in-the-cart
- Given Ada's cart holds only "Domain-Driven Design" × 1
- When her client sends `DELETE /api/carts/<cartId>/items/<id of Team Topologies>`
- Then the shop answers 404 with an `application/problem+json` body
- And its title is "Position not in cart" and its detail "The cart holds no position for <id of Team Topologies>."
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#removingAProductTheCartDoesNotHoldIsNotFoundStatedAsAProblem

### Rule: "Remove" takes one line out of the cart, whatever its quantity; the other lines keep their order

#### remove-takes-the-line-out
- Given a guest who added "Domain-Driven Design", "Clean Architecture" and "Team Topologies" once each, in that order
- When they press "Remove" on the line "Clean Architecture" on the cart page
- Then the cart page shows two lines, "Domain-Driven Design" first and "Team Topologies" second
- And the header's cart entry shows the badge "2"
Test: dev.domaincentric.sample.ecommerce.e2e.RemoveCartLineE2ETest#removingALineLeavesTheOtherLinesInTheirOrder

#### remove-takes-the-whole-quantity
- Given a guest whose cart holds "Domain-Driven Design" with quantity 3 and "Clean Architecture" with quantity 1
- When they press "Remove" on the line "Domain-Driven Design"
- Then the cart page shows one line, "Clean Architecture" with quantity 1
- And it shows "Total Items: 1" and "Total Quantity: 1"
Test: dev.domaincentric.sample.ecommerce.cart.RemoveCartItemIntegrationTest#removingALineTakesItsWholeQuantityOut

#### removing-the-last-line-empties-the-cart
- Given a guest whose cart holds only "Domain-Driven Design" with quantity 1
- When they press "Remove" on that line
- Then the cart page shows "Your cart is empty"
Test: dev.domaincentric.sample.ecommerce.cart.RemoveCartItemIntegrationTest#removingTheLastLineLeavesAnEmptyCart

### Rule: A "Remove" for a line the visitor's cart does not hold changes nothing

#### removing-a-line-already-gone-changes-nothing
- Given a guest who opened the cart page with "Domain-Driven Design" and "Clean Architecture" in two tabs and
  pressed "Remove" on "Clean Architecture" in the first tab
- When they press "Remove" on "Clean Architecture" in the second tab
- Then the cart page opens and shows the one line "Domain-Driven Design"
Test: dev.domaincentric.sample.ecommerce.cart.RemoveCartItemIntegrationTest#removingALineAnotherTabRemovedAlreadyChangesNothing

#### a-remove-with-a-malformed-line-id-changes-nothing
- Given a guest whose cart holds "Domain-Driven Design" with quantity 1
- When a "Remove" naming the line "no-such-line" is sent to the shop
- Then the cart page opens and still shows "Domain-Driven Design" with quantity 1
Test: dev.domaincentric.sample.ecommerce.cart.RemoveCartItemIntegrationTest#aRemoveNamingNoLineOfTheCartChangesNothing

#### a-remove-for-another-visitors-line-changes-nothing
- Given a guest whose cart holds `"Ubiquitous Language" Mug` and a second guest in another browser whose cart holds
  "Team Topologies"
- When the second guest sends a "Remove" naming the first guest's `"Ubiquitous Language" Mug` line
- Then the first guest's cart still shows `"Ubiquitous Language" Mug` with quantity 1
- And the second guest's cart page still shows "Team Topologies"
Test: dev.domaincentric.sample.ecommerce.cart.RemoveCartItemIntegrationTest#aRemoveForAnotherVisitorsLineChangesNothing

### Rule: Reading the active cart opens none; a customer without one is answered 404

#### no-active-cart-is-not-found
- Given a client holding the token of the customer "grace@example.com", who has no cart yet, and her `customerId`
- When it sends `GET /api/carts/customer/<her customerId>/active` with her token
- Then the shop answers 404
Test: dev.domaincentric.sample.ecommerce.e2e.ShoppingCartApiE2ETest#theActiveCartOfACustomerWithoutOneIsNotFound

#### a-read-leaves-no-cart-behind
- Given the client of "grace@example.com" has just been answered 404 for her active cart
- When it sends `POST /api/carts` with her token
- Then the shop answers 201
- And the body has `items` [], `status` "ACTIVE" and `itemCount` 0
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#readingAMissingActiveCartLeavesNothingInTheWayOfOpeningOne

### Rule: A product id is a UUID; a product id that does not parse is a malformed call

#### a-malformed-product-id-is-a-bad-request
- Given Ada's open, empty cart
- When her client sends `POST /api/carts/<cartId>/items` with `{"productId":"no-such-product","quantity":1}`
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail
- And her cart is still empty
Test: dev.domaincentric.sample.ecommerce.e2e.ShoppingCartApiE2ETest#addingAProductIdThatIsNoUuidIsABadRequestAndLeavesTheCartEmpty

#### removing-a-malformed-product-id-is-a-bad-request
- Given Ada's cart holds "Domain-Driven Design" × 1
- When her client sends `DELETE /api/carts/<cartId>/items/no-such-product`
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail
- And her cart still holds "Domain-Driven Design" × 1
Test: dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#removingAProductIdThatIsNoUuidIsABadRequestAndLeavesTheCartAsItWas
