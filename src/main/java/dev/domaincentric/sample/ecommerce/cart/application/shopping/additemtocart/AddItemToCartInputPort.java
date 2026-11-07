package dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for adding items to a shopping cart.
 *
 * <p>This port defines the contract for adding products to carts in the Cart bounded context.
 * Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see
 *     dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartUseCase
 */
public interface AddItemToCartInputPort extends UseCase<AddItemToCartCommand, AddItemToCartResult> {

  /**
   * Adds an item to a shopping cart.
   *
   * @param command the command containing cart ID, product ID, and quantity
   * @return {@link AddItemToCartResult.Added} with the updated cart, or the refusal: {@link
   *     AddItemToCartResult.ArticleNotAvailable}, {@link AddItemToCartResult.CartNotFound}, {@link
   *     AddItemToCartResult.InsufficientArticleStock} or {@link
   *     AddItemToCartResult.CartNotModifiable}
   */
  @Override
  AddItemToCartResult execute(AddItemToCartCommand command);
}
