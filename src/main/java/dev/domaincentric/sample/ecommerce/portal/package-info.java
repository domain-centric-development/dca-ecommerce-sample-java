/**
 * Portal Bounded Context.
 *
 * <p>Responsible for the web portal: the landing page, navigation and cross-context views. A
 * generic subdomain — UI composition — and a bounded context because its terms are its own; it
 * displays the concepts of other contexts by link, never by call, and has no rich domain model yet.
 * The pattern-selection decision (ADR-025) allows that thin shape for a generic subdomain.
 */
@NullMarked
@BoundedContext(
    name = "Portal",
    description = "Web portal, user interface composition, and cross-context views")
@ApplicationModule(allowedDependencies = {"sharedkernel", "infrastructure"})
package dev.domaincentric.sample.ecommerce.portal;

import dev.domaincentric.dca.buildingblocks.ddd.strategic.BoundedContext;
import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
