package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * The header every storefront page shares, whichever page is open.
 *
 * <p>Reads the logo and follows it home, and opens the basket dropdown of the cart entry.
 */
public class SiteHeader extends BasePage {

  private static final String SITE_HEADER = "site-header";
  private static final String LOGO = "site-logo";
  private static final String GREETING = "user-greeting";
  private static final String REGISTER_LINK = "nav-register-link";
  private static final String BASKET_TRIGGER = "mini-basket-trigger";
  private static final String BASKET_DROPDOWN = "mini-basket-dropdown";
  private static final String BASKET_VIEW_CART = "mini-basket-view-cart-link";
  // The dropdown's lines and total carry no data-test; adopting the header changes no template.
  private static final String BASKET_ITEM = ".mini-basket__item";
  private static final String BASKET_TOTAL = ".mini-basket__total";

  /**
   * Creates the header of the page that is open and waits for it.
   *
   * @param page the Playwright page instance
   */
  public SiteHeader(Page page) {
    super(page);
    waitFor(SITE_HEADER);
  }

  /**
   * The logo's name as the visitor reads it.
   *
   * @return the logo text, without whitespace between its parts
   */
  public String logoName() {
    return page.locator("[data-test='" + LOGO + "']").textContent().replaceAll("\\s+", "");
  }

  /**
   * The greeting a logged-in visitor reads.
   *
   * @return the greeting text
   */
  public String greeting() {
    waitFor(GREETING);
    return page.locator("[data-test='" + GREETING + "']").textContent().strip();
  }

  /**
   * Whether the header greets a logged-in visitor.
   *
   * @return true when the greeting is shown
   */
  public boolean greets() {
    return exists(GREETING);
  }

  /**
   * Whether the header offers the "Register" link.
   *
   * @return true when the link is shown
   */
  public boolean offersRegister() {
    return exists(REGISTER_LINK);
  }

  /**
   * Follows the logo.
   *
   * @return the home page it opens
   */
  public HomePage followLogo() {
    click(LOGO);
    return new HomePage(page);
  }

  /**
   * Hovers the header's cart entry and waits for the dropdown it reveals.
   *
   * @return this header, its basket dropdown open
   */
  public SiteHeader hoverBasket() {
    page.locator("[data-test='" + BASKET_TRIGGER + "']").hover();
    waitFor(BASKET_DROPDOWN);
    return this;
  }

  /**
   * The quantity the open basket dropdown shows beside the named product.
   *
   * @param name the product name as the dropdown shows it
   * @return the quantity text, such as "x2"
   */
  public String basketQuantityOf(String name) {
    return basketDropdown()
        .locator(BASKET_ITEM)
        .filter(
            new Locator.FilterOptions()
                .setHas(page.locator("span", new Page.LocatorOptions().setHasText(name))))
        .locator("span")
        .nth(1)
        .innerText()
        .strip();
  }

  /**
   * The label of the open basket dropdown's total row.
   *
   * @return the label text, trimmed
   */
  public String basketTotalLabel() {
    return basketDropdown().locator(BASKET_TOTAL + " span").nth(0).innerText().strip();
  }

  /**
   * The amount of the open basket dropdown's total row.
   *
   * @return the amount text, trimmed
   */
  public String basketTotal() {
    return basketDropdown().locator(BASKET_TOTAL + " span").nth(1).innerText().strip();
  }

  /**
   * The label of the open basket dropdown's button to the cart.
   *
   * @return the button text, trimmed
   */
  public String basketViewCartLabel() {
    return basketViewCart().innerText().strip();
  }

  /**
   * Where the open basket dropdown's button to the cart leads.
   *
   * @return the button's link target
   */
  public String basketViewCartTarget() {
    return basketViewCart().getAttribute("href");
  }

  private Locator basketViewCart() {
    return basketDropdown().locator("[data-test='" + BASKET_VIEW_CART + "']");
  }

  private Locator basketDropdown() {
    return page.locator("[data-test='" + BASKET_DROPDOWN + "']");
  }
}
