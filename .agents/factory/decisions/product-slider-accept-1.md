---
id: product-slider-accept-1
story: product-slider
stage: document
kind: acceptance
asked: 2026-09-26T14:53:54Z
digest: 269c6cbd526941e253d353ced82ab7bd8869670c30d3c9cda14eb31a775dfa09
---

# Accept product-slider?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- shows-discover-products-slider-below-hero: Given the shop has started and seeded its sample catalog; When the shopper opens the homepage; Then a slider headed "Discover products" is shown directly below the hero; And it comes before the section "Why Shop With Us" — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#showsTheDiscoverProductsSliderDirectlyBelowTheHero`
- slider-holds-eight-different-products: Given the shop has started and seeded its sample catalog; When the shopper opens the homepage; Then the slider holds 8 product cards; And each card shows a different product of the sample catalog — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#holdsEightDifferentProductsOfTheSampleCatalog`
- products-are-drawn-anew-per-request: Given the shopper has opened the homepage and noted the 8 products in the slider; When they reload the homepage 10 times; Then at least one reload shows a different selection of products — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#drawsItsProductsAnewOnEveryRequest`
- product-without-price-is-not-offered: Given a product of the catalogue has no price; When the shopper opens the homepage; Then that product is not among the cards of the slider — `dev.domaincentric.sample.ecommerce.product.ProductSliderIntegrationTest#aProductWithoutAPriceIsNotAmongTheSlidersCards`
- shows-the-priced-products-there-are: Given exactly 2 products of the catalogue have a price; When the shopper opens the homepage; Then the slider holds 2 product cards, one for each of them — `dev.domaincentric.sample.ecommerce.product.ProductSliderIntegrationTest#withTwoPricedProductsTheSliderHoldsACardForEachOfThem`
- card-shows-image-name-and-price: Given the shopper has opened the homepage; When they look at a card of the slider; Then it shows the product's image, its name and the price its product page shows — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#aCardShowsTheProductsImageNameAndPrice`
- card-links-to-product-page: Given the shopper has opened the homepage; When they follow the link of a card; Then the product page of that card's product is shown — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#aCardLeadsToItsProductPage`
- desktop-shows-four-cards-side-by-side: Given the shop has started and seeded its sample catalog; When the shopper opens the homepage on the desktop; Then the first 4 cards are in view side by side and the other 4 are not; And "Previous" is disabled and "Next" is enabled — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#onTheDesktopTheFirstFourCardsStandSideBySide`
- size-l-shows-four-cards-side-by-side: Given the shop has started and seeded its sample catalog; When the shopper opens the homepage on size l; Then the first 4 cards are in view side by side and the other 4 are not — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderTabletE2ETest#onALargeTabletTheFirstFourCardsStandSideBySide`
- size-m-shows-two-cards-side-by-side: Given the shop has started and seeded its sample catalog; When the shopper opens the homepage on size m; Then the first 2 cards are in view side by side and the other 6 are not — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderSmallTabletE2ETest#onASmallTabletTheFirstTwoCardsStandSideBySide`
- phone-shows-one-card-at-a-time: Given the shop has started and seeded its sample catalog; When the shopper opens the homepage on a phone; Then only the first card is in view; And "Previous" is disabled — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#onAPhoneOnlyTheFirstCardIsInView`
- desktop-next-moves-by-one-card: Given the shopper has opened the homepage on the desktop and the first 4 cards are in view; When they press "Next"; Then the second to the fifth card are in view and the first is not; And "Previous" is enabled — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#onTheDesktopNextMovesTheSliderOnByOneCard`
- desktop-next-is-disabled-at-the-last-card: Given the shopper has opened the homepage on the desktop; When they press "Next" four times; Then the fifth to the eighth card are in view; And "Next" is disabled — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#onTheDesktopTheSliderStopsAtItsLastCard`
- next-brings-the-following-card-into-view: Given the shopper has opened the homepage on a phone and the first card is in view; When they press "Next"; Then the second card is in view instead of the first — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#onAPhoneNextBringsTheSecondCardIntoView`
- previous-brings-the-preceding-card-into-view: Given the shopper on a phone has pressed "Next" once and the second card is in view; When they press "Previous"; Then the first card is in view again — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#onAPhonePreviousBringsTheFirstCardBackIntoView`
- next-is-operable-by-keyboard: Given the shopper on a phone has moved the keyboard focus to "Next" with the Tab key; When they press Enter; Then the second card is in view instead of the first — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#onAPhoneNextWorksFromTheKeyboard`
- next-is-disabled-at-the-last-card: Given the shopper has opened the homepage on a phone; When they press "Next" seven times; Then the eighth card is in view; And "Next" is disabled — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#onAPhoneTheSliderStopsAtItsLastCard`
- slider-does-not-move-by-itself: Given the shopper has opened the homepage on a phone and the first card is in view; When they wait 10 seconds without touching the slider; Then the first card is still in view — `dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#theSliderDoesNotMoveByItself`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.
