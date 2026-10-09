---
id: product-slider-02
story: product-slider
stage: test
asked: 2026-09-26T13:19:29Z
---

# The gate's title check reads the raw source, so a title with a quote in it never matches its escaped display name

## Question
With decision product-slider-01 applied, 15 of the 16 browser tests pass the story gate's `test-titles` check.
The one that does not is `HomeSliderE2ETest#showsTheDiscoverProductsSliderDirectlyBelowTheHero`: its scenario's
`Title:` line (`project/backlog/homepage-discovery/product-slider.md:25`) is
`The homepage shows a "Discover products" slider directly below the hero`, and Java can only carry it as
`@DisplayName("The homepage shows a \"Discover products\" slider directly below the hero")`
(`HomeSliderE2ETest.java:27`). The runtime display name is the title verbatim — the same gate's `tests-red`
check finds the test in the report by that unescaped name.

`check_titles` (`.agents/factory/story-gate.py:447-466`, identical in the marketplace's 0.40.0) tests
`titles[key] not in read_text(path)`: the raw file text, where the quotes are escaped. Its neighbour
`display_name_of` (`story-gate.py:1525`) already reads the declaration and applies `unescape_literal`. The
.NET twin's test (`HomeSliderE2eTest.cs:15`) carries the same escaped literal and hits the same check.

The test stage cannot close this itself: the gate is pipeline code, and every in-test workaround (a text block,
a `\u0022` escape, concatenation) either still is not the title in the raw text or breaks the display-name
readers of the gate and of `SharedScenariosTest`.

## Options
- a: Fix the gate: `check_titles` compares the declared display name (`display_name_of(path, method)`, which
  unescapes) and falls back to the raw text. Changes the pipeline (dca-marketplace, version bump), then
  `/factory-update` in both shops; no test changes.
- b: Change the shared title so it carries no quote (e.g. `The homepage shows the Discover products slider
  directly below the hero`) in the specification's `scenario.home.slider-*`, the story's `Title:` line in both
  shops, and both suites' display names (changes a test in each shop).
- c: Accept the fail for this one check on this story and let the runner go on. Leaves the gate refusing every
  later title with a quote or backslash.

## Recommendation
a: the title is right and the test carries it; the gate's check is the one place that reads the literal without
unescaping it, and fixing it there closes the gap for every stack and every later story.

## Answer
answer: a
by: shop-owner
at: 2026-09-26T13:28:59Z
rationale: a defect of the gate, fixed at the source: `check_titles` compares the title with the unescaped test source, as `display_name_of` already reads the name a report carries (dca-factory 0.40.1, with a verify case for a title with a quote); this project's gate is updated to it. The tests stay as written.

## Applied
at: 2026-09-26T13:35:58Z
stage: test
