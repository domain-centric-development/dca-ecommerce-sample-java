# Document — product-slider

Carrier: glossary entries in the format of `carrier.glossary: ubiquitous-language`
(`.claude/skills/ubiquitous-language/SKILL.md`, "Glossary entry format"; required fields Definition and Type),
applied in-session. `carrier.domain: context-map` was not needed: no relationship changed. Knowledge source
`dca-knowledge` (the profile's `knowledge:`) was not consulted — no statement here decides a pattern question.
`factory:ask` is available in this session but not named in the profile, so it was not used.

## Glossary
| Term | Context | Added or changed | Definition source |
|---|---|---|---|
| ProductSelection | product | added, `src/main/java/dev/domaincentric/sample/ecommerce/product/domain/glossary.md` (section "ProductSelection" under "Value Objects") | plan's glossary proposal, worded as the .NET Product glossary entry (`../dca-ecommerce-sample-dotnet/src/DcaShop.Product/Domain/glossary.md:107-126`); checked against `src/main/java/dev/domaincentric/sample/ecommerce/product/domain/model/ProductSelection.java` (`implements Value`, `MAX_SIZE = 8`, `draw`) and `src/main/java/dev/domaincentric/sample/ecommerce/product/application/getproductselection/GetProductSelectionUseCase.java` (exists, `ls`) |
| Product slider | portal (referenced term, owned by `product`) | added as a row in "Referenced Terms" of `src/main/java/dev/domaincentric/sample/ecommerce/portal/domain/glossary.md` | plan's glossary proposal and the .NET Portal glossary row (`../dca-ecommerce-sample-dotnet/src/DcaShop.Portal/Domain/glossary.md:52`), with the Java composition checked in `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductSliderControllerAdvice.java:11,40` (model attribute `productSlider`) and `src/main/resources/templates/home/index.pug:14-16` (`productSlider.hasCards()`, heading "Discover products") |

## Documents updated
| File | What changed | Verified by |
|---|---|---|
| README.md | package tree of `product/`: `ProductSelection.java` in `domain/model/`, the `getproductselection/` use-case package with its four files, `ProductSliderControllerAdvice.java` and `ProductSliderViewModel.java` in `adapter/incoming/web/` | `ls src/main/java/dev/domaincentric/sample/ecommerce/product/application/getproductselection/` (the four files), `git status --short` (the two web files and `ProductSelection.java` untracked, present) |
| docs/architecture/package-structure.md | the same three additions in the `product/` tree (domain model, use case, web adapter) | same listing as above; `AGENTS.md` "Update documentation" names this file for package-structure changes |
| src/main/java/dev/domaincentric/sample/ecommerce/product/domain/glossary.md | entry `ProductSelection` | see Glossary |
| src/main/java/dev/domaincentric/sample/ecommerce/portal/domain/glossary.md | referenced-term row "Product slider" | see Glossary |


Checked and left unchanged:
- `docs/architecture/context-map.md` (generated): no relationship changed — `grep -c package-info tasks/product-slider/.verify/changed.txt` → 0; the use case uses the existing ports `PricingDataPort` / `ProductStockDataPort`; map lines 51-54 already show `product` → pricing / inventory as ACL.
- `project/domain.md` (designed map): `portal` stays Separate Ways (`project/domain.md:70`); the advice lives in `product`'s web adapter, the portal only has `src/main/resources/templates/home/index.pug` read a model attribute.
- `project/product.md`: lists the home page as a surface and describes no homepage sections (`grep -n -i "homepage\|home page\|Discover\|hero" project/product.md` → line 16 only).

README.md does not state that the slider exists only in the other shop (`grep -rn -i slider README.md docs/` found nothing before this stage), so there was no such statement to correct.

## Not documented
- `dca-sample-specification/exceptions.md:7` (Java shop recorded without a slider): another repository; handled by the orchestrator.
- `docs/architecture/package-structure.md` lists `product/application/updateproductprice/`, which does not exist (`ls src/main/java/dev/domaincentric/sample/ecommerce/product/application/` → createproduct, getallproducts, getproductbyid, getproductselection, shared). Drift that predates this story; not corrected here because this stage touches only what the story made wrong — a TODO candidate.
- `SharedScenariosTest` escape-reading finding and the harness finding on where a selection rule over a read model belongs (plan "Open assumptions"): tooling / catalog work, not reader documentation.
- The judge's minor finding (price text decided in a third place, `ProductSliderViewModel.java:52-54`): a code concern, not documentation.
