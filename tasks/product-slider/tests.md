# Tests — product-slider

<!-- gate:tests -->
| criterion | test |
| --- | --- |
| shows-discover-products-slider-below-hero | dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#showsTheDiscoverProductsSliderDirectlyBelowTheHero |
| slider-holds-eight-different-products | dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#holdsEightDifferentProductsOfTheSampleCatalog |
| products-are-drawn-anew-per-request | dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#drawsItsProductsAnewOnEveryRequest |
| product-without-price-is-not-offered | dev.domaincentric.sample.ecommerce.product.ProductSliderIntegrationTest#aProductWithoutAPriceIsNotAmongTheSlidersCards |
| shows-the-priced-products-there-are | dev.domaincentric.sample.ecommerce.product.ProductSliderIntegrationTest#withTwoPricedProductsTheSliderHoldsACardForEachOfThem |
| card-shows-image-name-and-price | dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#aCardShowsTheProductsImageNameAndPrice |
| card-links-to-product-page | dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#aCardLeadsToItsProductPage |
| desktop-shows-four-cards-side-by-side | dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#onTheDesktopTheFirstFourCardsStandSideBySide |
| size-l-shows-four-cards-side-by-side | dev.domaincentric.sample.ecommerce.e2e.HomeSliderTabletE2ETest#onALargeTabletTheFirstFourCardsStandSideBySide |
| size-m-shows-two-cards-side-by-side | dev.domaincentric.sample.ecommerce.e2e.HomeSliderSmallTabletE2ETest#onASmallTabletTheFirstTwoCardsStandSideBySide |
| phone-shows-one-card-at-a-time | dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#onAPhoneOnlyTheFirstCardIsInView |
| desktop-next-moves-by-one-card | dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#onTheDesktopNextMovesTheSliderOnByOneCard |
| desktop-next-is-disabled-at-the-last-card | dev.domaincentric.sample.ecommerce.e2e.HomeSliderE2ETest#onTheDesktopTheSliderStopsAtItsLastCard |
| next-brings-the-following-card-into-view | dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#onAPhoneNextBringsTheSecondCardIntoView |
| previous-brings-the-preceding-card-into-view | dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#onAPhonePreviousBringsTheFirstCardBackIntoView |
| next-is-operable-by-keyboard | dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#onAPhoneNextWorksFromTheKeyboard |
| next-is-disabled-at-the-last-card | dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#onAPhoneTheSliderStopsAtItsLastCard |
| slider-does-not-move-by-itself | dev.domaincentric.sample.ecommerce.e2e.HomeSliderPhoneE2ETest#theSliderDoesNotMoveByItself |

## Files
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/HomeSliderE2ETest.java
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/HomeSliderTabletE2ETest.java
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/HomeSliderSmallTabletE2ETest.java
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/HomeSliderPhoneE2ETest.java
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/HomePage.java
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/ProductDetailPage.java
- src/test-integration/java/dev/domaincentric/sample/ecommerce/product/ProductSliderIntegrationTest.java
- src/test/java/dev/domaincentric/sample/ecommerce/product/domain/model/ProductSelectionTest.java
- src/test/java/dev/domaincentric/sample/ecommerce/product/application/getproductselection/GetProductSelectionUseCaseTest.java
- src/test/java/dev/domaincentric/sample/ecommerce/specification/SharedScenariosTest.java
- src/main/java/dev/domaincentric/sample/ecommerce/product/application/getproductselection/GetProductSelectionInputPort.java
- src/main/java/dev/domaincentric/sample/ecommerce/product/application/getproductselection/GetProductSelectionQuery.java
- src/main/java/dev/domaincentric/sample/ecommerce/product/application/getproductselection/GetProductSelectionResult.java
- src/main/java/dev/domaincentric/sample/ecommerce/product/application/getproductselection/GetProductSelectionUseCase.java
- src/main/java/dev/domaincentric/sample/ecommerce/product/domain/model/ProductSelection.java

## Notes
- Carrier: in-session, following the `e2e-testing` craft (`carrier.test`): page objects over `data-test`
  selectors, settled waits on the rendered boxes and the buttons' state, the application started by the suite
  (`ShopUnderTest`). Knowledge source `dca-knowledge` (profile) was not consulted: this stage took no
  architecture decision beyond the plan's. `factory:ask` is available but not named in the profile, so it was
  not used.
