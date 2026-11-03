package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Words the {@link RegistrationNotice} on whichever page a registration leads to.
 *
 * <p>The registration sends the visitor to the home page or to the page they came from — a page of
 * any context — so the notice is not read by one controller but here, for every page, and the
 * layout shows it. Spring has put the flash attributes addressed to the page on the model before
 * this runs; a page the notice is not addressed to gets none.
 */
@ControllerAdvice
public class RegistrationNoticeControllerAdvice {

  /** The model attribute the layout shows as a success. */
  private static final String SUCCESS_MESSAGE = "successMessage";

  /**
   * Puts the sentence of the registration notice a redirect handed the page on its model, as {@code
   * successMessage}. An absent or unknown code puts nothing.
   *
   * @param model the page's model
   */
  @ModelAttribute
  public void showRegistrationNotice(final Model model) {
    RegistrationNotice.in(model)
        .ifPresent(notice -> model.addAttribute(SUCCESS_MESSAGE, notice.sentence()));
  }
}
