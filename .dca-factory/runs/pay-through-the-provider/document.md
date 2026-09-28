# Document — pay-through-the-provider

Carriers used as the profile names them: `carrier.glossary: ubiquitous-language` for the checkout
glossary, `carrier.domain: context-map` for the designed map. `dca-knowledge` (named as
`knowledge:`) was not asked: no statement written here depended on a pattern question. The plan had
already settled both pattern questions and cited their nodes. The story adds no context and no
relationship, and it changes no relationship's pattern. The designed map needed only its
translation text brought in line.

## Glossary
| Term | Context | Added or changed | Definition source |
| --- | --- | --- | --- |
| `PaymentResult` (outcomes `SUCCEEDED` / `REFUSED` / `UNAVAILABLE`: authorized payment, refused payment, unavailable provider; payment request; payment stand-in) | checkout | added, `src/main/java/dev/domaincentric/sample/ecommerce/checkout/domain/glossary.md` section `### PaymentResult` | the plan's `## Glossary proposals` (payment request, authorized payment, refused payment, unavailable provider, payment stand-in), checked against `PaymentProvider.PaymentResult.Outcome` in `src/main/java/dev/domaincentric/sample/ecommerce/checkout/application/shared/PaymentProvider.java` and `RestPaymentProvider.translate` in `src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/outgoing/payment/RestPaymentProvider.java` |
| `PaymentProviderId` | checkout | changed: it names both ids, `provider` where `checkout.payment-provider.base-url` is set and `mock` otherwise (judge finding 3) | `RestPaymentProvider.PROVIDER_ID` (`"provider"`), `MockPaymentProvider.PROVIDER_ID` (`"mock"`), and the two `@ConditionalOnExpression` lines in the story diff |
| `PaymentProviderNotFound · PaymentProviderUnavailable · PaymentInitiationFailed` | checkout | changed: the provider's reason is kept for logs and callers, and the payment page shows its own fixed message (judge finding 2) | `PaymentPageController.submitPayment` catch blocks and the `PAYMENT_REFUSED` / `PROVIDER_UNAVAILABLE` constants (story diff) |

## Documents updated
| File | What changed | Verified by |
| --- | --- | --- |
| `src/main/java/dev/domaincentric/sample/ecommerce/checkout/domain/glossary.md` | the three rows above | read the glossary before and after the edit. `rg -i "PaymentResult\|payment request\|stand-in" --glob glossary.md src` found no other context defining these terms |
| `src/main/java/dev/domaincentric/sample/ecommerce/checkout/application/shared/PaymentInitiationFailedException.java` | JavaDoc only: the reason is carried for logs and callers, and the page shows its own message (judge finding 2). No code change | `PaymentPageController.java` catch block (story diff lines 128-130). `./gradlew spotlessCheck`: passed |
| `project/domain.md` | row "Payment Service Provider", Translation cell: `RestPaymentProvider` where `checkout.payment-provider.base-url` is configured, the stand-in `MockPaymentProvider` otherwise. The pattern, the planned webhook and the reason are unchanged | the plan's `## Context` finding. Both classes exist under `src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/outgoing/payment/` (glob). The generated `docs/architecture/context-map.md:93` says the same |
| `docs/architecture/context-map.md` | not touched here. The build stage regenerated it from `src/main/java/dev/domaincentric/sample/ecommerce/checkout/package-info.java` through `ContextMapDocumentationTest` | the story diff (`context-map.md` hunk matches the `@ExternalUpstream` `exchanges`/`rationale` hunk). `build.md` `## Checks`: `test-architecture` passed |
| `project/product.md` | `## Qualities`: "a stand-in in the sample, unless a provider address is configured" | the plan's `## Context` finding. `src/main/resources/application*.yml` sets no `checkout.payment-provider.base-url` (rg: no match) |
| `README.md` | checkout tree: `infrastructure/` with `CheckoutDomainConfiguration.java` and `CheckoutPaymentConfiguration.java`, and `adapter/outgoing/payment/` with `RestPaymentProvider.java` and `PaymentProviderProperties.java`. `### Running the Application`: the stand-in by default, and how to give a provider address (`--args='--checkout.payment-provider.base-url=…'`), pointing at `project/tech.md` `## Integrations` | glob of `src/main/java/dev/domaincentric/sample/ecommerce/checkout/{adapter/outgoing/payment,infrastructure}/*` lists all five files. `project/tech.md:26` is `## Integrations`. The property prefix comes from `PaymentProviderProperties` (`@ConfigurationProperties(prefix = "checkout.payment-provider")`) |
| `docs/architecture/package-structure.md` | checkout `adapter/outgoing/payment/`: `RestPaymentProvider, PaymentProviderProperties` | same glob |

## Not documented
- `project/product.md` `## Not part of the product` still lists "Real payment". The shop ships no
  provider address, so as delivered it still pays through the stand-in. Whether "real payment" now
  belongs in the product is a product decision for the person who owns `project/product.md`. It is
  not a documentation fix.
- Judge finding 1 (`PaymentResult.failure(..)` yields `REFUSED`, including for the confirmation and
  cancellation the REST adapter never asks about): this is a code question, whether to rename to
  `refused(..)` or add an outcome. Renaming is a story or refactoring of its own. The glossary states
  today's behaviour in the `PaymentResult` notes.
- The request and answer field names (`amount`, `currency`, `reference`), and "any other status or a
  connection failure counts as unavailable": the plan assumed these, and `project/tech.md` does not
  fix them. They are documented in the adapter's JavaDoc and the glossary entry. `project/tech.md`
  was left as the owner's text; widening its contract is the owner's call.
- The README's `### Running with Docker` and the compose `e2e` service: no provider address is
  wired there, so they keep the stand-in. Nothing in them became wrong.
- `docs/architecture/README.md` and `docs/architecture/architecture-principles.md`: their text was
  not searched beyond the rg over `docs/**/*.md` for `MockPaymentProvider|mock adapter|PaymentProvider|payment-provider|adapter/outgoing/payment`. That search found no statement the story made wrong, apart from the two files updated above.
- No ADR: the outbound ACL was already designed (`project/domain.md`, `@ExternalUpstream`). The
  story implemented it and took no new architectural decision.
