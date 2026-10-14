package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import java.util.Arrays;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * A sentence a checkout step page shows after a redirect led the customer there.
 *
 * <p>The shop runs as several instances without sticky sessions, so the notice travels with the
 * redirect as the query parameter {@value #PARAMETER}: only its code, never the text, so a URL
 * cannot make a page say anything else.
 */
public enum StepNotice {
  EARLIER_STEPS_FIRST("earlier-steps-first", "Please complete the previous checkout steps first.");

  /** The query parameter that carries a notice's code. */
  public static final String PARAMETER = "notice";

  private final String code;
  private final String sentence;

  StepNotice(final String code, final String sentence) {
    this.code = code;
    this.sentence = sentence;
  }

  /**
   * The notice with that code.
   *
   * @param code the parameter's value, absent when the request carries none
   * @return the notice, or empty for an absent or unknown code
   */
  public static Optional<StepNotice> fromCode(final @Nullable String code) {
    return Arrays.stream(values()).filter(notice -> notice.code.equals(code)).findFirst();
  }

  public String code() {
    return code;
  }

  public String sentence() {
    return sentence;
  }
}
