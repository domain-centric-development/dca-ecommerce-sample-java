---
story: the-order-confirmation-findings-are-closed
epic: guest-checkout
---

# Findings — the-order-confirmation-findings-are-closed

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/incoming/web/checkoutcompletion/ConfirmationPageViewModel.java:11 | minor | The page's view model still describes itself with the name the glossary keeps for the mail. | domain-term: the class comment reads "ViewModel for the order confirmation (thank you) page", the wording the story removed from the controller beside it; the glossary note gives the term to the mail alone. Outside the diff, raised by ddd and clean-code alike. | Reword to "ViewModel for the confirmation (thank you) page of a placed order". | open |
