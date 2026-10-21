package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.web.shopping;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.cart.adapter.incoming.web.CartPageNotice;
import dev.domaincentric.sample.ecommerce.cart.application.shared.CartNotFoundException;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.ArticleNotAvailableException;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.InsufficientArticleStockException;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getactivecart.GetActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getactivecart.GetActiveCartQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removecartitem.RemoveCartItemCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removecartitem.RemoveCartItemInputPort;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartNotModifiableException;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for rendering shopping cart pages using Pug templates.
 *
 * <p>This controller handles traditional server-side rendered pages using Pug4j templates. Unlike
 * REST API controllers which return JSON, this controller returns HTML views.
 *
 * <p><b>Clean Architecture:</b> This controller depends on use case interfaces (input ports)
 * instead of application services, following the Dependency Inversion Principle.
 *
 * <p><b>Naming Convention:</b> MVC controllers use {@code @Controller} annotation and end with
 * "Controller" suffix. REST controllers use {@code @RestController} and end with "Resource" suffix.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/cart/}
 *
 * <p><b>Template Engine:</b> Pug4j (Java implementation of Pug/Jade)
 */
@Controller
@RequestMapping("/cart")
public class CartPageController {

  private static final String CART_PATH = "/cart";

  private final GetCartByIdInputPort getCartByIdUseCase;
  private final GetActiveCartInputPort getActiveCartUseCase;
  private final GetOrCreateActiveCartInputPort getOrCreateActiveCartUseCase;
  private final AddItemToCartInputPort addItemToCartUseCase;
  private final RemoveCartItemInputPort removeCartItemUseCase;
  private final IdentityProvider identityProvider;

  public CartPageController(
      final GetCartByIdInputPort getCartByIdUseCase,
      final GetActiveCartInputPort getActiveCartUseCase,
      final GetOrCreateActiveCartInputPort getOrCreateActiveCartUseCase,
      final AddItemToCartInputPort addItemToCartUseCase,
      final RemoveCartItemInputPort removeCartItemUseCase,
      final IdentityProvider identityProvider) {
    this.getCartByIdUseCase = getCartByIdUseCase;
    this.getActiveCartUseCase = getActiveCartUseCase;
    this.getOrCreateActiveCartUseCase = getOrCreateActiveCartUseCase;
    this.addItemToCartUseCase = addItemToCartUseCase;
    this.removeCartItemUseCase = removeCartItemUseCase;
    this.identityProvider = identityProvider;
  }

  /**
   * Displays the shopping cart page for the current user.
   *
   * <p>Returns the cart details rendered using the Pug template. The user's identity is obtained
   * from the JWT token via IdentityProvider.
   *
   * @param model Spring MVC model to pass data to the view, holding the notice a redirect handed
   *     the page, if any
   * @return view name "cart/view" which resolves to templates/cart/view.pug
   */
  @GetMapping
  public String showCart(final Model model) {
    CartPageNotice.show(model);

    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final String customerId = identity.userId().value();

    // A visitor without an active cart sees an empty one; showing the page opens no cart.
    final Optional<String> activeCartId =
        getActiveCartUseCase.execute(new GetActiveCartQuery(customerId)).cartId();
    if (activeCartId.isEmpty()) {
      model.addAttribute("shoppingCart", CartPageViewModel.empty());
      model.addAttribute("title", "Shopping Cart");
      return "cart/view";
    }

    final GetCartByIdResult result =
        getCartByIdUseCase.execute(new GetCartByIdQuery(activeCartId.get(), customerId));

    if (!result.found()) {
      return "error/404";
    }

    // Convert to page-specific ViewModel
    final CartPageViewModel viewModel =
        CartPageViewModel.fromEnrichedCart(result.cart().orElseThrow(), result.totals());

    model.addAttribute("shoppingCart", viewModel);
    model.addAttribute("title", "Shopping Cart");

    return "cart/view";
  }

  /**
   * Handles adding a product to the cart.
   *
   * <p>This endpoint:
   *
   * <ul>
   *   <li>Gets or creates an active cart for the current user (via JWT identity)
   *   <li>Adds the specified product to the cart
   *   <li>Redirects to the cart view page
   * </ul>
   *
   * @param productId the product ID to add
   * @param quantity the quantity to add (default: 1)
   * @param attributes the redirect's attributes, which take the cart page's notice
   * @return redirect to cart view
   */
  @PostMapping("/add-product")
  public String addProductToCart(
      @RequestParam final String productId,
      @RequestParam(defaultValue = "1") final int quantity,
      final RedirectAttributes attributes) {

    // Get customer ID from JWT identity
    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final String customerId = identity.userId().value();

    // Get or create active cart
    final GetOrCreateActiveCartResult cartResponse =
        getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));

    // Add product to cart
    final AddItemToCartCommand command =
        new AddItemToCartCommand(cartResponse.cartId(), customerId, productId, quantity);
    try {
      addItemToCartUseCase.execute(command);
      return CartPageNotice.PRODUCT_ADDED.redirectTo(CART_PATH, attributes);
    } catch (final UseCaseException | DomainException | IllegalArgumentException e) {
      // The context's own refusals, plus the argument exception a form field still reaches a value
      // object with.
      return redirectFor(e, attributes);
    }
  }

  /**
   * Handles "Remove" on a line of the cart page: takes the line out of the current user's active
   * cart, whatever its quantity, and leads back to the cart page.
   *
   * <p>The cart is the caller's own, never one the request names. A line that cart does not hold —
   * already removed, malformed, another visitor's — changes nothing: the page shows the cart as it
   * is, without a notice.
   *
   * @param cartItemId the ID of the line to remove
   * @return redirect to cart view
   */
  @PostMapping("/remove-item")
  public String removeCartItem(@RequestParam final String cartItemId) {
    final String customerId = identityProvider.getCurrentIdentity().userId().value();

    final Optional<String> activeCartId =
        getActiveCartUseCase.execute(new GetActiveCartQuery(customerId)).cartId();
    if (activeCartId.isPresent()) {
      try {
        removeCartItemUseCase.execute(
            new RemoveCartItemCommand(activeCartId.get(), customerId, cartItemId));
      } catch (final UseCaseException | DomainException | IllegalArgumentException e) {
        // The cart stays as it is; the page shows it.
      }
    }
    return "redirect:" + CART_PATH;
  }

  /**
   * The cart page with the notice for a refusal: chosen by the failure's type, the number it names
   * taken from its values. The failure's message is for the log, never for the customer.
   */
  private static String redirectFor(
      final RuntimeException failure, final RedirectAttributes attributes) {
    return switch (failure) {
      case InsufficientArticleStockException e ->
          CartPageNotice.NOT_ENOUGH_STOCK.redirectTo(
              CART_PATH, attributes, e.requested(), e.held(), e.inStock());
      case ArticleNotAvailableException e ->
          CartPageNotice.PRODUCT_UNAVAILABLE.redirectTo(CART_PATH, attributes);
      case CartNotFoundException e ->
          CartPageNotice.CART_NOT_FOUND.redirectTo(CART_PATH, attributes);
      case CartNotModifiableException e ->
          CartPageNotice.CART_NOT_MODIFIABLE.redirectTo(CART_PATH, attributes);
      case IllegalArgumentException e ->
          CartPageNotice.INVALID_QUANTITY.redirectTo(CART_PATH, attributes);
      default -> CartPageNotice.PRODUCT_NOT_ADDED.redirectTo(CART_PATH, attributes);
    };
  }
}
