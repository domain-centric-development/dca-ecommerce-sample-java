---
story: reduced-motion-stills-the-transitions
epic: fits-its-surroundings
---

# Findings — reduced-motion-stills-the-transitions

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Defect | Fix | Status |
|---|---|---|---|---|---|---|
| 1 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ReducedMotionE2ETest.java:77 | minor | the test drops from page-object calls into a raw `window` poll, an unchecked cast and the string keys `ran`/`animated`, which the init script spells a second time | move the read into `ProductDetailPage` as a method returning a small record (`ran`, `animatedParts`) | open |
