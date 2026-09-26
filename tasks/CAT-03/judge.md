# Judge — CAT-03

Adopt mode (`status: adopted`): nothing was built, so the review is whether each mapped test proves
its scenario. Read from the story, `plan.md`, `tests.md`, `.verify/story.diff`, and
`src/main/resources/templates/error/404.pug`, which I read to check the selectors and texts the
tests rely on. `build.md` does not exist, because adopt mode has no build stage. There is no
`.judge-previous.md`: this is the first round.

## Verdict
verdict: pass

## Perspectives covered
- ddd: in-session. `review-ddd` was not invoked, because adopt mode reviews only the tests against
  their scenarios and there is no model change to review.
- hexagonal: in-session, for the same reason. `review-hexagonal` was not invoked, and no adapter,
  port or dependency changed.
- clean-code: in-session, applied to the three new test files. `review-clean-code` was not invoked.
- dca (added): not covered. Adopt mode reviews nothing but the scenario proofs, and the change has
  no production code for `dca-review` to review.

`dca-knowledge` (the profile's knowledge source) was not consulted, because no finding turned on a
pattern or rule question.

## Confirmed defects
| Perspective | File:line | Severity | Defect | Fix |
| --- | --- | --- | --- | --- |
| — | — | — | none | — |

## Considered and dropped
- **The integration test reads markup as text** (`ProductNotFoundPageIntegrationTest.java:30`,
  `:88`). The rule against reading markup as text covers end-user tests. This is an integration
  test at the level the plan chose, and the story marks only
  `unknown-product-shows-the-not-found-page` as its happy path. That scenario is driven in the
  browser (`ProductNotFoundE2ETest`), so the test levels match the rule.
- **"When they follow" is done by requesting the link's `href`** (`ProductNotFoundPageIntegrationTest.java:48`,
  `:58`). The test first asserts the link's label, then requests its real target and asserts the
  target's `h1`. A break in the label, the target or the destination heading makes it fail, so it
  proves the scenario at integration level.
- **The link regex depends on attribute order** (`ProductNotFoundPageIntegrationTest.java:87-89`:
  `href` has to come before `data-test`). This is fragile, but `404.pug:11-12` renders the
  attributes in that order and the `find()` assertion fails loudly if it changes. It is a
  robustness preference, not a defect in the proof.
- **The page object reads the code, heading and message by CSS class** (`NotFoundPage.java:44`,
  `:53`, `:62`). The product description says every element a test addresses carries a `data-test`
  attribute (`project/product.md`, look and feel), and only the two links do (`404.pug:11-12`).
  That gap is in the markup, not in the test. Adopt mode changes no production code, and the plan
  already records it as an open assumption. It is a candidate for a later story, not a finding
  against this change.
- **No HTTP status is asserted on the not-found page** (`ProductNotFoundPageIntegrationTest.java:62`).
  No scenario names a status. The page renders with 200 today, which the plan records as an open
  assumption.
- **The Given "no product has the id `no-such-product`" is not asserted separately.** The Then
  already proves it: the not-found page appears only when the use case returns `notFound()`.

## Criteria re-checked
- unknown-product-shows-the-not-found-page: met. The browser opens `/products/no-such-product`
  (`NotFoundPage.java:33`) and the test asserts "404", "Page Not Found" and the full message with
  the scenario's wording (`ProductNotFoundE2ETest.java:18-23`). The whitespace normalisation only
  undoes the source line break in `404.pug:8-9`. The recorded break (shortened message) makes the
  test fail.
- not-found-page-has-the-shop-title: met. The test asserts the `<title>` of the rendered page is
  exactly "domaincentric.commerce" (`ProductNotFoundPageIntegrationTest.java:37`). The recorded
  break (title "Not Found") makes the test fail.
- browse-all-products-leads-to-the-catalogue: met. It asserts the label "Browse All Products", then
  the `h1` "Our Products" of the page the link leads to
  (`ProductNotFoundPageIntegrationTest.java:45-47`).
- go-to-homepage-leads-to-the-home-page: met. It asserts the label "Go to Homepage", then the `h1`
  "Welcome to domaincentric.commerce" of the page the link leads to, with the `span` markup around
  ".commerce" dropped (`ProductNotFoundPageIntegrationTest.java:55-58`, `:95`).
