---
id: pay-through-the-provider-01
story: pay-through-the-provider
stage: plan
asked: 2026-09-25T22:10:50Z
---

# How does the browser suite point the running shop at a payment-provider stub it controls, and arrange a 20.00 EUR total?

## Question

The happy path `authorized-payment-moves-to-review` has to be an `e2e` browser test (profile:
`e2eTest: ./gradlew test-e2e --rerun`, `browser: playwright`). Two of its lines cannot be arranged
or observed against the shop that suite runs against today:

1. "And the payment provider authorizes payments" / "And the payment provider received one payment
   request for 20.00 EUR". The suite drives a shop that was started beforehand
   (`gradle/plugins/test-e2e.gradle`: "They require a running application instance"; profile
   `run: ./gradlew bootRun`; `compose.yaml` service `e2e` runs `test-e2e` against the `shop`
   service). That shop is started without a provider address, so per `project/tech.md`
   `## Integrations` and the story's second assumption it pays through the stand-in. The suite has
   no provider stub the shop calls, so the browser test can neither arrange the provider's answer
   nor count the requests. Without a stub, the browser test for the happy path would pass today
   against the stand-in, so it would never be red, and the test stage requires it to be.
2. "Given a checkout session ... with a total of 20.00 EUR". The seeded prices
   (`product/adapter/incoming/bootstrap/SampleDataInitializer.java`, lines 51–235) and shipping costs
   (`GetShippingOptionsUseCase.java`, lines 28–45: 4.99 / 9.99 / 19.99 / 0.00) all end in .99 or
   .00 and add up to no total of exactly 20.00. The only way to add a product is
   `POST /api/products`, guarded by `hasRole('STAFF')` (`ProductResource.java`), and the suite
   holds no staff token.

Both are decisions about how the shop under the end-user suite is run, so they belong to the
stack, not to one story. The integration level has neither problem: a `@SpringBootTest` in
`src/test-integration` can set the provider address to a WireMock (`http.stub: wiremock`,
`HttpStubSmokeTest`) and create a product at a price of its choice.

## Options

A. **The browser suite owns the stub, and the shop under it is started against that stub.** The
   e2e suite starts a WireMock on a fixed port read from a system property (for example
   `e2e.paymentProvider.port`, default 8089, passed like `e2e.baseUrl` in `test-e2e.gradle`). The
   shop under test is started with the provider address set to it: the profile's `run:` line,
   the `shop` service in `compose.yaml`, and the README instructions for `test-e2e` change. The
   happy-path test asserts one `POST /payments` whose amount is the total the review page shows.
   The literal "20.00 EUR" is held by an integration test under the same key, which can arrange
   that total. Cost: the shop under the browser suite no longer uses the stand-in, and whoever
   starts it by hand for the suite has to pass the address.
B. **Option A, with a seeded article that makes 20.00 EUR reachable.** For example a product at
   15.01 EUR plus standard shipping at 4.99 EUR. The browser test then arranges the literal total
   itself. Cost: an item in the sample catalogue that exists only for a test, which
   `project/product.md` `## Not part of the product` warns against ("Anything that exists only to
   look like a feature").
C. **The happy path goes to the HTTP level for this story.** A `@SpringBootTest(webEnvironment =
   RANDOM_PORT)` in `src/test-integration` drives the payment page's form post against a WireMock
   provider. This covers every line of the scenario literally, but it sets aside the plan rule that
   the happy path gets a browser test where the project has a browser runner. The journey item the
   epic names ("the flow from the cart through payment to the confirmation") would still need
   option A later.

## Recommendation

Option A. It gives the browser suite a stub it controls for this story and for the journey the epic
already announces. It keeps the catalogue free of test-only items, and it leaves the literal
amount at the level that can arrange it. The runtime change (`run:`, `compose.yaml`) should be
recorded as a stack decision, not taken silently inside the story.

## Answer
answer: The end-user suite starts the shop itself, in the test process and on a free port (done in this sample's e2e base since 2026-09-26). The payment provider's stub belongs to the suite: it starts with the shop, the shop's provider address points at it, and each test arranges the stub's answers. The story no longer names 20.00 EUR: the provider receives one payment request for the checkout session's total.
by: Christoph Bloemer
at: 2026-09-26T06:08:25Z
rationale: An end-user suite that drives a shop started beforehand makes the gate depend on whatever answers on that address; a suite that starts its own shop brings every dependency, the stub included.

## Applied
at: 2026-09-26T06:20:12Z
stage: plan
