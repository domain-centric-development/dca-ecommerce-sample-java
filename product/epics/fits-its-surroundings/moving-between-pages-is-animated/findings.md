---
story: moving-between-pages-is-animated
epic: fits-its-surroundings
---

# Findings — moving-between-pages-is-animated

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Defect | Fix | Status |
|---|---|---|---|---|---|---|
| 1 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/PageTransitionE2ETest.java:178 | minor | The reduced-motion test builds its context from `new Browser.NewContextOptions()`, which bypasses the base class's `contextOptions()` hook (`BaseE2ETest.java:111`) | Build it from `contextOptions().setReducedMotion(ReducedMotion.REDUCE)` | done (c137d3a) |
