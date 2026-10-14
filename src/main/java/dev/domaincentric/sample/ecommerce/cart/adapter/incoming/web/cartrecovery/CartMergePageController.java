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

/**
 * MVC Controller for cart merge options page.
 *
 * <p>This controller handles the cart merge decision flow when a user logs in and both their
 * anonymous cart and account cart have items.
 *
 * <p><b>Stateless Design:</b> This controller uses URL parameters instead of HTTP sessions to pass
 * state between requests. The anonymous user ID and return URL are passed via URL, making it
 * compatible with session-disabled configurations.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/cart/merge-options.pug}
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
   * Displays the cart merge options page.
   *
   * <p>Shows the user both carts and lets them choose how to handle the conflict. If no merge is
   * required, redirects to the return URL or cart page.
   *
   * @param model Spring MVC model
   * @param anonymousUserId the anonymous user ID (passed via URL from login)
   * @param returnUrl optional URL to redirect to after merge
   * @param noticeCode the code of the notice a redirect brought along, if any
   * @return view name "cart/merge-options" or redirect if no merge needed
   */
  @GetMapping
  public String showMergeOptions(
      final Model model,
      @RequestParam final String anonymousUserId,
      @RequestParam(required = false) final String returnUrl,
      @RequestParam(name = CartPageNotice.PARAMETER, required = false)
          final @Nullable String noticeCode) {

    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final String registeredUserId = identity.userId().value();

    // Get merge options
    final GetCartMergeOptionsQuery query =
        new GetCartMergeOptionsQuery(anonymousUserId, registeredUserId);
    final GetCartMergeOptionsResult options = getCartMergeOptionsUseCase.execute(query);

    if (!options.mergeRequired()) {
      // Nothing to decide does not mean nothing to do: when only the anonymous cart holds items,
      // they still
      // have to follow the visitor into their account, or logging in would silently empty the cart.
      // The use
      // case is idempotent, so the other cases pass through it untouched.
      recoverCartUseCase.execute(new RecoverCartCommand(anonymousUserId, registeredUserId));

      return redirectAfterLogin(returnUrl, CartPageNotice.WELCOME_BACK);
    }

    // Convert to page-specific ViewModel
    final CartMergePageViewModel viewModel =
        CartMergePageViewModel.fromResult(options, anonymousUserId, returnUrl);

    CartPageNotice.show(model, noticeCode, null);
    model.addAttribute("cartMerge", viewModel);
    model.addAttribute("title", "Cart Merge Options");

    return "cart/merge-options";
  }

  /**
   * Handles the user's cart merge decision.
   *
   * @param strategy the chosen merge strategy
   * @param anonymousUserId the anonymous user ID (from hidden form field)
   * @param returnUrl optional URL to redirect to after merge
   * @return redirect to return URL or cart
   */
  @PostMapping
  public String handleMergeDecision(
      @RequestParam final String strategy,
      @RequestParam final String anonymousUserId,
      @RequestParam(required = false) final String returnUrl) {

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
              + (returnUrl != null ? "&returnUrl=" + returnUrl : ""));
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
    return redirectAfterLogin(returnUrl, notice);
  }

  /**
   * Where the login leads once the carts are settled: the page it came from, or the cart. Only the
   * cart page knows the cart's notices, so only a redirect there carries one.
   */
  private static String redirectAfterLogin(
      final @Nullable String returnUrl, final CartPageNotice notice) {
    if (returnUrl != null && !returnUrl.isBlank() && !CART_PATH.equals(returnUrl)) {
      return "redirect:" + returnUrl;
    }
    return notice.redirectTo(CART_PATH);
  }
}
