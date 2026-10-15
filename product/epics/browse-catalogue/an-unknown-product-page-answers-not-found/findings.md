---
story: an-unknown-product-page-answers-not-found
epic: browse-catalogue
---

# Findings — an-unknown-product-page-answers-not-found

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductPageController.java:90 | minor | The product page says "not found" by changing a passed-in response instead of returning its whole answer. | `showProductDetail` takes `HttpServletResponse` only to call `setStatus(SC_NOT_FOUND)` on it (output argument); no other page controller pulls the Servlet API into a handler signature. The hexagonal review's nit on the same line is the same point. | Return `new ModelAndView("error/404", HttpStatus.NOT_FOUND)` from the not-found branch (a `ModelAndView` on the found branch too) and drop the `response` parameter. | open |
| 2 | clean-code | src/test-integration/java/dev/domaincentric/sample/ecommerce/product/ProductNotFoundPageIntegrationTest.java:61 | minor | A test comment still says no scenario names a status for the not-found page, which is no longer true. | `// No status is asserted: the scenarios name none, only what the page shows.` is outdated now that `a-malformed-product-address-answers-404` and `an-unknown-product-answers-404` name status 404. | Change it to "No status is asserted here; `ProductPageNotFoundStatusIntegrationTest` holds the 404", or delete it. | open |
