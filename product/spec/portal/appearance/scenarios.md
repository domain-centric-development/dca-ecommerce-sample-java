# portal — appearance

### Rule: Every page offers five themes in its footer; Blueprint is the default

#### the-footer-offers-five-themes
- Given a browser that has never chosen a theme
- When the visitor opens the catalogue page
- Then the footer shows a "Theme" choice with the options Blueprint, Organic, Sharp & Minimal, Classic and Neon + Dark, in that order
- And Blueprint is selected and the page is shown in the Blueprint theme
Test: dev.domaincentric.sample.ecommerce.e2e.ThemeChoiceE2ETest#theFooterOffersFiveThemesWithBlueprintSelected

### Rule: A chosen theme applies at once

#### choosing-a-theme-applies-at-once
- Given the visitor is on the catalogue page in the Blueprint theme
- When they choose "Organic" in the footer's theme choice
- Then the page is shown in the Organic theme without being reloaded
Test: dev.domaincentric.sample.ecommerce.e2e.ThemeChoiceE2ETest#choosingAThemeAppliesItWithoutReloadingThePage

### Rule: The browser remembers the choice across pages and visits

#### the-choice-follows-to-other-pages
- Given the visitor chose "Neon + Dark" on the catalogue page
- When they open the cart page
- Then the cart page is shown in the Neon + Dark theme and the footer shows "Neon + Dark" selected
Test: dev.domaincentric.sample.ecommerce.e2e.ThemeChoiceE2ETest#aThemeChosenOnTheCatalogueFollowsToTheCartPage

#### the-choice-survives-a-new-visit
- Given the visitor chose "Classic" and closed the shop
- When they open the home page again in the same browser
- Then it is shown in the Classic theme and the footer shows "Classic" selected
Test: dev.domaincentric.sample.ecommerce.e2e.ThemeChoiceE2ETest#aChosenThemeIsKeptWhenTheShopIsOpenedAgainInTheSameBrowser

#### choosing-blueprint-again-returns-to-the-default
- Given the visitor chose "Sharp & Minimal"
- When they choose "Blueprint" and open the catalogue page again
- Then the catalogue page is shown in the Blueprint theme
Test: dev.domaincentric.sample.ecommerce.e2e.ThemeChoiceE2ETest#choosingBlueprintAgainShowsTheCatalogueInTheDefaultTheme

### Rule: The choice belongs to one browser

#### another-browser-keeps-the-default
- Given the visitor chose "Organic" in one browser
- When the catalogue page is opened in another browser
- Then that browser shows it in the Blueprint theme
Test: dev.domaincentric.sample.ecommerce.e2e.ThemeChoiceE2ETest#aThemeChosenInOneBrowserLeavesAnotherOnTheDefault

### Rule: At 393 px wide, no page on the way to the checkout scrolls sideways

#### the-catalogue-fits-a-phone
- Given a 393 px wide viewport and the seeded sample catalogue
- When the visitor opens the catalogue page
- Then the page does not scroll sideways
Test: dev.domaincentric.sample.ecommerce.e2e.ShopFitsAPhoneE2ETest#theCataloguePageDoesNotScrollSidewaysOnAPhone

#### a-product-page-fits-a-phone
- Given a 393 px wide viewport
- When the visitor opens the product page of "Patterns of Enterprise Application Architecture"
- Then the page does not scroll sideways
Test: dev.domaincentric.sample.ecommerce.e2e.ShopFitsAPhoneE2ETest#aProductPageDoesNotScrollSidewaysOnAPhone

#### the-cart-fits-a-phone
- Given a 393 px wide viewport and "Domain-Driven Design" added to the cart
- When the visitor opens the cart page
- Then the page does not scroll sideways
Test: dev.domaincentric.sample.ecommerce.e2e.ShopFitsAPhoneE2ETest#theCartPageDoesNotScrollSidewaysOnAPhone

#### the-buyer-step-fits-a-phone
- Given a 393 px wide viewport and a cart holding "Domain-Driven Design"
- When the visitor presses "Proceed to Checkout"
- Then the buyer step opens and does not scroll sideways
Test: dev.domaincentric.sample.ecommerce.e2e.ShopFitsAPhoneE2ETest#theBuyerStepOpensAndDoesNotScrollSidewaysOnAPhone

### Rule: Motion is switched off for a visitor who prefers reduced motion

#### reduced-motion-means-no-animation
- Given a browser that supports animated transitions between pages and whose user prefers reduced motion
- When the visitor follows "View Details" from the catalogue to "Domain-Driven Design"
- Then the product page appears without any animation
Test: dev.domaincentric.sample.ecommerce.e2e.PageTransitionE2ETest#theProductPageAppearsWithoutAnimationWhenTheVisitorPrefersReducedMotion

### Rule: Page changes are animated where the browser supports transitions between pages

#### the-frame-stays-while-the-page-changes
- Given a browser that supports animated transitions between pages
- When the visitor follows "View Details" from the catalogue to "Domain-Driven Design"
- Then the page content changes with a fade, while the header, the logo and the footer stay in place
Test: dev.domaincentric.sample.ecommerce.e2e.PageTransitionE2ETest#theContentFadesWhileHeaderLogoAndFooterStayInPlace

#### the-product-image-carries-over
- Given a browser that supports animated transitions between pages
- When the visitor follows "View Details" on the card "Domain-Driven Design"
- Then the product's image moves from its place on the card to its place on the product page
Test: dev.domaincentric.sample.ecommerce.e2e.PageTransitionE2ETest#theProductImageMovesFromTheCardToTheProductPage

#### the-checkout-progress-stays-anchored
- Given a browser that supports animated transitions between pages and a checkout on the buyer step
- When the visitor continues to the delivery step
- Then the checkout progress bar stays in place while the step's form changes
Test: dev.domaincentric.sample.ecommerce.e2e.PageTransitionE2ETest#theProgressBarStaysWhileTheBuyerStepGivesWayToTheDeliveryStep
