/**
 * Backoffice Bounded Context.
 *
 * <p>Responsible for operating this application: the event publication log and, as it grows,
 * dashboards and operator navigation. A generic subdomain — one would buy this capability rather
 * than build it — and it is a bounded context because it has a language of its own: an <em>event
 * publication</em> is a dispatched domain event with a completion status, a concept no business
 * context uses or needs to know about.
 *
 * <p>It reads what other contexts have already published and negotiates no contract with any of
 * them, so it declares no upstream relationships: Separate Ways on the context map.
 *
 * <p>Context-specific admin pages (product editing, pricing, inventory) belong to their own bounded
 * contexts under {@code /backoffice/{context}/}, not here. Only what belongs to no business context
 * lives in this one.
 */
@NullMarked
@BoundedContext(
    name = "Backoffice",
    description = "Operating this application: event publication log, dashboards, operator views")
@ApplicationModule(allowedDependencies = {"sharedkernel", "infrastructure"})
package dev.domaincentric.sample.ecommerce.backoffice;

import dev.domaincentric.dca.buildingblocks.ddd.strategic.BoundedContext;
import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
