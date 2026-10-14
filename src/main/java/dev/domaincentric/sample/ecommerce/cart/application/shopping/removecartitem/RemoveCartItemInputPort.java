package dev.domaincentric.sample.ecommerce.cart.application.shopping.removecartitem;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input Port for taking one line out of a shopping cart, whatever its quantity.
 *
 * <p>The line is named by its own id; removing by product is {@code RemoveItemFromCartInputPort}.
 */
public interface RemoveCartItemInputPort
    extends UseCase<RemoveCartItemCommand, RemoveCartItemResult> {}
