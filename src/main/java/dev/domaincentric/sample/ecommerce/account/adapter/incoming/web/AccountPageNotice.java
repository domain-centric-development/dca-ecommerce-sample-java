package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import java.util.Arrays;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * A sentence an account page shows after a redirect led the person there.
 *
 * <p>The notice travels with the redirect as a flash attribute ({@value #KEY}), which the shop
 * keeps in a sealed cookie addressed to the page the redirect leads to: only its code, never the
 * text. A code the page does not know shows nothing.
 */
public enum AccountPageNotice {
  PROFILE_UPDATED("profile-updated", "Your profile has been updated."),
  PASSWORD_CHANGED("password-changed", "Your password has been changed.");

  /** The flash attribute that carries an account page notice's code. */
  public static final String KEY = "notice.account";

  private final String code;
  private final String sentence;

  AccountPageNotice(final String code, final String sentence) {
    this.code = code;
    this.sentence = sentence;
  }

  /**
   * The notice with that code.
   *
   * @param code the code a redirect carried, absent when it carried none
   * @return the notice, or empty for an absent or unknown code
   */
  public static Optional<AccountPageNotice> fromCode(final @Nullable String code) {
    return Arrays.stream(values()).filter(notice -> notice.code.equals(code)).findFirst();
  }

  /**
   * The notice a redirect handed the page.
   *
   * @param model the page's model, where Spring puts the flash attributes addressed to the page
   * @return the notice, or empty when none came or its code is unknown
   */
  public static Optional<AccountPageNotice> in(final Model model) {
    return fromCode(model.getAttribute(KEY) instanceof String code ? code : null);
  }

  /**
   * The Spring MVC redirect to a page, handing it this notice.
   *
   * @param path the page's path
   * @param attributes the redirect's attributes, which take the notice
   * @return {@code redirect:} followed by the path
   */
  public String redirectTo(final String path, final RedirectAttributes attributes) {
    attributes.addFlashAttribute(KEY, code);
    return "redirect:" + path;
  }

  public String code() {
    return code;
  }

  public String sentence() {
    return sentence;
  }
}
