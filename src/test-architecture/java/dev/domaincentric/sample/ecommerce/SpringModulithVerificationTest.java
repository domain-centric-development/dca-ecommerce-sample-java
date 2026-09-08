package dev.domaincentric.sample.ecommerce;

import dev.domaincentric.dca.archunit.DcaLayout;
import dev.domaincentric.dca.archunit.springmodulith.DcaSpringModulithTest;

/**
 * Spring Modulith module structure verification, through {@code dca-archunit-spring-modulith}: no
 * undeclared cross-module dependencies, named interfaces ({@code api/}, {@code events/}) properly
 * configured, {@code allowedDependencies} respected. The base class excludes the architecture tests
 * in this package from Modulith's root module and lists the discovered modules as a diagnostic.
 */
class SpringModulithVerificationTest extends DcaSpringModulithTest {

  @Override
  protected DcaLayout layout() {
    return EcommerceLayout.layout();
  }
}
