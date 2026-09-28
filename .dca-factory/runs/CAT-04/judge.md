# Judge — CAT-04

## Verdict
verdict: pass

## Perspectives covered
Adopt mode (`status: adopted`): nothing was built. The change is the claim that each mapped test
proves its scenario. As the stage prescribes, only that claim was reviewed. The diff
(`tasks/CAT-04/.verify/story.diff`) holds three new test files and no production code, so the
perspective passes had no model, port or adapter to review.
- ddd: in-session, adopt-mode reading only (`review-ddd` is named by `review.ddd:`, but no model
  changed)
- hexagonal: in-session, adopt-mode reading only (`review-hexagonal` is named by
  `review.hexagonal:`, but no port or adapter changed)
- clean-code: in-session, adopt-mode reading of the three new test files (`review-clean-code` is
  named by `review.clean-code:`; the only question reviewed was whether each test proves its
  scenario)
- dca (added, `review.dca: dca-review`): not covered. Adopt mode reviews only the claim that each
  test proves its scenario, and the diff holds no production code.
- knowledge (`dca-knowledge`): not consulted, because no pattern or rule question came up.

## Confirmed defects
| Perspective | File:line | Severity | Defect | Fix |
| --- | --- | --- | --- | --- |

None.

## Considered and dropped
- The integration tests read the rendered HTML as text, but the "end-user test reads markup
  instead of driving the browser" finding does not apply. These are integration tests at the level
  the plan set for every scenario except the happy path (`plan.md`, "Acceptance criteria", levels),
  and the happy path runs in Playwright (`HomePageE2ETest`).
- `shop-now-opens-the-catalogue`: "below" is proved as membership of the `cta-section`, not by
  position. Inside that section the link follows the heading and the sentence, so treating the
  section as the unit is a fair reading. The previous round dropped this too. It is not blocking.
- `section(...)` matches up to the first `</section>` (`HomePageIntegrationTest.java:51-58`). A
  nested section would cut the match short. None of the four sections the tests read nests one
  before the asserted content, so this is not a defect today.
- The regex in `link(...)` needs `href` before `data-test` (`HomePageIntegrationTest.java:68-75`).
  That ties the test to the template's attribute order. If the order changed, the test would fail
  loudly, not pass falsely, so it is not a defect in the claim.
- The break patches were not applied one at a time with `git apply` (`tests.md`, Notes). That is a
  concern for the gate and the test stage, not a defect in any test's assertion.

## Criteria re-checked
- home-page-welcomes-the-visitor: met. Playwright opens `/` on the shop the suite starts and asserts
  the heading, subtitle and description with the scenario's exact values
  (`HomePageE2ETest.java:16-25`, `pages/HomePage.java:36-61`).
- home-page-title: met. The `<title>` of `GET /` equals "domaincentric.commerce"
  (`HomePageIntegrationTest.java:46`).
- browse-products-opens-the-catalogue: met. The label "Browse Products" is asserted, the link's
  `href` is followed, and the target's `h1` is "Our Products" (`HomePageIntegrationTest.java:52-57`).
- view-cart-links-to-the-cart: met. The label "View Cart" and `href` `/cart` are asserted inside the
  hero, after its `</h1>` (`HomePageIntegrationTest.java:62-67`).
- why-shop-with-us: met. The heading and exactly the four title–text pairs are asserted, in order
  (`HomePageIntegrationTest.java:72-93`).
- popular-categories: met. The heading and exactly the five title–text pairs are asserted, and the
  section holds no `<a ` (`HomePageIntegrationTest.java:98-122`).
- shop-now-opens-the-catalogue: met. The heading, the sentence and the label "Shop Now" are asserted,
  the link is followed, and the target's `h1` is "Our Products"
  (`HomePageIntegrationTest.java:127-135`).

## Previous round
- `browse-products-opens-the-catalogue` never checked the label "Browse Products" (major, previously
  at `HomePageIntegrationTest.java:164`): fixed. The test now asserts
  `linkLabel(hero, "browse-products-link")` equals "Browse Products" before it follows the link
  (`HomePageIntegrationTest.java:54`), the same way the Shop Now test checks its label. According to
  `tests.md` (Notes, "Browse Products"), the break patch now renames the label to "Shop", and only
  this test fails ("expected "Browse Products", but was "Shop"", the other 5 of the class passed).
