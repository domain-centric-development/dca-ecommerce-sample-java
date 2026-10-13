package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the payment selection page in checkout.
 *
 * <p>Provides methods to select payment provider and proceed to review.
 */
public class PaymentPage extends BasePage {

  private static final String URL_PATTERN = "/checkout/payment";
  private static final String CONTINUE_BUTTON = "payment-continue-button";
  private static final String PROVIDER_RADIO = "payment-provider-radio";

  /**
   * Creates a new PaymentPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public PaymentPage(Page page) {
    super(page, URL_PATTERN);
  }

  /**
   * Selects the first available payment provider.
   *
   * @return this page for method chaining
   */
  public PaymentPage selectFirstPaymentProvider() {
    if (exists(PROVIDER_RADIO)) {
      selectFirstRadio(PROVIDER_RADIO);
    }
    return this;
  }

  /**
   * Chooses the one payment provider the page offers; fails when it offers more than one.
   *
   * @return this page for method chaining
   */
  public PaymentPage chooseTheOfferedProvider() {
    page.locator("[data-test='" + PROVIDER_RADIO + "']").check();
    return this;
  }

  /** Presses "Continue to Review" without assuming where the shop sends the guest. */
  public void pressContinue() {
    click(CONTINUE_BUTTON);
  }

  /**
   * Clicks the continue button to proceed to review.
   *
   * @return the ReviewPage
   */
  public ReviewPage continueToReview() {
    click(CONTINUE_BUTTON);
    return new ReviewPage(page);
  }
}
