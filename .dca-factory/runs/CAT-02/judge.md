# Judge — CAT-02

Adopt mode (`status: adopted`): the change is the claim that each mapped test proves its scenario.
Every test was read against its scenario's `Given`, `When`, `Then` and `And`, and every break against
what its test depends on. Nothing was built, so nothing else is reviewed. First round — there is no
`.judge-previous.md`.

## Verdict
verdict: pass

## Perspectives covered
- ddd: not run — adopt mode; nothing was built (carrier `review-ddd` is named and available)
- hexagonal: not run — adopt mode; nothing was built (carrier `review-hexagonal` is named and available)
- clean-code: not run — adopt mode; nothing was built (carrier `review-clean-code` is named and available)
- dca (added): not run — adopt mode; nothing was built (carrier `dca-review` is named and available)
- scenario proof (the adopt-mode review): in-session
- knowledge (`dca-knowledge`, named in the profile): not consulted — reading a test against its
  scenario takes no pattern decision

## Confirmed defects
| Perspective | File:line | Severity | Defect | Fix |
| --- | --- | --- | --- | --- |
| scenario proof | `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/ProductCatalogPage.java:65-73` | minor | `viewProductNamed` filters cards with `setHasText(name)`, which matches substrings. "Domain-Driven Design" also matches "Implementing Domain-Driven Design", "Domain-Driven Design Distilled" and "Learning Domain-Driven Design" (`SampleDataInitializer.java:66,75,93`). `.first()` then picks whichever of these comes first in the catalogue. The test passes today because the right card comes first. If the order changes, the heading assertion fails, so the test cannot pass on the wrong product; it would only break. | Match the title exactly: `setHasText(Pattern.compile("^" + Pattern.quote(name) + "$"))`, or check `getByText(name, new Page.GetByTextOptions().setExact(true))`. |

## Considered and dropped
- `ShopUnderTest`, the changes to `BaseE2ETest`/`BasePage`, `gradle/plugins/test-e2e.gradle` and
  `README.md` in `story.diff`: these come from commit `33236c6`, which is CAT-01 work committed after
  this story's baseline (`tests.md`, notes; decision CAT-02-01 answered). None of them is a mapped
  test for a CAT-02 scenario, so adopt mode does not review them.
- The heading has no `data-test` of its own, and `ProductDetailPage.heading` finds it as `h1` inside
  `data-test="product-detail"` (`ProductDetailPage.java:65-67`). The plan records this as an open
  assumption, and adopt mode changes no markup. The selector is anchored on a stable `data-test`,
  so it is not a defect.
- The integration tests read rendered HTML with regular expressions
  (`ProductDetailPageIntegrationTest.java:32-55`). This is not the "end-user test reads markup as
  text" defect: these are integration-level tests, which the plan assigned to the four
  non-happy-path scenarios. The browser test drives the browser.
- Test levels: the one browser test is the story's marked happy path
  (`<!-- happy-path: view-details-opens-the-product-page -->`). The other four scenarios run through
  MockMvc against the wired application. No adapter was changed, so no integration test was owed
  for one.
- `HEADING` takes the first `<h1>` of the catalogue page (`ProductDetailPageIntegrationTest.java:37`).
  If some other `h1` came first, the test would fail, not pass falsely. The back-link break shows
  that it tells the catalogue apart from `/`.
- Breaks: each one changes only what its own test asserts (`detail.pug:13`, `:37`, `:7`, `:54`,
  `ProductDetailPageViewModel.java:66`). `tests.md` records that each turned exactly its own test red.

## Criteria re-checked
- view-details-opens-the-product-page: met. The test opens the catalogue, clicks "View Details"
  (`data-test="view-product"`) on the "Domain-Driven Design" card and checks three things: the path
  is `/products/<uuid>`, `product-detail` renders, and the heading equals "Domain-Driven Design"
  (`ProductPageE2ETest.java:18-25`). The card lookup is fragile; see the minor defect.
- product-page-shows-image-description-and-category: met. The test reaches the page through the card
  link and checks three values: the image `src` is `/images/products/ddd-book.webp`, the description
  equals the scenario's full text, and the meta item labelled "Category" has the value "Books"
  (`ProductDetailPageIntegrationTest.java:64-70`).
- product-page-title-is-the-product-name: met. `<title>` equals "Clean Architecture"
  (`ProductDetailPageIntegrationTest.java:75-78`).
- product-page-breadcrumb: met. The breadcrumb's visible text equals
  "Home / Products / Clean Architecture", "Home" links to `/` and "Products" links to `/products`
  (`ProductDetailPageIntegrationTest.java:83-88`).
- back-to-products-returns-to-the-catalogue: met. The test starts on the "Team Topologies" page and
  checks that the link reads "Back to Products". It then follows the link's target, and the page
  there is headed "Our Products" (`ProductDetailPageIntegrationTest.java:93-101`).
