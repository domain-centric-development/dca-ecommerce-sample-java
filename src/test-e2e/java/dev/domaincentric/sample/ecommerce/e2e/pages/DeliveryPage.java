package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page object for the delivery information page in checkout.
 *
 * <p>Provides methods to fill delivery address and select shipping options.
 */
public class DeliveryPage extends BasePage {

  private static final String URL_PATTERN = "/checkout/delivery";
  private static final String CONTINUE_BUTTON = "delivery-continue-button";
  private static final String BACK_LINK = "delivery-back-link";
  private static final String SHIPPING_RADIO = "delivery-shipping-radio";

  /**
   * Creates a new DeliveryPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public DeliveryPage(Page page) {
    super(page, URL_PATTERN);
  }

  /**
   * Creates this page of a shop the test started itself and waits for it to load.
   *
   * @param page the Playwright page instance
   * @param shopUrl the base URL of that shop
   */
  public DeliveryPage(Page page, String shopUrl) {
    super(page, shopUrl, URL_PATTERN);
  }

  /**
   * Fills the street address field.
   *
   * @param street the street address
   * @return this page for method chaining
   */
  public DeliveryPage fillStreet(String street) {
    fill("street", street);
    return this;
  }

  /**
   * Fills the city field.
   *
   * @param city the city name
   * @return this page for method chaining
   */
  public DeliveryPage fillCity(String city) {
    fill("city", city);
    return this;
  }

  /**
   * Fills the postal code field.
   *
   * @param postalCode the postal/ZIP code
   * @return this page for method chaining
   */
  public DeliveryPage fillPostalCode(String postalCode) {
    fill("postalCode", postalCode);
    return this;
  }

  /**
   * Fills the country field.
   *
   * @param country the country name
   * @return this page for method chaining
   */
  public DeliveryPage fillCountry(String country) {
    fill("country", country);
    return this;
  }

  /**
   * Fills the state/province field.
   *
   * @param state the state or province
   * @return this page for method chaining
   */
  public DeliveryPage fillState(String state) {
    fill("state", state);
    return this;
  }

  /**
   * Fills all delivery address fields at once.
   *
   * @param street the street address
   * @param city the city name
   * @param postalCode the postal/ZIP code
   * @param country the country name
   * @param state the state or province
   * @return this page for method chaining
   */
  public DeliveryPage fillAddress(
      String street, String city, String postalCode, String country, String state) {
    return fillStreet(street)
        .fillCity(city)
        .fillPostalCode(postalCode)
        .fillCountry(country)
        .fillState(state);
  }

  /**
   * Selects the first available shipping option.
   *
   * @return this page for method chaining
   */
  public DeliveryPage selectFirstShippingOption() {
    if (exists(SHIPPING_RADIO)) {
      selectFirstRadio(SHIPPING_RADIO);
    }
    return this;
  }

  /**
   * Chooses the shipping method shown under the given name.
   *
   * @param name the shipping method's name, e.g. "Standard Shipping"
   * @return this page for method chaining
   */
  public DeliveryPage chooseShippingMethod(String name) {
    page.locator("[data-test='delivery-shipping-option']")
        .filter(new Locator.FilterOptions().setHasText(name))
        .locator("[data-test='" + SHIPPING_RADIO + "']")
        .check();
    return this;
  }

  /** Presses "Continue to Payment" without assuming where the shop sends the guest. */
  public void pressContinue() {
    click(CONTINUE_BUTTON);
  }

  /**
   * Clicks the continue button to proceed to payment.
   *
   * @return the PaymentPage
   */
  public PaymentPage continueToPayment() {
    click(CONTINUE_BUTTON);
    return new PaymentPage(page, shopUrl);
  }

  /**
   * Navigates back to the buyer info page.
   *
   * @return the BuyerInfoPage
   */
  public BuyerInfoPage goBack() {
    click(BACK_LINK);
    return new BuyerInfoPage(page, shopUrl);
  }

  /**
   * Checks if the back link exists.
   *
   * @return true if back navigation is available
   */
  public boolean hasBackLink() {
    return exists(BACK_LINK);
  }
}
