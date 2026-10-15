package dev.domaincentric.sample.ecommerce;

import dev.domaincentric.dca.archunit.DcaLayout;
import dev.domaincentric.dca.archunit.DcaRuleSelection;
import dev.domaincentric.dca.archunit.junit.DcaArchitectureTest;

/**
 * The complete Domain-Centric Architecture rule catalog from {@code
 * dev.domaincentric:dca-archunit}, one dynamic test per rule, grouped by rule set. The rules pin to
 * the {@code dca-building-blocks} markers the domain code implements.
 *
 * <p>The reference implementation runs the catalog unabridged — every rule at {@code ERROR}, the
 * experimental rules included ({@code dca.rules.experimental = true} in {@code
 * dca-archunit.properties}); the file turns off only the rule on state kept in one instance, which
 * the libraries retire. A project adopting DCA on an existing code base rarely can, and does not
 * have to: {@link DcaRuleSelection} narrows the run, lowers a rule to a warning, tolerates a
 * documented exception, or accepts today's violations as a baseline. Configure it in {@code
 * dca-archunit.properties} on the test class path, in {@link #additionalSelection()}, or both — the
 * file is the base, the method is applied on top.
 */
class ArchitectureRulesTest extends DcaArchitectureTest {

  @Override
  protected DcaLayout layout() {
    return EcommerceLayout.layout();
  }

  /**
   * Applied on top of {@code dca-archunit.properties} — the reference implementation adds nothing.
   * Everything a consuming project may need:
   *
   * <pre>{@code
   * return DcaRuleSelection.all()
   *     .onlySets("cycles", "layered", "hexagonal")     // adopt the catalog in stages
   *     .excluding("DCA-NAM-002", "no DI framework")    // reported as skipped, with the reason
   *     .warning("DCA-TAC-009", "being made final")     // reported, but does not fail the build
   *     .ignoringViolationsMatching("DCA-STR-003", ".*legacy.*")   // documented exception
   *     .frozen("DCA-ONI-002");                         // baseline: only new violations fail
   * }</pre>
   */
  @Override
  protected DcaRuleSelection additionalSelection() {
    return DcaRuleSelection.all();
  }
}
