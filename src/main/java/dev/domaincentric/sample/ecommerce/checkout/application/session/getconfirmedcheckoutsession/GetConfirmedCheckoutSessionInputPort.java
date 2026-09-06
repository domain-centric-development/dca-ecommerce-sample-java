package dev.domaincentric.sample.ecommerce.checkout.application.getconfirmedcheckoutsession;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input Port for getting a confirmed or completed checkout session for a customer.
 *
 * <p>This port defines the contract for the "Get Confirmed Checkout Session" use case, which
 * retrieves a confirmed or completed checkout session for displaying the confirmation/thank you
 * page.
 */
public interface GetConfirmedCheckoutSessionInputPort
    extends UseCase<GetConfirmedCheckoutSessionQuery, GetConfirmedCheckoutSessionResult> {}
