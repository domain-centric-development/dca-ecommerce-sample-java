# AGENTS.md

This file provides guidance to AI coding agents (Claude Code, Codex, and others) when working with code in this repository.

## Principles that apply in every DCA repository

They are in the monorepo's root `AGENTS.md` — the samples exist to make the agent harness
deterministic rather than to ship features, the rules, markers and catalog stay general and
framework-neutral, each reader artifact stands alone, and a generated artifact is never hand-edited.
Read them there; this file does not restate them, because two copies drift.

## Where the detail is

This file stays short: some tools stop reading project documents at 32 KiB and truncate without
saying so, and an instruction past that point does not exist for them.

| Read it before | File |
|---|---|
| writing code here | [`docs/code-standards.md`](docs/code-standards.md) — conventions, practices, the patterns review rejects |
| changing anything documented | [`docs/agent-playbook.md`](docs/agent-playbook.md) — which document a change pulls along, the recurring tasks step by step, troubleshooting, the shared semantics both samples are held to |
| touching the architecture | [`docs/architecture/`](docs/architecture/) — principles, the ADRs and their index, the context map |
| taking a decision | [`docs/architecture/adr/adr-template.md`](docs/architecture/adr/adr-template.md) |

## Documentation Language

**⚠️ All documentation files (`*.md`, JavaDoc, code comments, and any other written artifacts checked into the repository) MUST be written in English.**

This applies to:
- All files under `docs/`
- `README.md` and all other top-level Markdown files
- JavaDoc and inline code comments
- Commit messages and PR descriptions
- ADRs, glossaries, context maps, and any generated documentation

Conversational replies to the user may follow the user's language preference, but persisted artifacts are always English to keep the reference implementation accessible to an international audience.

## Build Commands

```bash
# Build & Test
./gradlew build                              # Build project (compile + tests)
./gradlew build -x test                      # Build without tests
./gradlew test                               # Run unit tests (JUnit 5)
./gradlew test --tests "*ProductTest*"       # Run specific test class
./gradlew test -Pfilter=Cart                 # Filter tests by name substring
./gradlew test-architecture                  # Run ArchUnit architecture tests
./gradlew test-architecture --tests "*ArchitectureRulesTest*"  # DCA rule catalog only

# Run Application
./gradlew bootRun                            # Start app (JDWP debug on port 5005)
docker compose up --build                    # Same shop in a container, http://localhost:8080
docker compose run --rm test                 # Tests without a local JDK
./gradlew -Plog-debug bootRun                # Start with debug logging

# Debugging tests
./gradlew -Plog-debug test                   # Tests with verbose output
./gradlew -Plog-debug test-architecture      # Arch tests with verbose output
```

**Test Reports:**
- Unit tests: `build/reports/test/`
- Architecture tests: `build/reports/test-architecture/`

## Project Overview

This is a **sample e-commerce application** demonstrating best practices for:

- **Domain-Driven Design (DDD)** - Strategic and tactical patterns
- **Hexagonal Architecture** - Ports and Adapters pattern
- **Onion Architecture** - Dependency inversion with layers pointing inward
- **Clean Architecture** - Framework-independent business logic

**Tech Stack:**
- Java 25
- Spring Boot 4.0.2
- Gradle 9.3.1
- Spring Modulith 2.0.3
- Spring AI 2.0.0-M2 (milestone)
- `dev.domaincentric:dca-building-blocks` — architectural markers (DDD tactical/strategic, hexagonal ports)
- `dev.domaincentric:dca-archunit` — the DCA governance rules (ArchUnit), run via JUnit 5
- Both come from Maven Central (`dca-building-blocks` 0.2.0, `dca-archunit` 0.4.0). Working on unreleased rules or markers: `./gradlew -PwithDcaJava <task>` makes `settings.gradle` include the sibling build `../dca-java` and substitute the coordinates — **run the build once without the switch before calling anything done**: it hides what a stranger sees, and CI (`.github/workflows/ci.yml`) exists because the sample once matched no published rule version for a day. The composite build also compiles `dca-java`, whose toolchain is Java 21: install a JDK 21 beside JDK 25, or pass `-PjavaToolchain=25`
- JSpecify for nullability annotations

**Purpose:**
This project serves as a reference implementation showing how to properly structure an enterprise application using modern architectural patterns.

## Development Workflow

### Standard Development Process

1. **Understand the requirement**
   - Read existing code and documentation
   - Identify affected components
   - Plan the changes

2. **Make code changes**
   - Follow DDD patterns
   - Maintain architectural boundaries
   - Write clean, well-documented code

3. **Update documentation** ⚠️
   - This is NOT optional
   - Update `README.md` if structure or features changed
   - Update `docs/architecture/README.md` if package structure changed
   - Update `docs/architecture/package-structure.md` if package structure changed
   - Update `docs/architecture/architecture-principles.md` for pattern changes
   - Add examples from your actual changes
   - Update any affected diagrams or references

4. **Run tests**
   - `./gradlew build` - Compile and build
   - `./gradlew test-architecture` - Verify architecture rules
   - Ensure all tests pass

5. **Verify the application**
   - `./gradlew bootRun` - Start the application
   - Test the changes manually if needed

### Architecture Test Failures

If architecture tests fail:

1. **Understand the violation** - Read the error message carefully
2. **Determine if the change is correct**:
   - If the code violates architecture rules → Fix the code
   - If the rule is too strict → Discuss with the team before changing tests
3. **Never disable tests** without documenting why
4. **Update documentation** to reflect any architectural decisions

---

## Testing Requirements

