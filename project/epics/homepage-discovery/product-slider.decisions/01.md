---
id: product-slider-01
story: product-slider
stage: test
asked: 2026-09-26T13:01:26Z
---

# Which title do the slider's browser tests carry: the shared scenario's or the story's key in words?

## Question
The story binds its browser tests to two different names, and the test stage may not choose between them.

- The story's answered assumption (`project/backlog/homepage-discovery/product-slider.md:146`): "their browser
  tests carry the scenario titles verbatim" — the `Title:` lines of `scenario.home.slider-*` in the shared
  specification (`dca-sample-specification/scenarios.md:233-352`). The plan (`tasks/product-slider/plan.md:248-270`)
  names each one, and the .NET twin's delivered tests carry exactly those (`HomeSlider*E2eTest.cs`).
- The story's scenarios have no `Title:` line, so the story gate (contract 9, `test-titles`,
  `story-gate.py:447-466`) expects each end-user test to carry the scenario's **key in words** instead
  ("Card links to product page", "Slider holds eight different products", ...), and refuses all 16 browser tests.

The tests are written with the specification titles as `@DisplayName`. Satisfying the gate as the story stands
would mean writing the key in words into the test file — a criterion key in disguise, which the stage must not
write — or dropping the shared-scenario binding the story asks for.

## Options
- a: Add a `Title:` line under each of the 16 browser-observable scenarios of the story, equal to its shared
  scenario's title (the mapping is in the plan's `## Acceptance criteria`), in both shops' identical story file.
  The tests stay as written; the gate then finds its title. Changes the backlog, not a test.
- b: Drop the binding: the browser tests carry the key in words as display name. Changes 16 tests and loses the
  pairing with the specification and with the .NET suite, whose names would then differ.
- c: Change the gate so a story whose assumption binds tests to shared scenarios reads the titles from there.
  Changes the pipeline (dca-marketplace), not this story.

## Recommendation
a: the story already chose the specification titles; a `Title:` line is the contract's own way to say which title
a scenario carries, and it closes the gap for every later run of this story in either shop.

## Answer
answer: a
by: shop-owner
at: 2026-09-26T13:04:47Z
rationale: the story carries each browser scenario's shared title as its `Title:` line — sixteen lines, taken from `scenario.home.slider-*`, in the one story text both shops share; the tests keep the titles they carry. `SharedScenariosTest` in this sample reads escaped literals as the gate does (`\"` is a quote of the title, `\\` a backslash), listed as a changed test of this story — the same change the other shop made with this story.

## Applied
at: 2026-09-26T13:06:02Z
stage: test
