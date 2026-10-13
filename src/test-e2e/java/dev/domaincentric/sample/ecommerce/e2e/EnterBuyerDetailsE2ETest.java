package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.domaincentric.sample.ecommerce.e2e.pages.BuyerInfoPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A guest tells the shop on the buyer step who they are and how to reach them. */
@DisplayName("Enter the buyer details")
class EnterBuyerDetailsE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Valid details open the delivery step")
  void validDetailsOpenTheDeliveryStep() {
    final BuyerInfoPage buyer = startTheCheckoutWithDomainDrivenDesign();

    buyer.fillBuyerInfo("jane@example.com", "Jane", "Doe", "+49 30 1234567").pressContinue();

    assertThat(page).hasURL(BASE_URL + "/checkout/delivery");
  }

  @Test
  @DisplayName("Invalid email and missing phone keep the buyer step")
  void anInvalidEmailAndAMissingPhoneKeepTheGuestOnTheBuyerStep() {
    final BuyerInfoPage buyer = startTheCheckoutWithDomainDrivenDesign();

    buyer.fillBuyerInfo("not-an-email", "Jane", "Doe", "").pressContinue();

    // The page was not sent: the values typed are still in the fields
    assertThat(page).hasURL(BASE_URL + "/checkout/buyer");
    assertThat(buyer.emailInput()).hasValue("not-an-email");
    assertEquals(true, buyer.emailInput().evaluate("el => el.validity.typeMismatch"));
    assertEquals(true, buyer.phoneInput().evaluate("el => el.validity.valueMissing"));
    assertFalse(((String) buyer.emailInput().evaluate("el => el.validationMessage")).isBlank());
  }

  private BuyerInfoPage startTheCheckoutWithDomainDrivenDesign() {
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();
    return CartPage.navigateTo(page).proceedToCheckout();
  }
}
