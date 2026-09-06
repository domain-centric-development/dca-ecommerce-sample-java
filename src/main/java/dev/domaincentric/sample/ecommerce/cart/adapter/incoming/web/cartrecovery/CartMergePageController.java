package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.cart.application.getcartmergeoptions.GetCartMergeOptionsInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.getcartmergeoptions.GetCartMergeOptionsQuery;
import dev.domaincentric.sample.ecommerce.cart.application.getcartmergeoptions.GetCartMergeOptionsResult;
import dev.domaincentric.sample.ecommerce.cart.application.mergecarts.CartMergeStrategy;
import dev.domaincentric.sample.ecommerce.cart.application.mergecarts.MergeCartsCommand;
import dev.domaincentric.sample.ecommerce.cart.application.mergecarts.MergeCartsInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.mergecarts.MergeCartsResult;
import dev.domaincentric.sample.ecommerce.cart.application.recovercart.RecoverCartOnLoginCommand;
import dev.domaincentric.sample.ecommerce.cart.application.recovercart.RecoverCartOnLoginInputPort;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

  private final GetCartMergeOptionsInputPort getCartMergeOptionsUseCase;
  private final MergeCartsInputPort mergeCartsUseCase;
  private final RecoverCartOnLoginInputPort recoverCartOnLoginUseCase;
  private final IdentityProvider identityProvider;

  public CartMergePageController(
      final GetCartMergeOptionsInputPort getCartMergeOptionsUseCase,
      final MergeCartsInputPort mergeCartsUseCase,
      final RecoverCartOnLoginInputPort recoverCartOnLoginUseCase,
      final IdentityProvider identityProvider) {
    this.getCartMergeOptionsUseCase = getCartMergeOptionsUseCase;
    this.mergeCartsUseCase = mergeCartsUseCase;
    this.recoverCartOnLoginUseCase = recoverCartOnLoginUseCase;
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
   * @param redirectAttributes for passing flash messages
   * @return view name "cart/merge-options" or redirect if no merge needed
   */
  @GetMapping
  public String showMergeOptions(
      final Model model,
      @RequestParam final String anonymousUserId,
      @RequestParam(required = false) final String returnUrl,
      final RedirectAttributes redirectAttributes) {

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
      recoverCartOnLoginUseCase.execute(
          new RecoverCartOnLoginCommand(anonymousUserId, registeredUserId));

      redirectAttributes.addFlashAttribute("message", "Welcome back!");

      if (returnUrl != null && !returnUrl.isBlank()) {
        return "redirect:" + returnUrl;
      }
      return "redirect:/cart";
    }

    // Convert to page-specific ViewModel
    final CartMergePageViewModel viewModel =
        CartMergePageViewModel.fromResult(options, anonymousUserId, returnUrl);

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
   * @param redirectAttributes for passing flash messages
   * @return redirect to return URL or cart
   */
  @PostMapping
  public String handleMergeDecision(
      @RequestParam final String strategy,
      @RequestParam final String anonymousUserId,
      @RequestParam(required = false) final String returnUrl,
      final RedirectAttributes redirectAttributes) {

    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final String registeredUserId = identity.userId().value();

    // Parse strategy
    final CartMergeStrategy mergeStrategy;
    try {
      mergeStrategy = CartMergeStrategy.valueOf(strategy);
    } catch (final IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("error", "Invalid merge option selected");
      return "redirect:/cart/merge?anonymousUserId="
          + anonymousUserId
          + (returnUrl != null ? "&returnUrl=" + returnUrl : "");
    }

    // Execute merge
    final MergeCartsCommand command =
        new MergeCartsCommand(anonymousUserId, registeredUserId, mergeStrategy);
    final MergeCartsResult response = mergeCartsUseCase.execute(command);

    // Add success message based on strategy
    final String message =
        switch (mergeStrategy) {
          case MERGE_BOTH -> "Carts merged successfully!";
          case USE_ACCOUNT_CART -> "Using your account cart.";
          case USE_ANONYMOUS_CART -> "Using your recent cart.";
        };
    redirectAttributes.addFlashAttribute("message", message);

    // Redirect to return URL or cart
    if (returnUrl != null && !returnUrl.isBlank()) {
      return "redirect:" + returnUrl;
    }
    return "redirect:/cart";
  }
}
