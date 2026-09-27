# Build — product-slider

Carrier: in-session; `dca-modelling` (`carrier.build`) and `dca-discipline` (`carrier.guard`) were not
invoked as skills, their invariants were kept as the stage describes (framework-free domain, input port
at the adapter, no cross-context import, read-only query without transaction). Knowledge source
`dca-knowledge` (profile) was not consulted: the build took no decision beyond the plan's, which cites
its nodes. `factory:ask` is available but not named in the profile, so it was not used.

Markup, script and stylesheet are taken over from the .NET sample, as the plan and `AGENTS.md`
("One markup for both shops") require.

## Changed
| File | Why |
| --- | --- |
| src/main/java/dev/domaincentric/sample/ecommerce/product/domain/model/ProductSelection.java | value object: `draw` as a partial Fisher–Yates over the distinct candidates, at most `MAX_SIZE`, defensive copy (mirrors `ProductSelection.cs`) |
| src/main/java/dev/domaincentric/sample/ecommerce/product/application/getproductselection/GetProductSelectionResult.java | defensive copy of the drawn products |
| src/main/java/dev/domaincentric/sample/ecommerce/product/application/getproductselection/GetProductSelectionUseCase.java | query use case as a `@Service`: priced candidates (price map holds the id), draw, enrichment with price and stock in draw order |
| src/main/java/dev/domaincentric/sample/ecommerce/product/infrastructure/ProductDomainConfiguration.java | `RandomGenerator` bean (`java.util.Random`, safe across concurrent requests, as `Random.Shared` in .NET) |
| src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductSliderControllerAdvice.java | adds `productSlider` to the model on `GET /` only, through `GetProductSelectionInputPort` |
| src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductSliderViewModel.java | primitive view model with `Card`, price text as the product page renders it |
| src/main/resources/templates/home/index.pug | slider fragment and inline script transcribed from the .NET `ProductSlider/Default.cshtml`, between hero and features, guarded by `hasCards()` |
| src/main/resources/templates/product/detail.pug | `data-test="product-detail-title"` and `data-test="product-detail-price"`, as the .NET `Detail.cshtml` |
| src/main/resources/static/css/main.css | section "28a. Product Slider (Homepage)" from the .NET stylesheet; the two files are now identical |

## Criteria
- shows-discover-products-slider-below-hero: met by the fragment "Discover products" rendered directly after `section.hero` and before the features section
- slider-holds-eight-different-products: met by `ProductSelection.draw` taking up to 8 distinct ids and one card per drawn product
- products-are-drawn-anew-per-request: met by the advice drawing on every `GET /` with a shared random source
- product-without-price-is-not-offered: met by the use case offering only ids the Pricing answer holds
- shows-the-priced-products-there-are: met by the draw returning every candidate when fewer than 8
- card-shows-image-name-and-price: met by the card's image (or placeholder), name and price text `amount currency`, the same text the product page shows
- card-links-to-product-page: met by the card link `/products/{id}`
- desktop-shows-four-cards-side-by-side, size-l-shows-four-cards-side-by-side, size-m-shows-two-cards-side-by-side, phone-shows-one-card-at-a-time: met by the copied CSS (4 / 2 at 768 px / 1 at 480 px) and the script disabling Previous at the start
- desktop-next-moves-by-one-card, desktop-next-is-disabled-at-the-last-card, next-brings-the-following-card-into-view, previous-brings-the-preceding-card-into-view, next-is-disabled-at-the-last-card: met by the script's `move(±1)` by one card step and `update()` on scroll
- next-is-operable-by-keyboard: met by native `button` elements
- slider-does-not-move-by-itself: met by the script setting no timer

## Deviations from the plan
- `ProductSliderControllerAdvice`: adds the attribute through `Model` in a `void` `@ModelAttribute` method (as `MiniBasketControllerAdvice`) instead of returning it, so requests other than `GET /` get no attribute rather than a `null` one.

## Checks
- ./gradlew testClasses: green
- ./gradlew test --rerun: green (ProductSelectionTest 6/6, GetProductSelectionUseCaseTest 5/5)
- ./gradlew test-integration --rerun: green (ProductSliderIntegrationTest 2/2, HomePageIntegrationTest 6/6)
- ./gradlew test-e2e --rerun: green (HomeSliderE2ETest 8/8, HomeSliderTabletE2ETest 1/1, HomeSliderSmallTabletE2ETest 1/1, HomeSliderPhoneE2ETest 6/6; the suite starts the shop itself)
- ./gradlew test-architecture: green
- ./gradlew spotlessApply, then ./gradlew spotlessCheck: green
