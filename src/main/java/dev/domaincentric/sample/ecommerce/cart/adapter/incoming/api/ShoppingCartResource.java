package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.createcart.CreateCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.createcart.CreateCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.createcart.CreateCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getactivecart.GetActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getactivecart.GetActiveCartQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartResult;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API Resource for Shopping Cart operations.
 *
 * <p>This is a primary adapter (incoming) in Hexagonal Architecture that exposes shopping cart
 * functionality via REST API using Clean Architecture use cases. It depends on use case interfaces
 * (input ports) rather than the use case classes.
 *
 * <p><b>Authorization:</b> every route acts on the cart of the caller's own identity — the cart id
 * in the path selects <i>which</i> of the caller's carts is meant, never <i>whose</i> cart is
 * meant. A cart belonging to somebody else answers {@code 404}, not {@code 403}: a {@code 403}
 * would confirm that the id exists, which is exactly the fact a stranger must not learn. Listing
 * every cart in the shop crosses that boundary by design; it is the cart oversight's, {@link
 * CartOversightResource}.
 *
 * <p><b>Refusals:</b> each route switches over its use case's result — no {@code default}, so the
 * compiler holds that every refusal has an answer — and takes the answer from {@link CartProblems}.
 * What is still thrown is {@link CartApiExceptionHandler}'s.
 *
 * <p><b>Bearer only:</b> {@code /api/**} is authenticated by an {@code Authorization: Bearer}
 * header and never by a browser cookie, which is what makes its CSRF exemption sound (ADR-035).
 */
@RestController
@RequestMapping("/api/carts")
public class ShoppingCartResource {

  private final CreateCartInputPort createCart;
  private final GetCartByIdInputPort getCartById;
  private final GetActiveCartInputPort getActiveCart;
  private final AddItemToCartInputPort addItemToCart;
  private final RemoveItemFromCartInputPort removeItemFromCart;
  private final ShoppingCartDtoConverter converter;
  private final IdentityProvider identityProvider;

  public ShoppingCartResource(
      final CreateCartInputPort createCart,
      final GetCartByIdInputPort getCartById,
      final GetActiveCartInputPort getActiveCart,
      final AddItemToCartInputPort addItemToCart,
      final RemoveItemFromCartInputPort removeItemFromCart,
      final ShoppingCartDtoConverter converter,
      final IdentityProvider identityProvider) {
    this.createCart = createCart;
    this.getCartById = getCartById;
    this.getActiveCart = getActiveCart;
    this.addItemToCart = addItemToCart;
    this.removeItemFromCart = removeItemFromCart;
    this.converter = converter;
    this.identityProvider = identityProvider;
  }

  /** Creates a cart for the caller. The customer is the caller's identity, never a parameter. */
  @PostMapping
  public ResponseEntity<?> createCart() {
    return switch (createCart.execute(new CreateCartCommand(currentCustomerId()))) {
      case CreateCartResult.Created created ->
          ResponseEntity.status(HttpStatus.CREATED).body(converter.toDto(created));
      case CreateCartResult.ActiveCartAlreadyExists refusal ->
          CartProblems.activeCartAlreadyExists(refusal.customerId());
    };
  }

  @GetMapping("/{cartId}")
  public ResponseEntity<ShoppingCartDto> getCart(@PathVariable final String cartId) {
    return ownCart(cartId)
        .map(cart -> ResponseEntity.ok(converter.toDto(cart)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * The caller's active cart. The customer id in the path must name the caller; any other value is
   * treated as a cart that does not exist for them. A read never opens a cart: a caller without an
   * active cart gets {@code 404} and opens one with {@code POST /api/carts}.
   */
  @GetMapping("/customer/{customerId}/active")
  public ResponseEntity<ShoppingCartDto> getActiveCart(@PathVariable final String customerId) {
    if (!currentCustomerId().equals(customerId)) {
      return ResponseEntity.notFound().build();
    }

    return getActiveCart
        .execute(new GetActiveCartQuery(customerId))
        .cartId()
        .flatMap(this::ownCart)
        .map(cart -> ResponseEntity.ok(converter.toDto(cart)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/{cartId}/items")
  public ResponseEntity<?> addItemToCart(
      @PathVariable final String cartId, @Valid @RequestBody final AddToCartRequest request) {

    if (ownCart(cartId).isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    final AddItemToCartCommand input =
        new AddItemToCartCommand(
            cartId, currentCustomerId(), parsedProductId(request.productId()), request.quantity());

    return switch (addItemToCart.execute(input)) {
      case AddItemToCartResult.Added added -> ResponseEntity.ok(converter.toDto(added));
      case AddItemToCartResult.ArticleNotAvailable refusal ->
          CartProblems.articleNotAvailable(refusal.productId());
      case AddItemToCartResult.CartNotFound refusal -> CartProblems.cartNotFound(refusal.cartId());
      case AddItemToCartResult.InsufficientArticleStock refusal ->
          CartProblems.notEnoughStock(refusal.productId(), refusal.requested());
      case AddItemToCartResult.CartNotModifiable refusal -> CartProblems.cartRefusesTheChange();
    };
  }

  @DeleteMapping("/{cartId}/items/{productId}")
  public ResponseEntity<?> removeItemFromCart(
      @PathVariable final String cartId, @PathVariable final String productId) {

    if (ownCart(cartId).isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    final RemoveItemFromCartCommand input =
        new RemoveItemFromCartCommand(cartId, currentCustomerId(), parsedProductId(productId));
    return switch (removeItemFromCart.execute(input)) {
      case RemoveItemFromCartResult.Removed removed -> ResponseEntity.ok(converter.toDto(removed));
      case RemoveItemFromCartResult.CartNotFound refusal ->
          CartProblems.cartNotFound(refusal.cartId());
      case RemoveItemFromCartResult.CartItemNotFound refusal ->
          CartProblems.positionNotInCart(refusal.productId().value());
      case RemoveItemFromCartResult.CartNotModifiable refusal ->
          CartProblems.cartRefusesTheChange();
    };
  }

  /**
   * The product id the caller sent, parsed here: text that is no UUID is a malformed call, answered
   * {@code 400} by {@link CartApiExceptionHandler} without reaching the use case — never a product
   * that does not exist.
   *
   * @throws IllegalArgumentException when the text does not parse
   */
  private static String parsedProductId(final String productId) {
    return UUID.fromString(productId).toString();
  }

  private String currentCustomerId() {
    return identityProvider.getCurrentIdentity().userId().value();
  }

  /**
   * The cart, but only if it is the caller's — otherwise empty, and the caller is told it is not
   * there.
   */
  private Optional<EnrichedCart> ownCart(final String cartId) {
    final Optional<EnrichedCart> found =
        switch (getCartById.execute(new GetCartByIdQuery(cartId, currentCustomerId()))) {
          case GetCartByIdResult.Found result -> Optional.of(result.cart());
          case GetCartByIdResult.CartNotFound refusal -> Optional.empty();
        };
    return found.filter(cart -> cart.customerId().value().equals(currentCustomerId()));
  }
}
