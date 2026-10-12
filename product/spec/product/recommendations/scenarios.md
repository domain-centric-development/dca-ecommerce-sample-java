# product — recommendations

### Rule: Directly below the hero the homepage shows a slider headed "Discover products"

#### shows-discover-products-slider-below-hero
Title: The homepage shows a "Discover products" slider directly below the hero
- Given the shop has started and seeded its sample catalog
- When the shopper opens the homepage
- Then a slider headed "Discover products" is shown directly below the hero
- And it comes before the section "Why Shop With Us"
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#showsTheDiscoverProductsSliderDirectlyBelowTheHero

### Rule: The slider holds up to eight random products that have a price, drawn anew on every request

#### slider-holds-eight-different-products
Title: The homepage slider holds eight different products of the sample catalog
- Given the shop has started and seeded its sample catalog
- When the shopper opens the homepage
- Then the slider holds 8 product cards
- And each card shows a different product of the sample catalog
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#holdsEightDifferentProductsOfTheSampleCatalog

#### products-are-drawn-anew-per-request
Title: The homepage slider draws its products anew on every request
- Given the shopper has opened the homepage and noted the 8 products in the slider
- When they reload the homepage 10 times
- Then at least one reload shows a different selection of products
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#drawsItsProductsAnewOnEveryRequest

#### product-without-price-is-not-offered
- Given a product of the catalogue has no price
- When the shopper opens the homepage
- Then that product is not among the cards of the slider
Test: dev.domaincentric.sample.ecommerce.product.ProductSliderIntegrationTest#aProductWithoutAPriceIsNotAmongTheSlidersCards

#### shows-the-priced-products-there-are
- Given exactly 2 products of the catalogue have a price
- When the shopper opens the homepage
- Then the slider holds 2 product cards, one for each of them
Test: dev.domaincentric.sample.ecommerce.product.ProductSliderIntegrationTest#withTwoPricedProductsTheSliderHoldsACardForEachOfThem

### Rule: A card shows the product's image, name and price and leads to its product page

#### card-shows-image-name-and-price
Title: A slider card shows the product's image, name and price
- Given the shopper has opened the homepage
- When they look at a card of the slider
- Then it shows the product's image, its name and the price its product page shows
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#aCardShowsTheProductsImageNameAndPrice

#### card-links-to-product-page
Title: A slider card leads to its product page
- Given the shopper has opened the homepage
- When they follow the link of a card
- Then the product page of that card's product is shown
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#aCardLeadsToItsProductPage

### Rule: Four cards are in view side by side on sizes xl and l, two on size m, one on size s

#### desktop-shows-four-cards-side-by-side
Title: On the desktop the slider shows four of its cards side by side
- Given the shop has started and seeded its sample catalog
- When the shopper opens the homepage on the desktop
- Then the first 4 cards are in view side by side and the other 4 are not
- And "Previous" is disabled and "Next" is enabled
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#onTheDesktopTheFirstFourCardsStandSideBySide

#### size-l-shows-four-cards-side-by-side
Title: On a large tablet the slider shows four of its cards side by side
- Given the shop has started and seeded its sample catalog
- When the shopper opens the homepage on size l
- Then the first 4 cards are in view side by side and the other 4 are not
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderTabletE2ETest#onALargeTabletTheFirstFourCardsStandSideBySide

#### size-m-shows-two-cards-side-by-side
Title: On a small tablet the slider shows two of its cards side by side
- Given the shop has started and seeded its sample catalog
- When the shopper opens the homepage on size m
- Then the first 2 cards are in view side by side and the other 6 are not
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderSmallTabletE2ETest#onASmallTabletTheFirstTwoCardsStandSideBySide

#### phone-shows-one-card-at-a-time
Title: On a phone the slider shows one card at a time
- Given the shop has started and seeded its sample catalog
- When the shopper opens the homepage on a phone
- Then only the first card is in view
- And "Previous" is disabled
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#onAPhoneOnlyTheFirstCardIsInView

### Rule: Previous and Next move by one card, by mouse or keyboard, and are disabled at the start and at the end

#### desktop-next-moves-by-one-card
Title: On the desktop Next moves the slider on by one card
- Given the shopper has opened the homepage on the desktop and the first 4 cards are in view
- When they press "Next"
- Then the second to the fifth card are in view and the first is not
- And "Previous" is enabled
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#onTheDesktopNextMovesTheSliderOnByOneCard

#### desktop-next-is-disabled-at-the-last-card
Title: On the desktop the slider stops at its last card
- Given the shopper has opened the homepage on the desktop
- When they press "Next" four times
- Then the fifth to the eighth card are in view
- And "Next" is disabled
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#onTheDesktopTheSliderStopsAtItsLastCard

#### next-brings-the-following-card-into-view
Title: On a phone Next brings the following slider card into view
- Given the shopper has opened the homepage on a phone and the first card is in view
- When they press "Next"
- Then the second card is in view instead of the first
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#onAPhoneNextBringsTheSecondCardIntoView

#### previous-brings-the-preceding-card-into-view
Title: On a phone Previous brings the preceding slider card back into view
- Given the shopper on a phone has pressed "Next" once and the second card is in view
- When they press "Previous"
- Then the first card is in view again
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#onAPhonePreviousBringsTheFirstCardBackIntoView

#### next-is-operable-by-keyboard
Title: On a phone the slider's Next button works from the keyboard
- Given the shopper on a phone has moved the keyboard focus to "Next" with the Tab key
- When they press Enter
- Then the second card is in view instead of the first
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#onAPhoneNextWorksFromTheKeyboard

#### next-is-disabled-at-the-last-card
Title: On a phone the slider stops at its last card
- Given the shopper has opened the homepage on a phone
- When they press "Next" seven times
- Then the eighth card is in view
- And "Next" is disabled
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#onAPhoneTheSliderStopsAtItsLastCard

#### slider-does-not-move-by-itself
Title: The homepage slider does not move by itself
- Given the shopper has opened the homepage on a phone and the first card is in view
- When they wait 10 seconds without touching the slider
- Then the first card is still in view
Test: dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#theSliderDoesNotMoveByItself
