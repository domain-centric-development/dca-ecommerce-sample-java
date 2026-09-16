/**
 * Shopping Cart Bounded Context.
 *
 * <p>Responsible for cart management, item additions/removals, and cart lifecycle.
 */
@NullMarked
@BoundedContext(
    name = "Shopping Cart",
    description = "Cart management, item additions/removals, and cart lifecycle")
@Upstream(
    context = "product",
    translation = Upstream.Translation.ANTI_CORRUPTION_LAYER,
    via = Upstream.Consumes.API,
    rationale =
        "Cart works with its own article snapshot; the catalog model must not leak into cart"
            + " invariants")
@Upstream(
    context = "pricing",
    translation = Upstream.Translation.ANTI_CORRUPTION_LAYER,
    via = Upstream.Consumes.API,
    rationale = "Price lookups are translated into the cart's own price representation")
@Upstream(
    context = "inventory",
    translation = Upstream.Translation.ANTI_CORRUPTION_LAYER,
    via = Upstream.Consumes.API,
    rationale = "Stock availability is translated into the cart's own article data")
@Partnership(
    context = "checkout",
    rationale =
        "Cart owns the consumer-defined CartCompletionTrigger contract that checkout events"
            + " implement; both contexts evolve it together")
@Upstream(
    context = "account",
    translation = Upstream.Translation.CONFORMIST,
    via = Upstream.Consumes.API,
    rationale =
        "Incoming adapters read the caller's identity from Account's published IdentityService"
            + " as-is and hand the customer to their use cases as a command or query parameter")
@ApplicationModule(
    allowedDependencies = {
      "sharedkernel",
      "infrastructure",
      "account :: api",
      "product :: api",
      "pricing :: api",
      "inventory :: api"
    })
package dev.domaincentric.sample.ecommerce.cart;

import dev.domaincentric.dca.buildingblocks.ddd.strategic.BoundedContext;
import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.Partnership;
import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.Upstream;
import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
