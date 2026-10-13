# cart — cartrecovery

### Rule: If only the guest cart holds items, they move into the account's cart without a question

#### guest-cart-follows-into-the-account
- Given the account "anna.schmidt@example.com" with password "Secret123", registered earlier, whose cart holds nothing
- And a browser with no cookies in which a guest added `"Ubiquitous Language" Mug` to the cart
- When the guest logs in on `/login` with "anna.schmidt@example.com" and "Secret123"
- Then the cart page `/cart` opens and shows "Welcome back!"
- And the cart shows one line `"Ubiquitous Language" Mug` with quantity 1
Test: dev.domaincentric.sample.ecommerce.e2e.CartAtLoginE2ETest#theGuestCartFollowsIntoAnAccountWhoseCartHoldsNothing

### Rule: If the guest cart is empty, the account's cart is shown as it is

#### account-cart-shown-after-login
- Given the account "anna.schmidt@example.com", registered earlier, which added "Clean Architecture" to its cart while logged in in another browser
- And a browser with no cookies and an empty cart
- When the visitor logs in there with "anna.schmidt@example.com" and "Secret123"
- Then the cart page shows one line "Clean Architecture" with quantity 1
Test: dev.domaincentric.sample.ecommerce.cart.CartAtLoginIntegrationTest#anEmptyGuestCartShowsTheAccountsCartAsItIs

### Rule: Logging in from the checkout keeps the guest's cart, not the guest's open checkout

#### login-from-the-checkout-keeps-the-cart-not-the-checkout
- Given the account "anna.schmidt@example.com", registered earlier, whose cart holds nothing
- And a browser with no cookies in which a guest added "Domain-Driven Design" and pressed "Proceed to Checkout"
- And the guest opened `/login?returnUrl=/checkout/buyer`
- When they log in with "anna.schmidt@example.com" and "Secret123"
- Then the cart page opens with the error "No active checkout session found"
- And the cart shows one line "Domain-Driven Design" with quantity 1
Test: dev.domaincentric.sample.ecommerce.cart.CartAtLoginIntegrationTest#loggingInFromTheBuyerStepKeepsTheGuestsCartButNotTheirCheckout

### Rule: A logged-in customer shops and checks out like a guest

#### logged-in-customer-starts-the-checkout
- Given the account "anna.schmidt@example.com", registered earlier, and a browser with no cookies
- And the visitor logged in with "anna.schmidt@example.com" and "Secret123" and added "Domain-Driven Design" to the cart
- When they press "Proceed to Checkout" on the cart page
- Then the buyer step opens at `/checkout/buyer`
Test: dev.domaincentric.sample.ecommerce.cart.CartAtLoginIntegrationTest#aLoggedInCustomerStartsTheCheckoutLikeAGuest

### Rule: If both carts hold items, logging in asks what to do with them

#### merge-page-when-both-carts-hold-items
- Given the two carts
- When the guest logs in with "anna.schmidt@example.com" and "Secret123"
- Then the merge page opens with the heading "Your Cart Has Items"
- And the sentence "You have items in both your current browsing session and your account. How would you like to proceed?"
Test: dev.domaincentric.sample.ecommerce.cart.CartMergeChoiceIntegrationTest#loggingInWithItemsInBothCartsAsksWhatToDoWithThem

#### merge-page-summarises-both-carts
- Given the two carts
- When the guest logs in with "anna.schmidt@example.com" and "Secret123"
- Then the box "Current Session Cart" shows "Items: 2", "Total Quantity: 3" and "Total: 96.97 EUR", and lists "Clean Architecture" with "Qty: 2" at "39.99 EUR" and `"Ubiquitous Language" Mug` with "Qty: 1" at "16.99 EUR", each with its image
- And the box "Account Cart" shows "Items: 1", "Total Quantity: 1" and "Total: 39.99 EUR", and lists "Clean Architecture" with "Qty: 1" at "39.99 EUR"
Test: dev.domaincentric.sample.ecommerce.cart.CartMergeChoiceIntegrationTest#theMergePageSummarisesTheGuestsCartAndTheAccountsCart

#### merge-page-offers-three-options
- Given the two carts
- When the guest logs in with "anna.schmidt@example.com" and "Secret123"
- Then under "Choose an Option" the page offers "Merge Both Carts" — "Combine all items from both carts. If the same product exists in both, quantities will be added together." — already selected
- And "Use Account Cart Only" — "Keep your account cart and discard the items from your current session."
- And "Use Current Session Cart Only" — "Replace your account cart with the items from your current session."
- And a "Continue" button
Test: dev.domaincentric.sample.ecommerce.cart.CartMergeChoiceIntegrationTest#theMergePageOffersThreeOptionsWithMergingBothChosen

### Rule: "Merge Both Carts" adds the guest lines to the account's cart, adding up quantities of the same product

#### merge-both-adds-up-the-carts
- Given the two carts, and the guest logged in and is on the merge page
- When they press "Continue" with "Merge Both Carts" chosen
- Then the cart page opens and shows "Carts merged successfully!"
- And the cart shows "Clean Architecture" with quantity 3 and `"Ubiquitous Language" Mug` with quantity 1
Test: dev.domaincentric.sample.ecommerce.e2e.CartMergeChoiceE2ETest#mergingBothCartsAddsUpTheQuantitiesOfTheSameProduct

### Rule: "Use Account Cart Only" keeps the account's cart and drops the guest lines

#### use-account-cart-only
- Given the two carts, and the guest logged in and is on the merge page
- When they press "Continue" with "Use Account Cart Only" chosen
- Then the cart page opens and shows "Using your account cart."
- And the cart shows only "Clean Architecture" with quantity 1
Test: dev.domaincentric.sample.ecommerce.cart.CartMergeChoiceIntegrationTest#usingTheAccountCartOnlyKeepsItAndDropsTheGuestLines

### Rule: "Use Current Session Cart Only" replaces the account's cart with the guest lines

#### use-current-session-cart-only
- Given the two carts, and the guest logged in and is on the merge page
- When they press "Continue" with "Use Current Session Cart Only" chosen
- Then the cart page opens and shows "Using your recent cart."
- And the cart shows "Clean Architecture" with quantity 2 and `"Ubiquitous Language" Mug` with quantity 1
Test: dev.domaincentric.sample.ecommerce.cart.CartMergeChoiceIntegrationTest#usingTheSessionCartOnlyReplacesTheAccountsCartWithTheGuestLines

### Rule: A choice the page does not offer is refused

#### unknown-merge-option-is-refused
- Given the two carts, and the guest logged in and is on the merge page
- When the merge form is sent with an option other than the three offered
- Then the merge page opens again and shows "Invalid merge option selected"
Test: dev.domaincentric.sample.ecommerce.cart.CartMergeChoiceIntegrationTest#anOptionThePageDoesNotOfferIsRefused
