package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.web.cartrecovery;

import dev.domaincentric.sample.ecommerce.cart.adapter.incoming.web.CartPageNotice;
import dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.getcartmergeoptions.GetCartMergeOptionsInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.getcartmergeoptions.GetCartMergeOptionsQuery;
import dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.getcartmergeoptions.GetCartMergeOptionsResult;
import dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.mergecarts.CartMergeStrategy;
import dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.mergecarts.MergeCartsCommand;
import dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.mergecarts.MergeCartsInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.mergecarts.MergeCartsResult;
import dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.recovercart.RecoverCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.recovercart.RecoverCartInputPort;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for the page after the login: the cart merge options and the take-over.
 *
 * <p>This controller handles the page after the login: the cart merge decision when both the
 * anonymous cart and the account cart have items, and the take-over when only the anonymous one
 * has. Either moves a cart only on a form the browser sends.
 *
 * <p><b>Stateless Design:</b> This controller uses URL parameters instead of HTTP sessions to pass
 * state between requests. The anonymous user ID and return URL are passed via URL, making it
 * compatible with session-disabled configurations.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/cart/merge-options.pug} and
 * {@code src/main/resources/templates/cart/take-over.pug}
 */
@Controller
@RequestMapping("/cart/merge")
public class CartMergePageController {

  private static final String CART_PATH = "/cart";

  private final GetCartMergeOptionsInputPort getCartMergeOptionsUseCase;
  private final MergeCartsInputPort mergeCartsUseCase;
  private final RecoverCartInputPort recoverCartUseCase;
  private final IdentityProvider identityProvider;

  public CartMergePageController(
      final GetCartMergeOptionsInputPort getCartMergeOptionsUseCase,
      final MergeCartsInputPort mergeCartsUseCase,
      final RecoverCartInputPort recoverCartUseCase,
      final IdentityProvider identityProvider) {
    this.getCartMergeOptionsUseCase = getCartMergeOptionsUseCase;
    this.mergeCartsUseCase = mergeCartsUseCase;
    this.recoverCartUseCase = recoverCartUseCase;
    this.identityProvider = identityProvider;
  }

  /**
   * Displays the page after the login.
   *
   * <p>When both carts hold items, shows the user both and lets them choose how to handle the
   * conflict. When only the anonymous cart holds items, shows the take-over page, whose form moves
   * them into the account — opening a page moves no cart. With nothing to take over, redirects to
   * the return URL or cart page.
   *
   * @param model Spring MVC model, holding the notice a redirect handed the page, if any
   * @param anonymousUserId the anonymous user ID (passed via URL from login)
   * @param returnUrl optional URL to redirect to after merge
   * @param attributes the redirect's attributes, which take the cart page's notice
   * @return view name "cart/merge-options" or "cart/take-over", or a redirect if neither is needed
   */
  @GetMapping
  public String showMergeOptions(
      final Model model,
      @RequestParam final String anonymousUserId,
      @RequestParam(required = false) final String returnUrl,
      final RedirectAttributes attributes) {

    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final String registeredUserId = identity.userId().value();

    // Get merge options
    final GetCartMergeOptionsQuery query =
        new GetCartMergeOptionsQuery(anonymousUserId, registeredUserId);
    final GetCartMergeOptionsResult options = getCartMergeOptionsUseCase.execute(query);

    if (!options.mergeRequired()) {
      if (options.anonymousCart() == null) {
        return redirectAfterLogin(returnUrl, CartPageNotice.WELCOME_BACK, attributes);
      }
      model.addAttribute("anonymousUserId", anonymousUserId);
      model.addAttribute("returnUrl", returnUrl);
      model.addAttribute("title", "Bringing your cart along");
      return "cart/take-over";
    }

    // Convert to page-specific ViewModel
    final CartMergePageViewModel viewModel =
        CartMergePageViewModel.fromResult(options, anonymousUserId, returnUrl);

    CartPageNotice.show(model);
    model.addAttribute("cartMerge", viewModel);
    model.addAttribute("title", "Cart Merge Options");

    return "cart/merge-options";
  }

  /**
   * Takes the anonymous cart into the account's cart: the form of the take-over page, sent by the
   * browser's script or with "Continue".
   *
   * @param anonymousUserId the anonymous user ID (from hidden form field)
   * @param returnUrl optional URL to redirect to after the take-over
   * @param attributes the redirect's attributes, which take the cart page's notice
   * @return redirect to return URL or cart
   */
  @PostMapping("/take-over")
  public String takeOverGuestCart(
      @RequestParam final String anonymousUserId,
      @RequestParam(required = false) final String returnUrl,
      final RedirectAttributes attributes) {

    final String registeredUserId = identityProvider.getCurrentIdentity().userId().value();
    recoverCartUseCase.execute(new RecoverCartCommand(anonymousUserId, registeredUserId));

    return redirectAfterLogin(returnUrl, CartPageNotice.WELCOME_BACK, attributes);
  }

  /**
   * Handles the user's cart merge decision.
   *
   * @param strategy the chosen merge strategy
   * @param anonymousUserId the anonymous user ID (from hidden form field)
   * @param returnUrl optional URL to redirect to after merge
   * @param attributes the redirect's attributes, which take the cart page's notice
   * @return redirect to return URL or cart
   */
  @PostMapping
  public String handleMergeDecision(
      @RequestParam final String strategy,
      @RequestParam final String anonymousUserId,
      @RequestParam(required = false) final String returnUrl,
      final RedirectAttributes attributes) {

    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final String registeredUserId = identity.userId().value();

    // Parse strategy
    final CartMergeStrategy mergeStrategy;
    try {
      mergeStrategy = CartMergeStrategy.valueOf(strategy);
    } catch (final IllegalArgumentException e) {
      return CartPageNotice.INVALID_MERGE_OPTION.redirectTo(
          "/cart/merge?anonymousUserId="
              + anonymousUserId
              + (returnUrl != null ? "&returnUrl=" + returnUrl : ""),
          attributes);
    }

    // Execute merge
    final MergeCartsCommand command =
        new MergeCartsCommand(anonymousUserId, registeredUserId, mergeStrategy);
    final MergeCartsResult response = mergeCartsUseCase.execute(command);

    // The notice the cart page shows for the chosen strategy
    final CartPageNotice notice =
        switch (mergeStrategy) {
          case MERGE_BOTH -> CartPageNotice.CARTS_MERGED;
          case USE_ACCOUNT_CART -> CartPageNotice.ACCOUNT_CART_USED;
          case USE_ANONYMOUS_CART -> CartPageNotice.RECENT_CART_USED;
        };
    return redirectAfterLogin(returnUrl, notice, attributes);
  }

  /**
   * Where the login leads once the carts are settled: the page it came from, or the cart. Only the
   * cart page knows the cart's notices, so only a redirect there carries one.
   */
  private static String redirectAfterLogin(
      final @Nullable String returnUrl,
      final CartPageNotice notice,
      final RedirectAttributes attributes) {
    if (returnUrl != null && !returnUrl.isBlank() && !CART_PATH.equals(returnUrl)) {
      return "redirect:" + returnUrl;
    }
    return notice.redirectTo(CART_PATH, attributes);
  }
}
