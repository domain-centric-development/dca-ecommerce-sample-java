/**
 * Inventory Bounded Context.
 *
 * <p>Responsible for managing stock levels and availability tracking.
 */
@NullMarked
@BoundedContext(
    name = "Inventory",
    description = "Stock level management and availability tracking")
@Partnership(
    context = "checkout",
    rationale =
        "Inventory owns the consumer-defined StockReductionTrigger contract that checkout events"
            + " implement; both contexts evolve it together")
@Partnership(
    context = "product",
    rationale =
        "Inventory owns the consumer-defined StockInitializationTrigger contract that catalog"
            + " events implement; both contexts evolve it together")
@ApplicationModule(allowedDependencies = {"sharedkernel", "infrastructure"})
package dev.domaincentric.sample.ecommerce.inventory;

import dev.domaincentric.dca.buildingblocks.ddd.strategic.BoundedContext;
import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.Partnership;
import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
