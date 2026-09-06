package dev.domaincentric.sample.ecommerce.cart.application.getallcarts;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input Port for retrieving all shopping carts.
 *
 * <p>This port defines the contract for the "Get All Carts" use case.
 */
public interface GetAllCartsInputPort extends UseCase<GetAllCartsQuery, GetAllCartsResult> {}
