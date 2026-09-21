package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.web.shopping;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartResult;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCartItem;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Provides mini basket data and identity information to all Pug templates.
 *
 * <p>This {@code @ControllerAdvice} adds the following model attributes to every web request,
 * making them available in the layout template:
 *
 * <ul>
 *   <li>{@code miniBasketItemCount} - number of distinct items in the cart
 *   <li>{@code miniBasketTotal} - formatted cart total (e.g., "49.97 EUR")
 *   <li>{@code miniBasketItems} - list of {@link MiniBasketItemViewModel} for the dropdown
 *   <li>{@code identity} - the current user's {@link IdentityProvider.Identity}
 * </ul>
 *
 * <p>Errors are handled gracefully: if the cart cannot be loaded, the mini basket shows zero items
 * and an empty total.
 *
 * <p><b>Pages only.</b> A {@code @ControllerAdvice} without a selector reaches every controller of
 * the application, the REST resources and the tool provider included — and this one does not merely
 * read: {@code GetOrCreateActiveCart} opens a cart for the visitor. An API caller would get a cart
 * they never asked for on every request, and {@code POST /api/carts} would then refuse its own work
 * because the caller already has an open one. The model attributes only mean something where a view
 * renders them, so the advice returns early for everything that is not a page.
 */
@ControllerAdvice
public class MiniBasketControllerAdvice {

  private static final Logger LOG = LoggerFactory.getLogger(MiniBasketControllerAdvice.class);

  private final GetOrCreateActiveCartInputPort getOrCreateActiveCartUseCase;
  private final GetCartByIdInputPort getCartByIdUseCase;
  private final IdentityProvider identityProvider;

  public MiniBasketControllerAdvice(
      final GetOrCreateActiveCartInputPort getOrCreateActiveCartUseCase,
      final GetCartByIdInputPort getCartByIdUseCase,
      final IdentityProvider identityProvider) {
    this.getOrCreateActiveCartUseCase = getOrCreateActiveCartUseCase;
    this.getCartByIdUseCase = getCartByIdUseCase;
    this.identityProvider = identityProvider;
  }

  /** Prefixes that answer with data rather than with a rendered page. */
  private static final List<String> NON_PAGE_PREFIXES = List.of("/api", "/mcp", "/actuator");

  /** Adds mini basket data and identity to the model of every page request. */
  @ModelAttribute
  public void addMiniBasketAndIdentity(final Model model, final HttpServletRequest request) {
    if (isNotAPage(request)) {
      return;
    }

    final IdentityProvider.Identity identity;
    try {
      identity = identityProvider.getCurrentIdentity();
    } catch (final IllegalStateException ex) {
      // Non-JWT security context (e.g., backoffice form login) — skip mini basket
      model.addAttribute("miniBasketItemCount", 0);
      model.addAttribute("miniBasketTotal", "");
      model.addAttribute("miniBasketItems", List.of());
      return;
    }
    model.addAttribute("identity", identity);

    try {
      final GetOrCreateActiveCartResult cartRef =
          getOrCreateActiveCartUseCase.execute(
              new GetOrCreateActiveCartCommand(identity.userId().value()));

      final GetCartByIdResult result =
          getCartByIdUseCase.execute(new GetCartByIdQuery(cartRef.cartId(), cartRef.customerId()));

      if (result.found()) {
        final EnrichedCart cart = result.cart().orElseThrow();
        populateMiniBasket(model, cart);
        return;
      }
    } catch (final UseCaseException | DomainException ex) {
      // The basket is decoration on every page: a cart the use case refuses to hand out leaves it
      // empty rather than breaking the page. Anything else is a defect and keeps travelling.
      LOG.debug("Could not load mini basket data: {}", ex.getMessage());
    }

    // Fallback: empty basket
    model.addAttribute("miniBasketItemCount", 0);
    model.addAttribute("miniBasketTotal", "");
    model.addAttribute("miniBasketItems", List.of());
  }

  private void populateMiniBasket(final Model model, final EnrichedCart cart) {
    final int itemCount = cart.items().stream().mapToInt(item -> item.quantity().value()).sum();

    final var subtotal = cart.calculateCurrentSubtotal();
    final String formattedTotal =
        subtotal.amount().toPlainString() + " " + subtotal.currency().getCurrencyCode();

    final List<MiniBasketItemViewModel> items =
        cart.items().stream().map(this::toMiniBasketItem).toList();

    model.addAttribute("miniBasketItemCount", itemCount);
    model.addAttribute("miniBasketTotal", formattedTotal);
    model.addAttribute("miniBasketItems", items);
  }

  private MiniBasketItemViewModel toMiniBasketItem(final EnrichedCartItem item) {
    final var lineTotal = item.currentLineTotal();
    final String formattedPrice =
        lineTotal.amount().toPlainString() + " " + lineTotal.currency().getCurrencyCode();

    return new MiniBasketItemViewModel(
        item.currentArticle().name(), item.quantity().value(), formattedPrice);
  }

  /** Whether this request is answered with data rather than with a page. */
  private static boolean isNotAPage(final HttpServletRequest request) {
    final String path = request.getRequestURI();
    return NON_PAGE_PREFIXES.stream().anyMatch(path::startsWith);
  }
}
