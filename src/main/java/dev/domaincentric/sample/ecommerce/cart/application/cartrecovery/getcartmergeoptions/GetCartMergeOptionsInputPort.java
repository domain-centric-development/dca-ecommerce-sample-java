package dev.domaincentric.sample.ecommerce.cart.application.getcartmergeoptions;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input Port for checking if cart merge options should be presented.
 *
 * <p>This port defines the contract for the "Get Cart Merge Options" use case, which determines
 * whether the user needs to choose how to handle conflicting carts when logging in.
 *
 * <p><b>Hexagonal Architecture:</b> This is a primary/driving port that adapters (e.g., web
 * controllers) use to invoke the use case.
 */
public interface GetCartMergeOptionsInputPort
    extends UseCase<GetCartMergeOptionsQuery, GetCartMergeOptionsResult> {}