- Mirrored from the .NET twin (plan): `HomeSlider*E2eTest.cs`, `Pages/HomePage.cs`, `ProductSliderTest.cs`,
  `ProductSelectionTest.cs`, `GetProductSelectionUseCaseTest.cs` — same split by size, same viewports (xl =
  default window, l = 1024 x 768, m = 768 x 1024, s = 393 x 852), same in-view judgement from the rendered boxes.
  Browser tests' display names are the `scenario.home.slider-*` titles of the shared specification, verbatim.
- One deviation from the twin: the no-auto-play test runs on Playwright's fake clock (`page.clock().install()`,
  `runFor(10_000)`) instead of a real 10 s wait, so every timer the page sets fires as in ten seconds.
- Every HomeSlider*E2ETest test: currently fails on `the homepage shows the product slider` (`showSlider()` is
  false), because the homepage has no `data-test="product-slider"` section yet.
- ProductSliderIntegrationTest#aProductWithoutAPriceIsNotAmongTheSlidersCards: fails on `hasSize(8)` (actual 0);
  #withTwoPricedProductsTheSliderHoldsACardForEachOfThem: fails on `containsExactlyInAnyOrderElementsOf` (actual
  []) — no slider cards are rendered yet. The test replaces `PricingDataPort` with a fake that prices only the
  products the test names (as the twin does); the plan changes no Pricing adapter, and the changed adapter
  (`ProductSliderControllerAdvice`, web) is passed through `GET /`. It runs in a context of its own
  (`@Import` of its test configuration, own H2 URL), so the replaced answer reaches no other test class.
- unit tests: ProductSelectionTest#drawsEightDifferentProductsFromTheCandidates,
  #drawsEveryCandidateWhenThereAreFewerThanEight, #drawsNothingWithoutCandidates, #aCandidateNamedTwiceIsDrawnOnce,
  #differentRandomSourcesDrawDifferentSelections, #theSameRandomSourceDrawsTheSameSelection for the invariants
  "at most 8, all distinct, all when fewer, none when none, duplicates count once, random per source";
  GetProductSelectionUseCaseTest#offersOnlyProductsThatHaveAPrice, #offersAtMostEightPricedProducts,
  #offersAPricedProductThatIsOutOfStock, #keepsTheOrderTheProductsWereDrawnIn, #offersNothingWhenNoProductHasAPrice
  for "only priced products are candidates, the result in draw order". All fail today on the stubs'
  `UnsupportedOperationException`.
- Stubs added for compilation, each throwing: `ProductSelection` (record, `MAX_SIZE = 8`, `draw` throws),
  `GetProductSelectionInputPort`, `GetProductSelectionQuery`, `GetProductSelectionResult`,
  `GetProductSelectionUseCase` (not yet a Spring bean — the build stage wires it with its `RandomGenerator`).
- Page objects: `HomePage` gained the slider accessors; `ProductDetailPage` gained `title()`
  (`product-detail-title`), `price()` (`product-detail-price`), `imageSource()` and `shownPath()`; existing
  methods unchanged.
- Decision product-slider-01 (answer a) applied: the story carries each browser scenario's shared title as its
  `Title:` line, so the browser tests keep the display names they carry. Changed test on that decision:
  `SharedScenariosTest` now reads escaped literals in `@DisplayName` as the gate does (`\\"` is a quote, `\\\\` a
  backslash), so "The homepage shows a \"Discover products\" slider directly below the hero" binds to its shared
  scenario; its old expectation (every scenario one browser test, every browser test a scenario) is unchanged. Green
  with `-Pspecification.path=../dca-sample-specification`.
- decision: product-slider-02 (answer a) applied: the gate's `test-titles` check was fixed at the source
  (dca-factory 0.40.1, compares the title with the unescaped test source) and this project's gate updated to it.
  No test changed: `HomeSliderE2ETest#showsTheDiscoverProductsSliderDirectlyBelowTheHero` keeps
  `@DisplayName("The homepage shows a \"Discover products\" slider directly below the hero")`, its title verbatim.
