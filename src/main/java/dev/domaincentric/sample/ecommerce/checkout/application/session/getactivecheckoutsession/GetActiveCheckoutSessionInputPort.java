package dev.domaincentric.sample.ecommerce.checkout.application.getactivecheckoutsession;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input Port for getting an active checkout session for a customer.
 *
 * <p>This port defines the contract for the "Get Active Checkout Session" use case, which retrieves
 * the active checkout session for a customer based on their identity.
 */
public interface GetActiveCheckoutSessionInputPort
    extends UseCase<GetActiveCheckoutSessionQuery, GetActiveCheckoutSessionResult> {}
