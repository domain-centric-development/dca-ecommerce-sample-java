package dev.domaincentric.sample.ecommerce.cart.application.shopping.getactivecart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input Port for reading the customer's active cart without creating one: a read never creates. A
 * page that shows the cart asks this; a command that needs a cart to change opens one.
 */
public interface GetActiveCartInputPort extends UseCase<GetActiveCartQuery, GetActiveCartResult> {}
