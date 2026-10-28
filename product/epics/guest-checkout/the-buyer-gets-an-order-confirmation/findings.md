---
story: the-buyer-gets-an-order-confirmation
epic: guest-checkout
---

# Findings — the-buyer-gets-an-order-confirmation

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | ddd | product/spec/checkout/glossary.md:48 | minor | The glossary now says "order confirmation" means only the mail, yet the confirmation page still calls its data the order confirmation. | domain-term: the new `### Order confirmation` note says the term is the mail to the buyer and not the confirmation step, while `ConfirmationPageController.java:179` puts the page's view model under `orderConfirmation`, which `confirmation.pug:22` (touched by this story) reads — one term, two meanings in checkout. | Rename the page's model attribute to a step-side name (e.g. `confirmedCheckout`) in the controller and `confirmation.pug`, or name the page attribute as a known exception in the glossary note. | open |
