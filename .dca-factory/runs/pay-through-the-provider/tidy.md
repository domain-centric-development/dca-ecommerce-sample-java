# Tidy — pay-through-the-provider

Done in this session. The profile names no `carrier.tidy`, so the stage followed its own
description. The guard `carrier.guard: dca-discipline` was applied to the one file written. That
file is an outgoing adapter (`adapter/outgoing/payment/`): it still implements the
`PaymentProvider` port and adds no domain concept. The provider's wire types (`PaymentRequest`,
`PaymentAuthorization`) stay private to it, and nothing framework-related moved inward.

## Moves
| File | Move | Why it reads better |
| --- | --- | --- |
| `src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/outgoing/payment/RestPaymentProvider.java` | Moved the 20-line `exchange` lambda into a private `translate(response, sessionId)` method, and read the status code once into a local `HttpStatusCode status`. | `initiatePayment` now reads at one level: build the request, send it, translate the answer, or count a transport failure as unavailable. The anti-corruption translation (`402` → refused, not `201` → unavailable, `201` without a reference → unavailable, `201` → success) has its own named method, where the plan's "all translation lives here" can be seen. Every branch, message and log line is unchanged. |

## Left alone
- `PaymentResult.failure(..)` now means *refused*, beside the new `unavailable(..)`. A name like
  `refused(..)` would say more. But the plan says the factory stays, and existing tests and the
  stand-in call it. It is a naming question for the judge or glossary, not a tidy.
- The key `checkout.payment-provider.base-url` is written three times: in the two
  `@ConditionalOnExpression` strings (`RestPaymentProvider`, `MockPaymentProvider`) and as the
  `@ConfigurationProperties` prefix. Lifting it into one constant would save little, and it would
  make the two opposite conditions harder to read side by side.
- `SubmitPaymentUseCase` checks `outcome() == UNAVAILABLE` and then `!success()`. A `switch` over
  `Outcome` would be exhaustive, but the two `if`s are short and match the file's style. Left as it
  is.
- `PaymentPageController`: the two new `catch` blocks repeat `return "redirect:/checkout/payment"`.
  They differ in the message only, and folding them would hide which failure maps to which text.
- As `build.md` already notes, the JavaDoc of `PaymentInitiationFailedException` still says the
  customer sees the provider's reason. That type is outside this story's changed files, so it is
  for the document stage.

## Checks
- `./gradlew spotlessApply` then `./gradlew testClasses`: passed
- `./gradlew test-integration --rerun --tests '*ProviderPaymentIntegrationTest*'` (right after the move): passed
- `./gradlew test --rerun`: passed
- `./gradlew test-integration --rerun`: passed
- `./gradlew test-e2e --rerun`: passed
- `./gradlew test-architecture`: passed
- `./gradlew spotlessCheck`: passed
