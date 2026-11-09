/**
 * Product Catalog Bounded Context.
 *
 * <p>Responsible for product management and catalog browsing.
 */
@NullMarked
@BoundedContext(name = "Product Catalog", description = "Product management and catalog browsing")
@Upstream(
    context = "pricing",
    translation = Upstream.Translation.ANTI_CORRUPTION_LAYER,
    via = Upstream.Consumes.API,
    rationale =
        "The catalog shows a price but does not own it; the pricing model is translated into the"
            + " catalog's own article view")
@Upstream(
    context = "inventory",
    translation = Upstream.Translation.ANTI_CORRUPTION_LAYER,
    via = Upstream.Consumes.API,
    rationale =
        "Availability is an inventory statement; the catalog translates it into its own article"
            + " view")
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
