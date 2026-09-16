/**
 * Product Catalog Bounded Context.
 *
 * <p>Responsible for product management, catalog browsing, and inventory tracking.
 */
@NullMarked
@BoundedContext(
    name = "Product Catalog",
    description = "Product management, catalog browsing, and inventory tracking")
@Upstream(
    context = "pricing",
    translation = Upstream.Translation.ANTI_CORRUPTION_LAYER,
    via = Upstream.Consumes.API,
    rationale = "Prices are translated into the catalog's own product presentation data")
@Upstream(
    context = "inventory",
    translation = Upstream.Translation.ANTI_CORRUPTION_LAYER,
    via = Upstream.Consumes.API,
    rationale = "Stock levels are translated into the catalog's own availability data")
@Upstream(
    context = "pricing",
    translation = Upstream.Translation.CONFORMIST,
    via = Upstream.Consumes.EVENTS,
    rationale =
        "ProductCreatedEvent implements pricing's consumer-defined PriceInitializationTrigger"
            + " contract as-is")
@Upstream(
    context = "inventory",
    translation = Upstream.Translation.CONFORMIST,
    via = Upstream.Consumes.EVENTS,
    rationale =
        "ProductCreatedEvent implements inventory's consumer-defined StockInitializationTrigger"
            + " contract as-is")
@Partnership(
    context = "pricing",
    rationale =
        "The catalog implements pricing's consumer-defined PriceInitializationTrigger contract;"
            + " both contexts evolve it together")
@Partnership(
    context = "inventory",
    rationale =
        "The catalog implements inventory's consumer-defined StockInitializationTrigger contract;"
            + " both contexts evolve it together")
@ApplicationModule(
    allowedDependencies = {
      "sharedkernel",
      "infrastructure",
      "pricing :: api",
      "pricing :: events",
      "inventory :: api",
      "inventory :: events"
    })
package dev.domaincentric.sample.ecommerce.product;

import dev.domaincentric.dca.buildingblocks.ddd.strategic.BoundedContext;
import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.Partnership;
import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.Upstream;
import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
