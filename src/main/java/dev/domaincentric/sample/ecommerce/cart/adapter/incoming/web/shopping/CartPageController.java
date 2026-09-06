package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.web.shopping;

import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartResult;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
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

  private final GetCartByIdInputPort getCartByIdUseCase;
  private final GetOrCreateActiveCartInputPort getOrCreateActiveCartUseCase;
  private final AddItemToCartInputPort addItemToCartUseCase;
  private final IdentityProvider identityProvider;

  public CartPageController(
      final GetCartByIdInputPort getCartByIdUseCase,
      final GetOrCreateActiveCartInputPort getOrCreateActiveCartUseCase,
      final AddItemToCartInputPort addItemToCartUseCase,
      final IdentityProvider identityProvider) {
    this.getCartByIdUseCase = getCartByIdUseCase;
    this.getOrCreateActiveCartUseCase = getOrCreateActiveCartUseCase;
    this.addItemToCartUseCase = addItemToCartUseCase;
    this.identityProvider = identityProvider;
  }

  /**
   * Displays the shopping cart page for the current user.
   *
   * <p>Returns the cart details rendered using the Pug template. The user's identity is obtained
   * from the JWT token via IdentityProvider.
   *
   * @param model Spring MVC model to pass data to the view
   * @return view name "cart/view" which resolves to templates/cart/view.pug
   */
  @GetMapping
  public String showCart(final Model model) {
    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final String customerId = identity.userId().value();

    // Get or create active cart for the current user
    final GetOrCreateActiveCartResult cartResponse =
        getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));

    // Fetch cart details
    final GetCartByIdResult result =
        getCartByIdUseCase.execute(new GetCartByIdQuery(cartResponse.cartId(), customerId));

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
   * @param redirectAttributes for passing flash messages
   * @return redirect to cart view
   */
  @PostMapping("/add-product")
  public String addProductToCart(
      @RequestParam final String productId,
      @RequestParam(defaultValue = "1") final int quantity,
      final RedirectAttributes redirectAttributes) {

    // Get customer ID from JWT identity
    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final String customerId = identity.userId().value();

    // Get or create active cart
    final GetOrCreateActiveCartResult cartResponse =
        getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));

    // Add product to cart
    final AddItemToCartCommand command =
        new AddItemToCartCommand(cartResponse.cartId(), customerId, productId, quantity);
    final AddItemToCartResult addResponse = addItemToCartUseCase.execute(command);

    // Add success message
    redirectAttributes.addFlashAttribute("message", "Product added to cart!");

    // Redirect to cart view
    return "redirect:/cart";
  }
}
