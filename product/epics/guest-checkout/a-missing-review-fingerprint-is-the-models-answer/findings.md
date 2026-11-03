---
story: a-missing-review-fingerprint-is-the-models-answer
epic: guest-checkout
---

# Findings — a-missing-review-fingerprint-is-the-models-answer

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/checkout/application/checkoutcompletion/confirmcheckout/ConfirmCheckoutCommand.java:11 | minor | The command's comment sends the reader elsewhere instead of saying what a missing fingerprint leads to. | Javadoc "the review fingerprint answers it" names who decides, not what holds; the page controller's Javadoc (:82-83) says it plainly | Write "`null` when none was sent, which matches no review" | open |
| 2 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/checkout/domain/model/ReviewFingerprint.java:23 | minor | One sentence in the fingerprint's description is ungrammatical. | "A value not in that form, and a missing one, is …" — plural subject, singular verb | "A value not in that form, or a missing one, is a fingerprint of no review." | open |
