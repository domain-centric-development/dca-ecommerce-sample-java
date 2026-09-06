package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.checkoutcart.CheckoutCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.checkoutcart.CheckoutCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.checkoutcart.CheckoutCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.operations.getallcarts.GetAllCartsInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.operations.getallcarts.GetAllCartsQuery;
import dev.domaincentric.sample.ecommerce.cart.application.operations.getallcarts.GetAllCartsResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.createcart.CreateCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.createcart.CreateCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.createcart.CreateCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartResult;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import jakarta.validation.Valid;
import java.util.Optional;
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
 * every cart in the shop crosses that boundary by design and therefore requires the staff role.
 *
 * <p><b>Bearer only:</b> {@code /api/**} is authenticated by an {@code Authorization: Bearer}
 * header and never by a browser cookie, which is what makes its CSRF exemption sound (ADR-035).
 */
@RestController
@RequestMapping("/api/carts")
public class ShoppingCartResource {

  private final CreateCartInputPort createCart;
  private final GetAllCartsInputPort getAllCarts;
  private final GetCartByIdInputPort getCartById;
  private final GetOrCreateActiveCartInputPort getOrCreateActiveCart;
  private final AddItemToCartInputPort addItemToCart;
  private final RemoveItemFromCartInputPort removeItemFromCart;
  private final CheckoutCartInputPort checkoutCart;
  private final ShoppingCartDtoConverter converter;
  private final IdentityProvider identityProvider;

  public ShoppingCartResource(
      final CreateCartInputPort createCart,
      final GetAllCartsInputPort getAllCarts,
      final GetCartByIdInputPort getCartById,
      final GetOrCreateActiveCartInputPort getOrCreateActiveCart,
      final AddItemToCartInputPort addItemToCart,
      final RemoveItemFromCartInputPort removeItemFromCart,
      final CheckoutCartInputPort checkoutCart,
      final ShoppingCartDtoConverter converter,
      final IdentityProvider identityProvider) {
    this.createCart = createCart;
    this.getAllCarts = getAllCarts;
    this.getCartById = getCartById;
    this.getOrCreateActiveCart = getOrCreateActiveCart;
    this.addItemToCart = addItemToCart;
    this.removeItemFromCart = removeItemFromCart;
    this.checkoutCart = checkoutCart;
    this.converter = converter;
    this.identityProvider = identityProvider;
  }

  /** Creates a cart for the caller. The customer is the caller's identity, never a parameter. */
  @PostMapping
  public ResponseEntity<ShoppingCartDto> createCart() {
    final CreateCartResult output = createCart.execute(new CreateCartCommand(currentCustomerId()));

    return ResponseEntity.status(HttpStatus.CREATED).body(converter.toDto(output));
  }

  /**
   * Every cart in the shop — the operator view, and the only route that leaves the caller's data.
   */
  @GetMapping
  public ResponseEntity<ShoppingCartListDto> getAllCarts() {
    if (!identityProvider.getCurrentIdentity().hasRole(IdentityProvider.Identity.ROLE_STAFF)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    final GetAllCartsResult output = getAllCarts.execute(new GetAllCartsQuery());

    return ResponseEntity.ok(converter.toListDto(output));
  }

  @GetMapping("/{cartId}")
  public ResponseEntity<ShoppingCartDto> getCart(@PathVariable final String cartId) {
    return ownCart(cartId)
        .map(cart -> ResponseEntity.ok(converter.toDto(cart)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * The caller's active cart. The customer id in the path must name the caller; any other value is
   * treated as a cart that does not exist for them.
   */
  @GetMapping("/customer/{customerId}/active")
  public ResponseEntity<ShoppingCartDto> getOrCreateActiveCart(
      @PathVariable final String customerId) {
    if (!currentCustomerId().equals(customerId)) {
      return ResponseEntity.notFound().build();
    }

    final var response =
        getOrCreateActiveCart.execute(new GetOrCreateActiveCartCommand(currentCustomerId()));

    final GetCartByIdResult result =
        getCartById.execute(new GetCartByIdQuery(response.cartId(), currentCustomerId()));
    return ResponseEntity.ok(converter.toDto(result.cart().orElseThrow()));
  }

  @PostMapping("/{cartId}/items")
  public ResponseEntity<ShoppingCartDto> addItemToCart(
      @PathVariable final String cartId, @Valid @RequestBody final AddToCartRequest request) {

    if (ownCart(cartId).isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    final AddItemToCartCommand input =
        new AddItemToCartCommand(
            cartId, currentCustomerId(), request.productId(), request.quantity());

    try {
      final AddItemToCartResult output = addItemToCart.execute(input);
      return ResponseEntity.ok(converter.toDto(output));
    } catch (IllegalArgumentException ex) {
      final String msg = ex.getMessage() != null ? ex.getMessage() : "Invalid request";
      if (msg.startsWith("Product not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{cartId}/items/{productId}")
  public ResponseEntity<ShoppingCartDto> removeItemFromCart(
      @PathVariable final String cartId, @PathVariable final String productId) {

    if (ownCart(cartId).isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    final RemoveItemFromCartCommand input =
        new RemoveItemFromCartCommand(cartId, currentCustomerId(), productId);
    final RemoveItemFromCartResult output = removeItemFromCart.execute(input);

    return ResponseEntity.ok(converter.toDto(output));
  }

  @PostMapping("/{cartId}/checkout")
  public ResponseEntity<ShoppingCartDto> checkout(@PathVariable final String cartId) {
    if (ownCart(cartId).isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    final CheckoutCartResult output =
        checkoutCart.execute(new CheckoutCartCommand(cartId, currentCustomerId()));

    return ResponseEntity.ok(converter.toDto(output));
  }

  private String currentCustomerId() {
    return identityProvider.getCurrentIdentity().userId().value();
  }

  /**
   * The cart, but only if it is the caller's — otherwise empty, and the caller is told it is not
   * there.
   */
  private Optional<EnrichedCart> ownCart(final String cartId) {
    return getCartById
        .execute(new GetCartByIdQuery(cartId, currentCustomerId()))
        .cart()
        .filter(cart -> cart.customerId().value().equals(currentCustomerId()));
  }
}
