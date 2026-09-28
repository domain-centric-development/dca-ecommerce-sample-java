# Judge — CAT-01

Mode: **adopt** (`status: adopted`). Nothing was built. The claim under review is that each mapped test
proves its scenario. This is round 2. The previous verdict was `pass`. After it, the adopt gate failed
on `break-proof` (`tasks/CAT-01/.gate-adopt.txt`): all four integration tests were reported as staying
green under their breaks. The test stage then ran again and changed no file (`.verify/changed-test.txt`
is empty). So the tests, the breaks and the diff are the same as in round 1. This round reads them again,
this time with the gate's `break-proof` result in mind. Stage carried in-session.

## Verdict
verdict: pass

## Perspectives covered
- ddd: not run. In adopt mode no model code changed. The carrier `review-ddd` is available but has nothing to review.
- hexagonal: not run. In adopt mode no port or adapter changed. The carrier `review-hexagonal` is available but has nothing to review.
- clean-code: not run on production code, because none changed. The adopt check of the mapped tests and their breaks ran in-session.
- dca (added): not covered. In adopt mode nothing was built, so the carrier `dca-review` has no change to review.
- Knowledge: the profile names `dca-knowledge`. It was not consulted, because no finding depended on a pattern or a rule.

## Confirmed defects
| Perspective | File:line | Severity | Defect | Fix |
| --- | --- | --- | --- | --- |
| — | — | — | none | — |

## Considered and dropped
- **The gate's `break-proof` failure. The tests do not explain it (suspicion, not a confirmed defect).**
  Each break was read against the current source it patches and against the assertion it targets. Every
  hunk's header and old side match the current file line for line. Under each break, the test fails on
  one of its own assertions:
  - The order break: `InMemoryProductRepository.java:40-41` becomes `...reversed()`. The titles are
    then in descending order, and `ProductCatalogPageIntegrationTest.java:61`
    (`isEqualTo(titles.stream().sorted().toList())`) fails.
  - The description break: `catalog.pug:23` renders `<p class="product-card__description"></p>`.
    `DESCRIPTION` (line 37) then captures `""`, and line 75 expects `DDD_DESCRIPTION`, so the test fails.
  - The link break: `catalog.pug:30` changes the label to `View`. Line 89 expects `View Details`, so the
    test fails.
  - The heading break: `catalog.pug:9` changes to `h1 Products`. Line 109 expects `Our Products`, so the
    test fails.

  `tests.md` also records that this failure was observed by hand, in both rounds. So the tests do notice
  the behaviour they claim to prove, and each break changes something its test depends on. What reading
  the files cannot settle is how the gate applies and runs the patches. Only two points are open:
  - The patch files were written by hand, with blank context lines stored as empty lines.
  - Nobody ran `git apply --check` on them. `tests.md` says so, and this session was not allowed to run it
    either.

  Next step: someone who is allowed to run `git apply --check tasks/CAT-01/breaks/*.patch`, or to
  regenerate each patch with `git diff`, should settle it. If a patch does not apply, that is a defect in
  the break artifact, not in the test. No change to the test follows from this reading.
- The `Given` of `catalogue-lists-the-seeded-range` names `seed-catalogue.json`, and this repository has
  no such file. The seed is built in code (`SampleDataInitializer.loadSampleProducts()`). The plan
  records this under `## Open assumptions`, reading the file name as the shared sample catalogue
  (`scenario.catalog.seeded-in-name-order`). The behaviour the scenario asserts is proved: 21 cards from
  the start-up seed. The same finding was dropped in round 1, and nothing has changed since.
- `SeededCatalogE2ETest.java:22` counts card titles, not cards. `catalog.pug:14,21` renders exactly one
  `product-card-title` per `product-card`, so the two counts are the same. Not a defect.
- `HEADING` (`ProductCatalogPageIntegrationTest.java:43`) takes the first `<h1>` in the page. If the page
  ever gains an earlier `<h1>`, the test fails; it cannot pass falsely. Not a defect.
- `cardShowsTheProductsNameDescriptionAndImage` checks the image by `src` only (line 76), not by `alt`.
  The scenario asks for "its image", and the plan states this reading. Not a defect.
- `ProductPageE2ETest`, `ProductDetailPageIntegrationTest` and the page-object additions belong to
  `CAT-02`. No CAT-01 criterion maps to them, so they were not reviewed here.
- `ShopUnderTest` and the edits to `BaseE2ETest`, `BasePage`, `test-e2e.gradle` and `README.md` apply the
  answered decision `CAT-01-01`. In adopt mode only the proof of each scenario is reviewed, so they were
  not reviewed further.

## Criteria re-checked
- catalogue-lists-the-seeded-range: met. `SeededCatalogE2ETest#catalogListsTheSeededProductsInNameOrder`
  opens the catalogue page in the browser against the seeded shop (`ShopUnderTest`) and asserts 21 cards
  (line 22). This is the story's happy path, tested at the browser level.
- catalogue-is-in-name-order: met. `#listsTheCardTitlesInOrdinalNameOrder` renders `GET /products` in the
  wired application. It asserts that the titles are in `String` natural (ordinal) order (line 61) and that
  the first title is `"Bounded Context" Enamel Pin`, after unescaping (line 62).
- card-shows-name-description-and-image: met. `#cardShowsTheProductsNameDescriptionAndImage` picks the
  "Domain-Driven Design" card. It asserts the card's name (line 74), its full seeded description
  (line 75) and its image `src` `/images/products/ddd-book.webp` (line 76).
- card-leads-to-the-product: met. `#everyCardOffersAViewDetailsLinkToThatProductsPage` checks every card
  (line 85). For each one it asserts:
  - the label "View Details" (line 89)
  - a `/products/<uuid>` target (line 90)
  - that the target answers 200 with the card's product name on the page (lines 92-99)
- catalogue-page-heading: met. `#pageIsTitledHeadedAndPlacedAsTheCatalogue` asserts:
  - the `<title>` "Product Catalog" (line 108)
  - the heading "Our Products" (line 109)
  - the breadcrumb "Home / Products" (line 110)

## Previous round
- The previous verdict (`pass`) confirmed no defect, so there is nothing to mark fixed or withdrawn. The
  only new fact since then is the gate's `break-proof` failure. It is handled above under
  `## Considered and dropped`, marked as a suspicion about the break artifacts, not about the tests.
