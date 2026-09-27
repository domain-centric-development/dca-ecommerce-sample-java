# Plan — product-slider: Product slider on the homepage

Carrier: in-session; the profile names no `carrier.plan`. Knowledge source: `dca-knowledge` (the
profile's `knowledge:`), bundle `dca-knowledge-catalog/bundle/`; nodes cited where they decided something.
`factory:ask` is available in this session but not named in the profile, so it was not used.

**The .NET sample delivered this story first.** The story file is identical in both shops (`diff` of
`project/backlog/homepage-discovery/product-slider.md`: no difference), and the .NET shop delivered and
accepted it (`../dca-ecommerce-sample-dotnet`, commits `50730b9` and `9225fe9` "the slider holds eight
products, paged by size — accepted", on its `main`). `AGENTS.md:87-91` ("One markup for both shops"): a page
change the .NET sample delivered first is taken over from there — its views under `src/DcaShop.Web/Views/`
and its `wwwroot/css/main.css` — not named anew. This plan therefore names no class, `data-test` attribute,
script hook or style of its own: every one below is copied from the .NET files cited. The application and
domain shape is mirrored as well (`ProductSelection` value object, `GetProductSelection` use case), so the
two shops do not build the same story two ways (root principle 2).

## Context

**Product Catalog (`product`)** — designed map (`project/domain.md:21`, Core) and generated map
(`docs/architecture/context-map.md`, module `product`). The slider shows catalogue products with their
price and leads to the product page `product` already owns
(`product/adapter/incoming/web/ProductPageController.java`, `GET /products/{id}`). The story's answered
assumption places it in `product`, shown on the homepage "the way it shows the mini basket": the mini basket
is a `@ControllerAdvice` of `cart` that adds model attributes for pages only
(`cart/adapter/incoming/web/shopping/MiniBasketControllerAdvice.java:40-45`, "returns early for everything
that is not a page"). Portal stays Separate Ways (`project/domain.md:70`); no Java import between `portal`
and `product` — the portal's homepage template only reads a model attribute (the Java counterpart of the
.NET `Component.InvokeAsync("ProductSlider")`, `Views/Home/Index.cshtml:11`).

Prices come from Pricing through the existing ACL port `PricingDataPort`
(`product/application/shared/PricingDataPort.java`); stock through `ProductStockDataPort`. No new context,
no new relationship. Designed and generated maps agree on `product`'s relationships — no difference to
report.

Project description used: `project/product.md` "Look and feel" — sizes s ≤ 480 px, m ≤ 768 px, l ≤ 1180 px,
xl above; a tested element carries `data-test`; one shared stylesheet; keyboard-usable. `project/tech.md`
"Frontend approach" — server-rendered Pug, a little progressive enhancement, no client framework: the
paging is the .NET view's plain inline script. "Persistence" — nothing is stored. REST API and MCP are out
of scope by the story.

Actor and way in: the shopper on the homepage (`portal/adapter/incoming/web/HomePageController.java`,
`GET /`) and the product page (`GET /products/{id}`); both exist (CAT-04, CAT-02, the story's
`depends_on`). No new surface, no new endpoint.

## Changes

| Element | Kind | Location | New or changed |
|---|---|---|---|
| `ProductSelection` — record implementing `Value` (`dca-building-blocks`, as `ProductName.java:6`); `MAX_SIZE = 8`; `static draw(Collection<ProductId> pricedProducts, RandomGenerator random)`: up to 8 distinct ids drawn at random (partial Fisher–Yates over the distinct candidates), all when fewer, none when none; `productIds()` in draw order, defensive copy. Mirrors `DcaShop.Product/Domain/Model/ProductSelection.cs` | value object (domain, framework-free; `java.util.random.RandomGenerator` is JDK) | `product/domain/model/` | new |
| `GetProductSelectionInputPort`, `GetProductSelectionQuery` (no fields, as the .NET record), `GetProductSelectionResult(List<EnrichedProduct> products)`, `GetProductSelectionUseCase` (`@Service`) | query use case with its input port | `product/application/getproductselection/` (flat, as `getallproducts/`) | new |
| Use-case flow (mirrors `GetProductSelectionUseCase.cs:27-38`): all products from `ProductRepository`; prices from `PricingDataPort.getPrices`; candidates = products whose id the price map **holds** (not the `Money.zero` fallback of `GetAllProductsUseCase.java:72-74`), in catalogue order; `ProductSelection.draw(candidates, random)`; the drawn products enriched with price and stock into `EnrichedProduct`, in draw order. Read-only, no transaction boundary (`recipe/add-a-use-case.md`; plain query use case per `decision/read-model-vs-domain-query.md`) | application logic | same package | new |
| `RandomGenerator` bean for the use case (the .NET registration passes `Random.Shared`, `ProductContextRegistration.cs:36`) | wiring | `product/infrastructure/ProductDomainConfiguration.java` | changed |
| `ProductRepository`, `PricingDataPort`, `ProductStockDataPort`, `EnrichedProduct` | output ports / enriched read model | `product/application/shared/`, `product/domain/model/` | unchanged, reused |
| `ProductSliderControllerAdvice` — `@ControllerAdvice` whose `@ModelAttribute("productSlider")` asks `GetProductSelectionInputPort` (not the use-case class: `rule/hexagonal/dca-hex-011.md`) only for the homepage request `GET /` and returns early for every other request, as the mini basket advice does; imports nothing from another module (`rule/hexagonal/dca-hex-007.md`) | incoming adapter (web) | `product/adapter/incoming/web/` | new |
| `ProductSliderViewModel(List<Card> cards)` with `hasCards()` and nested `record Card(String productId, String name, String imageUrl, String price)` — mirrors `ProductSliderViewModel.cs`; `price` is the text the product page renders (`detail.pug:28`, `#{priceAmount} #{priceCurrency}`), so card and page agree (`recipe/add-a-read-model.md`: primitive view model at the adapter edge) | view model | `product/adapter/incoming/web/` | new |
| Homepage slider fragment — a one-to-one Pug transcription of `Views/Shared/Components/ProductSlider/Default.cshtml` (structure below), rendered only when `productSlider` is present and `hasCards()` (the .NET component renders nothing on an empty selection, `ProductSliderViewComponent.cs:24-27`) | server-rendered page + progressive enhancement | `src/main/resources/templates/home/index.pug`, between `section.hero` and `section(data-test="features")` (the .NET position, `Views/Home/Index.cshtml:11`) | changed |
| Product page: `data-test="product-detail-title"` on the `h1.product-detail__title` and the price text wrapped in `span(data-test="product-detail-price")` — taken over from `Views/Product/Detail.cshtml:12,34`, where the .NET slider story added both (`git log -S` → `50730b9`) | template | `src/main/resources/templates/product/detail.pug:13,28` | changed |
| Slider styles — section "28a. Product Slider (Homepage)" copied verbatim from `.NET wwwroot/css/main.css:2832-2927`, at the same place (after section 28, before "29. Doc Links", Java `main.css:2832`). A `diff` of the two stylesheets today shows that block as the only difference, so after the copy the two files are identical | stylesheet | `src/main/resources/static/css/main.css` | changed |

### The markup, taken over (from `Default.cshtml:2-54`)

```
section.section.product-slider(data-test="product-slider" aria-labelledby="product-slider-title")
  h2.section__title#product-slider-title(data-test="product-slider-title") Discover products
  .product-slider__controls
    button.btn.btn--ghost.product-slider__button(type="button" data-test="product-slider-previous" data-product-slider-previous) Previous
    button.btn.btn--ghost.product-slider__button(type="button" data-test="product-slider-next" data-product-slider-next) Next
  .product-slider__track(data-product-slider-track)
    each card: article.product-slider__card(data-test="product-slider-card")
      a.product-slider__link(href="/products/{id}" data-test="product-slider-card-link")
        .product-slider__image
          img(src alt=name data-test="product-slider-card-image")            — with an image URL
          .product-card__image-placeholder(data-test="product-slider-card-image") first letter  — without
        h3.product-slider__name(data-test="product-slider-card-name") name
        .product-slider__price(data-test="product-slider-card-price") price
  script. — the .NET inline script, verbatim (Default.cshtml:29-53)
```

The script is taken over unchanged: it finds its section by `document.currentScript.parentElement` and the
three `data-product-slider-*` hooks; `step()` is the distance between two cards; `move(±1)` scrolls the
track by one step (`scroll-snap-type: x mandatory` in the CSS); `update()` disables Previous at
`scrollLeft <= 1` and Next at the track's end, on scroll, on resize and on load. Cards per view live in the
CSS alone (4 by default, 2 at `max-width: 768px`, 1 at `max-width: 480px`). Native buttons, so Tab and Enter
work; no timer. The buttons are not rendered `disabled` in the markup (as in .NET); the script sets both on
load.

Architecture held: domain gains one value object, free of framework types; the use case depends only on
ports in `product/application/shared/` and the domain; the advice lives in `product`'s incoming web adapter
and depends on the input port; no aggregate changes, nothing is written, no event is raised.

## Acceptance criteria

Browser runner: Playwright, `./gradlew test-e2e` (`src/test-e2e/java`, `BaseE2ETest`, page objects in
`e2e/pages/`). Integration: `./gradlew test-integration` (`src/test-integration/java`, `@SpringBootTest`
+ MockMvc, as `portal/HomePageIntegrationTest.java`). Sizes: xl is the suite's default window
(`BaseE2ETest.java:106-110`); s = 393 × 852 as the shop's other phone check (`MobileLayoutE2ETest.java:23-26`);
l = 1024 × 768 and m = 768 × 1024, the viewports the .NET suite uses (`HomeSliderTabletE2eTest.cs:17`,
`HomeSliderSmallTabletE2eTest.cs:17`), set through `contextOptions()`. The .NET suite splits the browser
tests by size into `HomeSliderE2eTest`, `HomeSliderTabletE2eTest`, `HomeSliderSmallTabletE2eTest`,
`HomeSliderPhoneE2eTest`; the Java suite mirrors that split.

The story's answered assumption binds every browser-observable scenario to the specification's
`scenario.home.slider-*` scenario (`dca-sample-specification/scenarios.md:233-352`), and the browser test
carries that title verbatim as `@DisplayName`. That binding is why four server-observable scenarios run in
the browser; the two scenarios the specification does not share run integrated.

- shows-discover-products-slider-below-hero: With the seeded catalog, the homepage shows a slider headed "Discover products" directly below the hero, and it comes before the section "Why Shop With Us".  →  level: browser-only (bound to shared scenario `scenario.home.slider-below-hero`, title `The homepage shows a "Discover products" slider directly below the hero`) — Playwright
- slider-holds-eight-different-products: With the seeded catalog, the slider holds 8 product cards, each showing a different product of the sample catalog.  →  level: browser-only (bound to `scenario.home.slider-eight-different-products`, `The homepage slider holds eight different products of the sample catalog`) — Playwright
- products-are-drawn-anew-per-request: Having noted the 8 products, at least one of 10 reloads of the homepage shows a different selection.  →  level: browser-only (bound to `scenario.home.slider-drawn-anew`, `The homepage slider draws its products anew on every request`) — Playwright
- product-without-price-is-not-offered: A catalogue product that has no price is not among the slider's cards.  →  level: integration — `GET /` through MockMvc on the wired application, `src/test-integration/java`
- shows-the-priced-products-there-are: When exactly 2 catalogue products have a price, the slider holds 2 cards, one for each of them.  →  level: integration — `GET /` through MockMvc on the wired application, `src/test-integration/java`
- card-shows-image-name-and-price: A card shows the product's image, its name and the price that product's page shows (`product-slider-card-price` equals `product-detail-price` on its page).  →  level: browser-only (bound to `scenario.home.slider-card-content`, `A slider card shows the product's image, name and price`) — Playwright
- card-links-to-product-page: Following a card's link shows the product page of that card's product (`product-detail-title` equals the card's `product-slider-card-name`).  →  level: e2e (happy path; bound to `scenario.home.slider-card-link`, `A slider card leads to its product page`) — Playwright
- desktop-shows-four-cards-side-by-side: On size xl the first 4 cards are in view side by side and the other 4 are not; "Previous" is disabled and "Next" is enabled.  →  level: browser-only (layout after load; `scenario.home.slider-desktop`, `On the desktop the slider shows four of its cards side by side`) — Playwright
- size-l-shows-four-cards-side-by-side: On size l the first 4 cards are in view side by side and the other 4 are not.  →  level: browser-only (layout; `scenario.home.slider-tablet-four-cards`, `On a large tablet the slider shows four of its cards side by side`) — Playwright
- size-m-shows-two-cards-side-by-side: On size m the first 2 cards are in view side by side and the other 6 are not.  →  level: browser-only (layout; `scenario.home.slider-small-tablet-two-cards`, `On a small tablet the slider shows two of its cards side by side`) — Playwright
- phone-shows-one-card-at-a-time: On size s only the first card is in view, and "Previous" is disabled.  →  level: browser-only (layout; `scenario.home.slider-phone-one-card`, `On a phone the slider shows one card at a time`) — Playwright
- desktop-next-moves-by-one-card: On size xl with the first 4 cards in view, pressing "Next" brings the second to the fifth card into view and the first out of it; "Previous" is enabled.  →  level: browser-only (script reaction to a click; `scenario.home.slider-desktop-next`, `On the desktop Next moves the slider on by one card`) — Playwright
- desktop-next-is-disabled-at-the-last-card: On size xl, after pressing "Next" four times the fifth to the eighth card are in view and "Next" is disabled.  →  level: browser-only (script reaction; `scenario.home.slider-desktop-stops-at-the-end`, `On the desktop the slider stops at its last card`) — Playwright
- next-brings-the-following-card-into-view: On size s with the first card in view, pressing "Next" shows the second card instead of the first.  →  level: browser-only (script reaction; `scenario.home.slider-next`, `On a phone Next brings the following slider card into view`) — Playwright
- previous-brings-the-preceding-card-into-view: On size s after "Next" once, pressing "Previous" shows the first card again.  →  level: browser-only (script reaction; `scenario.home.slider-previous`, `On a phone Previous brings the preceding slider card back into view`) — Playwright
- next-is-operable-by-keyboard: On size s with the keyboard focus moved to "Next" with the Tab key, pressing Enter shows the second card instead of the first.  →  level: browser-only (focus and script reaction; `scenario.home.slider-keyboard`, `On a phone the slider's Next button works from the keyboard`) — Playwright
- next-is-disabled-at-the-last-card: On size s, after pressing "Next" seven times the eighth card is in view and "Next" is disabled.  →  level: browser-only (script reaction; `scenario.home.slider-stops-at-the-end`, `On a phone the slider stops at its last card`) — Playwright
- slider-does-not-move-by-itself: On size s with the first card in view, after 10 seconds without touching the slider the first card is still in view.  →  level: browser-only (behaviour over time; `scenario.home.slider-no-auto-play`, `The homepage slider does not move by itself`) — Playwright

Details the story fixes, covered by the criteria above: the heading "Discover products"; placement
directly below the hero and before "Why Shop With Us"; the button labels "Previous" and "Next"; 4 / 4 / 2
/ 1 cards in view on xl / l / m / s; paging by one card; no auto-play. The `data-test` names come from the
.NET views, not from this plan.

Unit level (`src/test/java`): `ProductSelection.draw` with a seeded `RandomGenerator` — at most 8, all
distinct, all when fewer, empty when none, duplicates in the input count once; `GetProductSelectionUseCase`
with in-memory fakes of its three ports — only priced products are candidates, the result in draw order.

## Files

- `src/main/java/dev/domaincentric/sample/ecommerce/product/domain/model/ProductSelection.java` — changes: new value object (`MAX_SIZE`, `draw`)
- `src/main/java/dev/domaincentric/sample/ecommerce/product/application/getproductselection/GetProductSelectionInputPort.java` — changes: new input port
- `src/main/java/dev/domaincentric/sample/ecommerce/product/application/getproductselection/GetProductSelectionQuery.java` — changes: new query record, no fields
- `src/main/java/dev/domaincentric/sample/ecommerce/product/application/getproductselection/GetProductSelectionResult.java` — changes: new result record (defensive copy)
- `src/main/java/dev/domaincentric/sample/ecommerce/product/application/getproductselection/GetProductSelectionUseCase.java` — changes: new use case (priced candidates, draw, enrichment in draw order)
- `src/main/java/dev/domaincentric/sample/ecommerce/product/infrastructure/ProductDomainConfiguration.java` — changes: `RandomGenerator` bean
- `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductSliderControllerAdvice.java` — changes: new advice adding `productSlider` on `GET /` only
- `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductSliderViewModel.java` — changes: new view model with its `Card` record
- `src/main/resources/templates/home/index.pug` — changes: the slider fragment between hero and features, guarded by `hasCards()`; the inline script
- `src/main/resources/templates/product/detail.pug` — changes: `data-test="product-detail-title"` on the h1, `span(data-test="product-detail-price")` around the price text
- `src/main/resources/static/css/main.css` — changes: section 28a copied from the .NET stylesheet
- `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/HomePage.java` — changes: slider accessors (cards, in view, Previous/Next)
- `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/ProductDetailPage.java` — changes: `price()` over `product-detail-price` (and `heading()` may read `product-detail-title`)
- `src/main/java/dev/domaincentric/sample/ecommerce/product/domain/glossary.md` — changes: the glossary proposals below (document stage)
- `src/main/java/dev/domaincentric/sample/ecommerce/portal/domain/glossary.md` — changes: "Product slider" row as in the .NET Portal glossary (document stage)
- `../dca-ecommerce-sample-dotnet/src/DcaShop.Web/Views/Shared/Components/ProductSlider/Default.cshtml` — read: the markup and script to transcribe
- `../dca-ecommerce-sample-dotnet/src/DcaShop.Web/Views/Home/Index.cshtml` — read: the fragment's position
- `../dca-ecommerce-sample-dotnet/src/DcaShop.Web/Views/Product/Detail.cshtml` — read: lines 12 and 34, the two product-page attributes
- `../dca-ecommerce-sample-dotnet/src/DcaShop.Web/wwwroot/css/main.css` — read: lines 2832-2927, the block to copy
- `../dca-ecommerce-sample-dotnet/src/DcaShop.Product/Domain/Model/ProductSelection.cs`, `Application/GetProductSelection/*.cs`, `Adapter/Incoming/Web/ProductSlider*.cs` — read: the shape to mirror
- `../dca-ecommerce-sample-dotnet/tests/DcaShop.E2eTests/HomeSlider*E2eTest.cs` — read: the browser tests to mirror (split by size, viewports)
- `../dca-ecommerce-sample-dotnet/src/DcaShop.Product/Domain/glossary.md` (`ProductSelection`, line 107) and `src/DcaShop.Portal/Domain/glossary.md:52` — read: the glossary entries to take over
- `src/main/java/dev/domaincentric/sample/ecommerce/product/application/getallproducts/GetAllProductsUseCase.java` — read: the enrichment to mirror, and the `Money.zero` fallback "has a price" must not use
- `src/main/java/dev/domaincentric/sample/ecommerce/cart/adapter/incoming/web/shopping/MiniBasketControllerAdvice.java` — read: the advice pattern and its request guard
- `src/main/resources/templates/layout.pug` — read: inline `script.` precedent
- `src/test-integration/java/dev/domaincentric/sample/ecommerce/portal/HomePageIntegrationTest.java` — read: MockMvc page helpers
- `src/test-integration/java/dev/domaincentric/sample/ecommerce/product/ProductCatalogPageIntegrationTest.java` — read: its own H2 context — a test that changes the catalogue needs a context of its own
- `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/BaseE2ETest.java`, `MobileLayoutE2ETest.java` — read: default window and the `contextOptions()` override
- `src/test/java/dev/domaincentric/sample/ecommerce/specification/SharedScenariosTest.java` — read: display-name binding of browser tests to shared scenario titles

No existing test contradicts the story: `HomePageIntegrationTest` reads each section by its `data-test` and
asserts nothing about what follows the hero; `HomePageE2ETest` checks the welcome only; `ProductPageE2ETest`
reads the heading through `[data-test='product-detail'] h1`, which the added attribute leaves intact. No
`## Changed tests`.

## Glossary proposals

- ProductSelection: up to eight different catalogue products that have a price, drawn at random anew for every homepage request; fewer priced products yield all of them, none yield an empty selection (the .NET Product glossary's entry).
- Product slider: the homepage section "Discover products", directly below the hero: the `ProductSelection` as cards, paged with Previous and Next (the .NET Portal glossary's entry, with the Java composition: a model attribute of `product`'s page advice).

## Open assumptions

- "Has a price" means Pricing returns a price for the product (`PricingDataPort.getPrices` holds its id);
  out-of-stock products are offered (story, answered).
- No priced product → no slider (story, answered, not a criterion): the template's `hasCards()` guard, as the
  .NET component's empty result.
- The card's price text is the Java product page's (`#{priceAmount} #{priceCurrency}`); the .NET card uses its
  own product page's `Money.ToString()` (`0.00 CUR`). Each shop's card equals its own product page, which is
  what the criterion asks.
- Finding, outside this story: `SharedScenariosTest` reads a `@DisplayName` literal as `"[^"]*"`
  (`SharedScenariosTest.java:33,35`), so the quoted title of `scenario.home.slider-below-hero`, written `\"…\"`
  in Java, is not recognised; the story gate reads escapes (dca-factory 0.33.4). The .NET twin fixed its reader
  within this story (its decision `product-slider-01`, answer a) and root TODO #88 expects the Java reader to
  follow "with it"; the Java story, however, no longer carries that assumption, and no criterion asks for it.
  The test runs only with `-Pspecification.path`, so no command the pipeline runs is affected; it is not in
  `## Files` and wants a direct fix (tooling) before the specification check runs against this shop.
- Finding, outside this project: `dca-sample-specification/exceptions.md:7` records the Java shop without a
  slider (review 2026-10-31); once this story is delivered, that exception is obsolete. The specification is
  its own repository; the story no longer asks to remove it, so it is not planned here.
- Harness finding (root principle 2): the .NET plan put the draw into a domain value object; the earlier Java
  plan put it into the use case. Two stages building the same rule differently is a determinism gap; this plan
  closes it by mirroring, but the catalog (e.g. `decision/read-model-vs-domain-query.md` or a recipe) does not
  yet say where a selection rule over a read model belongs. Rule: none; marker: none.
