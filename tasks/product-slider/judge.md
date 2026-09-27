# Judge — product-slider

## Verdict
verdict: pass

## Perspectives covered
- ddd: `review-ddd` (the profile's `review.ddd`), the project skill at `.claude/skills/review-ddd/SKILL.md`, applied in-session
- hexagonal: `review-hexagonal` (the profile's `review.hexagonal`), `.claude/skills/review-hexagonal/SKILL.md`, applied in-session
- clean-code: `review-clean-code` (the profile's `review.clean-code`), `.claude/skills/review-clean-code/SKILL.md`, applied in-session
- dca (added): `dca-review` (the profile's `review.dca`), `.claude/skills/dca-review/SKILL.md`, applied in-session, including its twin checkpoint

Knowledge source: `dca-knowledge` (the profile's `knowledge:`) was not consulted. No finding depended on a
pattern question the project's own rules, glossary and the plan's cited nodes did not already settle.
`factory:ask` is available in this session but the profile does not name it, so it was not used.

Input: `tasks/product-slider/.verify/story.diff` together with the untracked files it lists, the story,
`plan.md`, `tests.md`, `build.md`, `tidy.md`, `project/product.md`, and decisions `product-slider-01` and `-02`.

## Confirmed defects
| Perspective | File:line | Severity | Defect | Fix |
|---|---|---|---|---|
| clean-code | `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductSliderViewModel.java:52-54` | minor | This is now the third place that decides how a price reads (`amount + " " + currencyCode`), after `ProductDetailPageViewModel.java:62` and `ProductCatalogPageViewModel.java:76`. The criterion `card-shows-image-name-and-price` holds only while the two copies agree. The e2e test catches drift, and the fix would touch files outside this story, as tidy noted. | Extract one price-text formatter in `product/adapter/incoming/web/` and use it in all three view models, in a later tidy story. |
| process | `tasks/product-slider/plan.md:167-170` | minor | The plan says "No `## Changed tests`", but `SharedScenariosTest.java` was changed. Decision `product-slider-01`'s answer ("listed as a changed test of this story") backs the change, and the diff does exactly what that line says: unescaping `\"` and `\\` in `@DisplayName` literals, with the old expectation unchanged. Only the plan's list is missing. | None needed for delivery. A later plan revision should list it under `## Changed tests`. |

No blocker or major finding.

## Considered and dropped
- Browser tests for scenarios that are not the happy path: each one is `browser-only` in the plan with a reason. Eleven depend on layout, script or timing. The other four (below-hero, eight-different, drawn-anew, card-content) are bound to shared `scenario.home.slider-*` scenarios by the story's answered assumption, and a browser test carries each of those titles. The one happy path, `card-links-to-product-page`, is e2e. Not a test-level defect.
- `ProductSliderIntegrationTest` replaces `PricingDataPort` with a fake: the plan changes no Pricing adapter. The adapter the plan does change (`ProductSliderControllerAdvice`) is exercised through `GET /` on the wired application. Not a mocked-port defect.
- No end-user test reads markup as text. The browser tests judge from rendered boxes, button state, focus and the page clock (`HomePage.java` `CARD_IN_VIEW`, `settlesTo`, `tabToNext`). The integration test's regex over the HTML runs at integration level, not in the browser.
- The advice has no error handling around the selection, unlike the mini basket's "handled gracefully" behaviour, so a Pricing failure would fail `GET /`. Neither the story nor `project/product.md` states a quality here, and the twin's `ProductSliderViewComponent` behaves the same way. This is a suspicion, not a defect.
- `ProductSelection` in `domain/model/` holds a `RandomGenerator` parameter: that is JDK, not framework. A value object implementing `Value` with a defensive copy and a size invariant (`ProductSelection.java:21-29`). It mirrors the twin, and the rule suite is green.
- The advice guard uses `getRequestURI()`, which would ignore a context path. The shop has none, and `MiniBasketControllerAdvice` uses the same approach. Speculative.
- Twin checkpoint: markup, `data-test` names and `main.css` match the .NET views (AGENTS.md "One markup for both shops"). The price text differs between the shops (`10.00 EUR` against .NET `Money.ToString()`), but each shop's card equals its own product page, as the plan's open assumption records. This is intentional and recorded.
- The removed story assumptions and the `AGENTS.md` paragraph are backlog and instruction edits, not code. The markup contract they carried now sits in `AGENTS.md:87-91`.

## Criteria re-checked
- shows-discover-products-slider-below-hero: met (the heading text, `sectionAfterHero() == "product-slider"`, and the layout position before `features`)
- slider-holds-eight-different-products: met (8 cards, 8 distinct names, all among the catalogue's titles)
- products-are-drawn-anew-per-request: met (notes 8, reloads 10 times, asserts at least one differs)
- product-without-price-is-not-offered: met (10 of the seeded products priced, 10 requests, every card among the priced ones)
- shows-the-priced-products-there-are: met (2 priced, cards are exactly those 2)
- card-shows-image-name-and-price: met (image loaded; name, price and image source equal the product page's)
- card-links-to-product-page: met (follows the link, shown path equals the link, title equals the card name)
- desktop-shows-four-cards-side-by-side: met (exactly cards 0 to 3 in view, one line left to right, Previous disabled, Next enabled)
- size-l-shows-four-cards-side-by-side: met (1024 × 768, exactly 0 to 3 in view)
- size-m-shows-two-cards-side-by-side: met (768 × 1024, exactly 0 and 1 in view)
- phone-shows-one-card-at-a-time: met (393 × 852, exactly card 0 in view, Previous disabled)
- desktop-next-moves-by-one-card: met (exactly 1 to 4 in view after Next, Previous enabled)
- desktop-next-is-disabled-at-the-last-card: met (four presses, exactly 4 to 7 in view, Next disabled)
- next-brings-the-following-card-into-view: met (exactly card 1 in view)
- previous-brings-the-preceding-card-into-view: met (Next, then Previous, exactly card 0 in view)
- next-is-operable-by-keyboard: met (Tab until focus is on Next, Enter, exactly card 1 in view)
- next-is-disabled-at-the-last-card: met (seven presses, exactly card 7 in view, Next disabled)
- slider-does-not-move-by-itself: met (fake clock runs 10 s, card 0 still in view, card 1 not)
