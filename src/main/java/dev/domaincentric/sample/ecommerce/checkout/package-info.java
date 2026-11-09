/**
 * Checkout Bounded Context.
 *
 * <p>Responsible for checkout process, order placement, and payment orchestration.
 */
@NullMarked
@BoundedContext(
    name = "Checkout",
    description = "Checkout process, order placement, and payment orchestration")
@Upstream(
    context = "product",
    translation = Upstream.Translation.ANTI_CORRUPTION_LAYER,
    via = Upstream.Consumes.API,
    rationale = "Product data is translated into checkout's own article types")
@Upstream(
    context = "pricing",
    translation = Upstream.Translation.ANTI_CORRUPTION_LAYER,
    via = Upstream.Consumes.API,
    rationale = "Prices are translated into checkout's own line item amounts")
@Upstream(
    context = "inventory",
    translation = Upstream.Translation.ANTI_CORRUPTION_LAYER,
    via = Upstream.Consumes.API,
    rationale = "Stock availability is translated into checkout's own article data")
@Upstream(
    context = "inventory",
    translation = Upstream.Translation.CONFORMIST,
    via = Upstream.Consumes.EVENTS,
    rationale =
        "CheckoutConfirmedEvent implements inventory's consumer-defined StockReductionTrigger"
            + " contract as-is")
@Upstream(
    context = "cart",
    translation = Upstream.Translation.ANTI_CORRUPTION_LAYER,
    via = Upstream.Consumes.API,
    rationale = "Cart snapshots are translated into checkout's own CartData")
@Upstream(
    context = "cart",
    translation = Upstream.Translation.CONFORMIST,
    via = Upstream.Consumes.EVENTS,
    rationale =
        "CheckoutConfirmedEvent implements cart's consumer-defined CartCompletionTrigger contract"
            + " as-is; cart's CartContentsChangedEvent is consumed as published")
@ExternalUpstream(
    name = "Payment Service Provider",
    translation = Upstream.Translation.ANTI_CORRUPTION_LAYER,
    interaction = ExternalUpstream.Interaction.OUTBOUND,
    protocol = "REST",
    exchanges = "payment operations (initiate, confirm, cancel)",
    rationale =
        "Behind a caller-owned port per provider and a registry that chooses one; a REST adapter"
            + " translates the provider's answers where its address is configured, a stand-in takes"
            + " payments otherwise")
@ExternalUpstream(
    name = "Mail Provider",
    translation = Upstream.Translation.ANTI_CORRUPTION_LAYER,
    interaction = ExternalUpstream.Interaction.OUTBOUND,
    protocol = "REST",
    exchanges =
        "order confirmation mail (recipient, template, idempotency key, total) and its answer"
            + " (accepted, unavailable)",
    rationale =
        "Behind the caller-owned OrderConfirmationMailer port; a REST adapter translates the"
            + " provider's answers where its address is configured, a stand-in accepts every mail"
            + " and sends nothing otherwise")
@Partnership(
    context = "cart",
    rationale =
        "Checkout implements cart's consumer-defined CartCompletionTrigger contract; both contexts"
            + " evolve it together")
@Partnership(
    context = "inventory",
    rationale =
        "Checkout implements inventory's consumer-defined StockReductionTrigger contract; both"
            + " contexts evolve it together")
@ApplicationModule(
    allowedDependencies = {
      "sharedkernel",
      "infrastructure",
      "product :: api",
      "pricing :: api",
      "inventory :: api",
      "inventory :: events",
      "cart :: api",
      "cart :: events"
    })
package dev.domaincentric.sample.ecommerce.checkout;

import dev.domaincentric.dca.buildingblocks.ddd.strategic.BoundedContext;
import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.ExternalUpstream;
import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.Partnership;
import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.Upstream;
import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
