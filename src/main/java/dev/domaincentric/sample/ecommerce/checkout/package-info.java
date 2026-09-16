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
    rationale = "Product data is translated into checkout's own article and product info types")
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
            + " as-is; cart change events are consumed directly")
@ExternalUpstream(
    name = "Payment Service Provider",
    translation = Upstream.Translation.ANTI_CORRUPTION_LAYER,
    interaction = ExternalUpstream.Interaction.OUTBOUND,
    protocol = "REST",
    exchanges = "payment operations (initiate, confirm, refund)",
    rationale =
        "Behind the caller-owned PaymentProvider port; the sample ships a mock adapter in place"
            + " of a real gateway")
@ExternalUpstream(
    name = "Payment Service Provider",
    translation = Upstream.Translation.ANTI_CORRUPTION_LAYER,
    interaction = ExternalUpstream.Interaction.INBOUND,
    status = Upstream.Status.PLANNED,
    protocol = "webhook",
    exchanges = "payment confirmation (payment id, status)",
    rationale =
        "Will trigger order fulfillment; the payload is the provider's contract, to be translated"
            + " into a local command at the incoming adapter — no webhook adapter exists yet")
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
