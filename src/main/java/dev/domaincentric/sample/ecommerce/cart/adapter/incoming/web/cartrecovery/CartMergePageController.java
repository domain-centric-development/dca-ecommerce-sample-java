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
import dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.recovercart.RecoverCartResult;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
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
 * <p><b>Whose cart:</b> the anonymous cart is only ever the one of the guest this browser was
 * before the login, as the identity names it ({@link IdentityProvider.Identity#guestBeforeLogin()}
 * — the browser's own signed session). No request parameter names it, so a request cannot reach
 * another guest's cart; a take-over or merge with nothing to bring shows the cart as it is, without
 * a notice.
 *
 * <p><b>Where it leads:</b> to the return URL only when it is a page of the shop, otherwise to the
 * cart page.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/cart/merge-options.pug} and
 * {@code src/main/resources/templates/cart/take-over.pug}
 */
@Controller
@RequestMapping("/cart/merge")
public class CartMergePageController {

  private static final String CART_PATH = "/cart";

  /** The cart as it is, without a notice: where a form that brought nothing along leads. */
  private static final String CART_AS_IT_IS = "redirect:" + CART_PATH;

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
   * @param returnUrl optional URL to redirect to after merge
   * @param attributes the redirect's attributes, which take the cart page's notice
   * @return view name "cart/merge-options" or "cart/take-over", or a redirect if neither is needed
   */
  @GetMapping
  public String showMergeOptions(
      final Model model,
      @RequestParam(required = false) final String returnUrl,
      final RedirectAttributes attributes) {

    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final Optional<UserId> guest = identity.guestBeforeLogin();
    if (guest.isEmpty()) {
      return redirectAfterLogin(returnUrl, CartPageNotice.WELCOME_BACK, attributes);
    }

    final GetCartMergeOptionsResult options =
        getCartMergeOptionsUseCase.execute(
            new GetCartMergeOptionsQuery(guest.get().value(), identity.userId().value()));

    if (!options.mergeRequired()) {
      if (options.anonymousCart() == null) {
        return redirectAfterLogin(returnUrl, CartPageNotice.WELCOME_BACK, attributes);
      }
      model.addAttribute("returnUrl", returnUrl);
      model.addAttribute("title", "Bringing your cart along");
      return "cart/take-over";
    }

    CartPageNotice.show(model);
    model.addAttribute("cartMerge", CartMergePageViewModel.fromResult(options, returnUrl));
    model.addAttribute("title", "Cart Merge Options");

    return "cart/merge-options";
  }

  /**
   * Takes the anonymous cart into the account's cart: the form of the take-over page, sent by the
   * browser's script or with "Continue".
   *
   * @param returnUrl optional URL to redirect to after the take-over
   * @param attributes the redirect's attributes, which take the cart page's notice
   * @return redirect to return URL or cart; to the cart without a notice when nothing moved
   */
  @PostMapping("/take-over")
  public String takeOverGuestCart(
      @RequestParam(required = false) final String returnUrl, final RedirectAttributes attributes) {

    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final Optional<UserId> guest = identity.guestBeforeLogin();
    if (guest.isEmpty()) {
      return CART_AS_IT_IS;
    }

    final RecoverCartResult result =
        recoverCartUseCase.execute(
            new RecoverCartCommand(guest.get().value(), identity.userId().value()));
    if (result.cartId() == null) {
      return CART_AS_IT_IS;
    }

    return redirectAfterLogin(returnUrl, CartPageNotice.WELCOME_BACK, attributes);
  }

  /**
   * Handles the user's cart merge decision.
   *
   * @param strategy the chosen merge strategy
   * @param returnUrl optional URL to redirect to after merge
   * @param attributes the redirect's attributes, which take the cart page's notice
   * @return redirect to return URL or cart; to the cart without a notice when nothing moved
   */
  @PostMapping
  public String handleMergeDecision(
      @RequestParam final String strategy,
      @RequestParam(required = false) final String returnUrl,
      final RedirectAttributes attributes) {

    final CartMergeStrategy mergeStrategy;
    try {
      mergeStrategy = CartMergeStrategy.valueOf(strategy);
    } catch (final IllegalArgumentException e) {
      return CartPageNotice.INVALID_MERGE_OPTION.redirectTo(
          "/cart/merge"
              + (returnUrl != null
                  ? "?returnUrl=" + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8)
                  : ""),
          attributes);
    }

    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final Optional<UserId> guest = identity.guestBeforeLogin();
    if (guest.isEmpty()) {
      return CART_AS_IT_IS;
    }

    final MergeCartsResult result =
        mergeCartsUseCase.execute(
            new MergeCartsCommand(guest.get().value(), identity.userId().value(), mergeStrategy));
    if (!result.anonymousCartDeleted()) {
      return CART_AS_IT_IS;
    }

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
   * Where the login leads once the carts are settled: the page of the shop it came from, or the
   * cart. Only the cart page knows the cart's notices, so only a redirect there carries one.
   */
  private static String redirectAfterLogin(
      final @Nullable String returnUrl,
      final CartPageNotice notice,
      final RedirectAttributes attributes) {
    if (isShopPage(returnUrl) && !CART_PATH.equals(returnUrl)) {
      return "redirect:" + returnUrl;
    }
    return notice.redirectTo(CART_PATH, attributes);
  }

  /**
   * Whether a return URL is a page of the shop: a path of its own, not an address of another site
   * (an absolute URL, or a scheme-relative {@code //host} the browser would leave the shop for).
   */
  private static boolean isShopPage(final @Nullable String returnUrl) {
    return returnUrl != null
        && returnUrl.startsWith("/")
        && !returnUrl.startsWith("//")
        && !returnUrl.startsWith("/\\");
  }
}
