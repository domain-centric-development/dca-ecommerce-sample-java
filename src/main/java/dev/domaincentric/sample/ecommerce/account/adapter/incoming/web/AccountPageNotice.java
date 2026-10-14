package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import java.util.Arrays;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * A sentence an account page shows after a redirect led the person there.
 *
 * <p>The shop runs as several instances without sticky sessions, so the notice travels with the
 * redirect as the query parameter {@value #PARAMETER}: only its code, never the text, so a URL
 * cannot make a page say anything else. A code the page does not know shows nothing.
 */
public enum AccountPageNotice {
  PROFILE_UPDATED("profile-updated", "Your profile has been updated."),
  PASSWORD_CHANGED("password-changed", "Your password has been changed.");

  /** The query parameter that carries a notice's code. */
  public static final String PARAMETER = "notice";

  private final String code;
  private final String sentence;

  AccountPageNotice(final String code, final String sentence) {
    this.code = code;
    this.sentence = sentence;
  }

  /**
   * The notice with that code.
   *
   * @param code the parameter's value, absent when the request carries none
   * @return the notice, or empty for an absent or unknown code
   */
  public static Optional<AccountPageNotice> fromCode(final @Nullable String code) {
    return Arrays.stream(values()).filter(notice -> notice.code.equals(code)).findFirst();
  }

  /**
   * The Spring MVC redirect to a page, carrying this notice.
   *
   * @param path the page's path, without a query
   * @return {@code redirect:} followed by the path with the notice
   */
  public String redirectTo(final String path) {
    return "redirect:" + path + "?" + PARAMETER + "=" + code;
  }

  public String code() {
    return code;
  }

  public String sentence() {
    return sentence;
  }
}
