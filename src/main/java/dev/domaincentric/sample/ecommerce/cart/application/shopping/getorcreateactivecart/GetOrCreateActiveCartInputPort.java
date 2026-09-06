package dev.domaincentric.sample.ecommerce.cart.application.getorcreateactivecart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input Port for getting or creating an active cart for a customer.
 *
 * <p>This port defines the contract for the "Get or Create Active Cart" use case, which retrieves
 * an existing active cart for a customer or creates a new one if none exists.
 */
public interface GetOrCreateActiveCartInputPort
    extends UseCase<GetOrCreateActiveCartCommand, GetOrCreateActiveCartResult> {}
