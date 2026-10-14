package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import java.util.Arrays;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * A sentence the page a registration leads to shows — the home page, or whichever page the visitor
 * came from.
 *
 * <p>The notice travels with the redirect as a flash attribute ({@value #KEY}), which the shop
 * keeps in a sealed cookie addressed to the page the redirect leads to: only its code, never the
 * text. A code the page does not know shows nothing. The layout shows it, as {@code
 * successMessage}, so any page can be the one a registration leads to.
 */
public enum RegistrationNotice {
  ACCOUNT_CREATED("account-created", "Account created successfully! Welcome!");

  /** The flash attribute that carries a registration notice's code. */
  public static final String KEY = "notice.registration";

  /** The model attribute the layout shows as a success. */
  private static final String SUCCESS_MESSAGE = "successMessage";

  private final String code;
  private final String sentence;

  RegistrationNotice(final String code, final String sentence) {
    this.code = code;
    this.sentence = sentence;
  }

  /**
   * Puts the sentence of the notice a redirect handed the page on the page's model, as {@code
   * successMessage}. An absent or unknown code puts nothing.
   *
   * @param model the page's model, where Spring puts the flash attributes addressed to the page
   */
  public static void show(final Model model) {
    fromCode(model.getAttribute(KEY) instanceof String code ? code : null)
        .ifPresent(notice -> model.addAttribute(SUCCESS_MESSAGE, notice.sentence));
  }

  private static Optional<RegistrationNotice> fromCode(final @Nullable String code) {
    return Arrays.stream(values()).filter(notice -> notice.code.equals(code)).findFirst();
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
}
