/**
 * Pricing Bounded Context.
 *
 * <p>Responsible for managing product prices, price changes, and exposing pricing information to
 * other bounded contexts via Open Host Service.
 */
@NullMarked
@BoundedContext(
    name = "Pricing",
    description = "Product pricing management and price change tracking")
@Partnership(
    context = "product",
    rationale =
        "Pricing owns the consumer-defined PriceInitializationTrigger contract that catalog"
            + " events implement; both contexts evolve it together")
@ApplicationModule(allowedDependencies = {"sharedkernel", "infrastructure"})
package dev.domaincentric.sample.ecommerce.pricing;

import dev.domaincentric.dca.buildingblocks.ddd.strategic.BoundedContext;
import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.Partnership;
import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