### Architecture Tests (ArchUnit)

Location: `src/test-architecture/java/dev/domaincentric/sample/ecommerce/`

The rules themselves live in the library `dev.domaincentric:dca-archunit` (rule counts: see the library's `rules.json`; ids
`DCA-<SET>-<NNN>`: LAY, ONI, HEX, TAC, STR, MAP, ADV, USE, NAM, CYC). This project only *runs* them:

- `ArchitectureRulesTest` — extends `DcaArchitectureTest`, one dynamic test per rule, grouped into a
  container per rule set; excludes nothing. Its `additionalSelection()` shows the configuration a
  consuming project would use (`DcaRuleSelection`: scope, severity, exceptions, freeze baseline).
- `EcommerceLayout` — the base package and `DcaLayout` all three tests share.
- `ContextMapDocumentationTest` — renders `docs/architecture/context-map.md` via `ContextMapRenderer`
  and fails when the committed file was stale (fix: commit the regenerated file).
- `SpringModulithVerificationTest` — Spring Modulith module boundaries via `DcaSpringModulithTest` from
  `dca-archunit-spring-modulith` (Modulith's own analyzer, not a DCA rule; the base class excludes these test
  classes from Modulith's root module).

To switch a rule off, return `DcaRuleSelection.all().excluding("<id>", "<reason>")` from
`additionalSelection()` in `ArchitectureRulesTest` — the rule then shows up as an aborted test with
that reason — and record the decision in an ADR. The same is configurable without code in
`dca-archunit.properties` on the test class path; never override `selection()` itself, which would
replace that file instead of adding to it.
Rule changes belong in `dca-java`, not here.

**Run architecture tests:**
```bash
./gradlew test-architecture
```

### Unit Tests

- Test domain logic in isolation
- Mock external dependencies
- Focus on business rules and invariants

### Integration Tests

- Test application services with real repository implementations
- Verify event publishing and handling
- Test REST endpoints

---

## Delivery pipeline

New work runs through the factory pipeline from `dca-marketplace/plugins/dca-factory`, installed
into this repository (the skill links in `.claude/`, `.codex/`, `.opencode/` are gitignored; a clone
gets them with `factory.sh update`). The pipeline's own instructions are in its section at the end of
this file; what is to be built is in the project description below it.

- `project/product.md`, `project/tech.md`, `project/domain.md` — the project description: the product,
  the technical decisions, the designed context map (the generated one is
  `docs/architecture/context-map.md`)
- `project/backlog/<epic>/<story>.md` — new stories; `tasks/prd.json` keeps the delivered 146 as history
- `.agents/factory/factory.profile.yaml` — the only file that tells the pipeline how this project
  builds: `./gradlew testClasses|test|test-e2e|test-architecture|spotlessCheck|spotlessApply`, plus
  the knowledge source (`dca-knowledge`), the carriers and the review perspectives
- `.agents/factory/story-gate.py` — the gate between the stages
  (`--story <id> --stage plan|test|build|tidy|document`)
- `.githooks/pre-commit` — `factory.sh check --staged` on every commit
  (`git config core.hooksPath .githooks`); narrow it with
  `FACTORY_PRECOMMIT_CHECKS="compile architecture"` when the full suite is too slow to wait for

Run one story with `/factory-run <story id>`. The pipeline owns the process; the architecture comes
from `dca-core` (`/dca-init` once, then `/dca-modelling`, `/dca-review`, `/dca-knowledge`) and the
craft from `dca-craft` (`/ubiquitous-language`, `/context-map`, `/review-ddd`, `/review-hexagonal`,
`/review-clean-code`, `/e2e-testing`).

### Design Decisions

For significant architectural decisions, create an Architecture Decision Record (ADR) in
`docs/architecture/adr/` from `docs/architecture/adr/adr-template.md`.

<!-- dca-describe: start -->
## Project description

What is to be built is described in three files. Read them before any implementation — in a
delivery pipeline or by hand — and treat a contradiction between them and a change as a finding,
not as something to fix in the code:

- product: `project/product.md` — what is built, for whom, surfaces, how it works, how it looks,
  qualities, what it is not
- tech: `project/tech.md` — stack, frontend approach, persistence, runtime, integrations,
  version policy
- domain: `project/domain.md` — the designed bounded contexts, their subdomain types and
  relationships; the map generated from the code shows what was built
<!-- dca-describe: end -->

<!-- dca-factory: start -->
## Delivery pipeline

This project delivers stories through the dca-factory pipeline. At the start of a session, unless the
person names a task right away, run `python3 .agents/factory/story-gate.py --status --brief`, show
its lines, and ask what they want to do: write or release a story (`/factory-backlog`), answer a
waiting question (`/factory-decisions`), work the backlog (`/factory-run`; to keep listening, a tool
that repeats a prompt runs it again — in Claude Code `/loop /factory-run`), look closer (`/factory-status`), or
learn how the factory works (`/factory-help`).

An instruction that changes what an actor can see or do is a user story. Before any code, ask once, in
these words: "As a story through the factory — to an existing epic, a new epic — or directly by hand?"
For the factory, run `/factory-run` with the person's words: it writes the story through
`/factory-backlog` and runs it once it is released. A fix, a refactoring, documentation, tooling or a
question is done directly.

A session never runs `factory.sh run` — it starts a tool process per stage. One worker per checkout: a
managing session writes backlog and decision files only. Every change — by a stage or by hand in a
session — passes `bash .agents/factory/factory.sh check` before it is committed; the commit hook runs it
on what is staged.
<!-- dca-factory: end -->
