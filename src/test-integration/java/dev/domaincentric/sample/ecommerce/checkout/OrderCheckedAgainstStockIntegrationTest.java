package dev.domaincentric.sample.ecommerce.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.inventory.application.provisioninitialstock.ProvisionInitialStockCommand;
import dev.domaincentric.sample.ecommerce.inventory.application.provisioninitialstock.ProvisionInitialStockInputPort;
import jakarta.servlet.http.Cookie;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.awaitility.Awaitility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Starting the checkout and placing the order while another visitor buys the same product, rendered
 * by the wired application — each visitor's cookies carried from request to request, and a
 * redirect's message carried to the page it leads to, as a browser would. The other visitor's order
 * lowers the stock shortly after it is placed, so each test waits for the product page to show it.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:order_checked_against_stock_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Starting and placing check the cart against what can still be sold")
class OrderCheckedAgainstStockIntegrationTest {

  private static final String DESK_MODEL = "Wooden Hexagon Desk Model";
  private static final String DDD_BOOK = "Domain-Driven Design";
  private static final int SEEDED_STOCK = 8;

  private static final String CART = "/cart";
  private static final String BUYER_STEP = "/checkout/buyer";
  private static final String DELIVERY_STEP = "/checkout/delivery";
  private static final String PAYMENT_STEP = "/checkout/payment";
  private static final String REVIEW_STEP = "/checkout/review";
  private static final String CONFIRMATION = "/checkout/confirmation";

  @Autowired private MockMvc mockMvc;
  @Autowired private ProvisionInitialStockInputPort provisionInitialStock;

  private Visitor guest;
  private Visitor anotherVisitor;

  @BeforeEach
  void theDeskModelHasItsSeededStock() throws Exception {
    guest = new Visitor();
    anotherVisitor = new Visitor();
    provisionInitialStock.execute(
        new ProvisionInitialStockCommand(guest.productIdOf(DESK_MODEL), SEEDED_STOCK));
    guest.waitForAvailability(DESK_MODEL, SEEDED_STOCK + " units available");
  }

  @Test
  @DisplayName("Cart shows cannot checkout when stock ran short")
  void theCartOffersNoCheckoutOnceAnotherVisitorBoughtTheStockAway() throws Exception {
    guest.addToCart(DESK_MODEL, 5);
    anotherVisitor.buys(DESK_MODEL, 5);
    guest.waitForAvailability(DESK_MODEL, "3 units available");

    final Document cart = guest.page(CART);

    assertThat(cart.selectFirst("[data-test=cart-checkout-button]")).isNull();
    final Element disabled = cart.selectFirst("[data-test=cart-checkout-disabled]");
    assertThat(disabled).isNotNull();
    assertThat(disabled.text()).isEqualTo("Cannot Checkout");
    assertThat(disabled.hasAttr("disabled")).isTrue();
    assertThat(cart.selectFirst(".cart__actions-warning").text())
        .isEqualTo("Some items are unavailable or have insufficient stock");
  }

  @Test
  @DisplayName("Start from an outdated cart page is refused")
  void proceedingFromACartPageOpenedBeforeTheStockRanShortIsRefused() throws Exception {
    guest.addToCart(DESK_MODEL, 5);
    final Element outdatedForm = guest.page(CART).selectFirst("[data-test=cart-checkout-form]");
    assertThat(outdatedForm.selectFirst("[data-test=cart-checkout-button]").text())
        .isEqualTo("Proceed to Checkout");
    anotherVisitor.buys(DESK_MODEL, 5);
    guest.waitForAvailability(DESK_MODEL, "3 units available");

    final MvcResult proceeded = guest.submit(outdatedForm);

    assertThat(URI.create(proceeded.getResponse().getRedirectedUrl()).getPath()).isEqualTo(CART);
    assertThat(cartErrorOn(guest.followed(proceeded)))
        .startsWith("Cannot start checkout, 1 item(s) unavailable or out of stock");
  }

  @Test
  @DisplayName("Start from an emptied cart is refused")
  void proceedingFromACartPageOpenedBeforeTheCartWasOrderedIsRefused() throws Exception {
    guest.addToCart(DDD_BOOK, 1);
    final Element outdatedForm = guest.page(CART).selectFirst("[data-test=cart-checkout-form]");
    assertThat(outdatedForm.selectFirst("[data-test=cart-checkout-button]").text())
        .isEqualTo("Proceed to Checkout");
    guest.placeTheOrderFromTheCart();
    Awaitility.await()
        .atMost(Duration.ofSeconds(15))
        .until(() -> guest.page(CART).select("[data-test=cart-item]").isEmpty());

    final MvcResult proceeded = guest.submit(outdatedForm);

    assertThat(URI.create(proceeded.getResponse().getRedirectedUrl()).getPath()).isEqualTo(CART);
    assertThat(cartErrorOn(guest.followed(proceeded))).isEqualTo("Cannot check out an empty cart.");
  }

  @Test
  @DisplayName("Place order is refused when sold out")
  void placingTheOrderAfterAnotherVisitorBoughtEverythingIsRefused() throws Exception {
    guest.addToCart(DESK_MODEL, 2);
    final Element confirmForm =
        guest.walkToTheReview().selectFirst("[data-test=review-confirm-form]");
    anotherVisitor.buys(DESK_MODEL, SEEDED_STOCK);
    guest.waitForAvailability(DESK_MODEL, "Currently out of stock");

    final MvcResult placed = guest.submit(confirmForm);

    assertThat(URI.create(placed.getResponse().getRedirectedUrl()).getPath())
        .isEqualTo(REVIEW_STEP);
    final Document review = guest.followed(placed);
    assertThat(review.selectFirst("[data-test=review-error-message]")).isNotNull();
    assertThat(review.selectFirst("[data-test=review-error-message]").text()).isNotBlank();
    assertThat(review.selectFirst("[data-test=confirmation-title]")).isNull();
    assertThat(
            URI.create(guest.perform(get(CONFIRMATION)).getResponse().getRedirectedUrl()).getPath())
        .as("no confirmed order to show")
        .isEqualTo(CART);
  }

  /** The cart page's error message, or null when it shows none. */
  private static String cartErrorOn(final Document page) {
    final Element error = page.selectFirst("[data-test=cart-error-message]");
    return error == null ? null : error.text();
  }

  /** One browser: its own cookies, so its own cart and its own checkout. */
  private final class Visitor {

    private final Map<String, Cookie> cookies = new LinkedHashMap<>();

    /** Presses "Add to Cart" on the product page of the product with that name, that often. */
    void addToCart(final String productName, final int times) throws Exception {
      final String productPage = productPageOf(productName);
      for (int i = 0; i < times; i++) {
        submit(page(productPage).selectFirst("[data-test=product-add-to-cart-form]"));
      }
    }

    /** Puts the product in an empty cart that often and places the order for it. */
    void buys(final String productName, final int times) throws Exception {
      addToCart(productName, times);
      placeTheOrderFromTheCart();
    }

    /** Starts the checkout from the cart page, walks every step and presses "Place Order". */
    void placeTheOrderFromTheCart() throws Exception {
      final MvcResult placed =
          submit(walkToTheReview().selectFirst("[data-test=review-confirm-form]"));
      assertThat(URI.create(placed.getResponse().getRedirectedUrl()).getPath())
          .isEqualTo(CONFIRMATION);
      page(CONFIRMATION);
    }

    /**
     * Starts the checkout from the cart page, submits the buyer step, the delivery step with
     * "Standard Shipping", chooses "Mock Payment (Test)" and opens the review.
     */
    Document walkToTheReview() throws Exception {
      final MvcResult started = submit(page(CART).selectFirst("[data-test=cart-checkout-form]"));
      assertThat(URI.create(started.getResponse().getRedirectedUrl()).getPath())
          .isEqualTo(BUYER_STEP);

      final Element buyerForm = page(BUYER_STEP).selectFirst("[data-test=buyer-form]");
      buyerForm.selectFirst("input[name=email]").val("jane@example.com");
      buyerForm.selectFirst("input[name=firstName]").val("Jane");
      buyerForm.selectFirst("input[name=lastName]").val("Doe");
      buyerForm.selectFirst("input[name=phone]").val("+49 30 1234567");
      assertThat(submit(buyerForm).getResponse().getRedirectedUrl()).isEqualTo(DELIVERY_STEP);

      final Element deliveryForm = page(DELIVERY_STEP).selectFirst("[data-test=delivery-form]");
      deliveryForm.selectFirst("input[name=street]").val("Hauptstrasse 1");
      deliveryForm.selectFirst("input[name=city]").val("Berlin");
      deliveryForm.selectFirst("input[name=postalCode]").val("10115");
      deliveryForm.selectFirst("input[name=country]").val("Germany");
      final MockHttpServletRequestBuilder delivery =
          MockMvcRequestBuilders.post(deliveryForm.attr("action"));
      for (final Element field :
          deliveryForm.select("input[name]:not([disabled]):not([type=radio])")) {
        delivery.param(field.attr("name"), field.val());
      }
      final Element method =
          deliveryForm.select("[data-test=delivery-shipping-option]").stream()
              .filter(o -> "Standard Shipping".equals(o.selectFirst(".option-card__label").text()))
              .findFirst()
              .orElseThrow(() -> new AssertionError("no shipping method named Standard Shipping"));
      for (final Element field : method.select("input[name]")) {
        delivery.param(field.attr("name"), field.val());
      }
      assertThat(URI.create(perform(delivery).getResponse().getRedirectedUrl()).getPath())
          .isEqualTo(PAYMENT_STEP);

      final Element paymentForm = page(PAYMENT_STEP).selectFirst("[data-test=payment-form]");
      final MockHttpServletRequestBuilder payment =
          MockMvcRequestBuilders.post(paymentForm.attr("action"));
      for (final Element field : paymentForm.select("input[name]:not([type=radio])")) {
        payment.param(field.attr("name"), field.val());
      }
      final Element provider =
          paymentForm.select("[data-test=payment-provider-option]").stream()
              .filter(
                  o -> "Mock Payment (Test)".equals(o.selectFirst(".option-card__label").text()))
              .findFirst()
              .orElseThrow(() -> new AssertionError("no provider named Mock Payment (Test)"));
      payment.param("providerId", provider.selectFirst("input[name=providerId]").val());
      assertThat(URI.create(perform(payment).getResponse().getRedirectedUrl()).getPath())
          .isEqualTo(REVIEW_STEP);

      return page(REVIEW_STEP);
    }

    /**
     * Waits until the product page reads that availability — the stock changes shortly after an
     * order or a stock change elsewhere.
     */
    void waitForAvailability(final String productName, final String availability) throws Exception {
      final String productPage = productPageOf(productName);
      Awaitility.await()
          .atMost(Duration.ofSeconds(15))
          .until(() -> availability.equals(availabilityOn(page(productPage))));
    }

    /** The product's id, as its product page address carries it. */
    String productIdOf(final String productName) throws Exception {
      return productPageOf(productName).substring("/products/".length());
    }

    private String productPageOf(final String productName) throws Exception {
      return page("/products").select("[data-test=product-card]").stream()
          .filter(c -> productName.equals(c.selectFirst("[data-test=product-card-title]").text()))
          .findFirst()
          .orElseThrow(() -> new AssertionError("no card titled " + productName))
          .selectFirst("a[data-test=view-product]")
          .attr("href");
    }

    private static String availabilityOn(final Document productPage) {
      return productPage.select(".product-detail__meta-item").stream()
          .filter(
              item -> "Availability".equals(item.selectFirst(".product-detail__meta-label").text()))
          .findFirst()
          .map(item -> item.selectFirst(".product-detail__meta-value").text())
          .orElse(null);
    }

    /** Submits a form with the values its fields carry, as the browser would. */
    MvcResult submit(final Element form) throws Exception {
      final MockHttpServletRequestBuilder request =
          MockMvcRequestBuilders.post(form.attr("action"));
      for (final Element field : form.select("input[name]")) {
        request.param(field.attr("name"), field.val());
      }
      final MvcResult result = perform(request);
      assertThat(result.getResponse().getStatus()).as("POST " + form.attr("action")).isEqualTo(302);
      return result;
    }

    /** Follows a redirect with the messages it carries, as the browser would. */
    Document followed(final MvcResult redirect) throws Exception {
      final String target = redirect.getResponse().getRedirectedUrl();
      final MvcResult result = perform(get(URI.create(target)));
      assertThat(result.getResponse().getStatus()).as("GET " + target).isEqualTo(200);
      return Jsoup.parse(result.getResponse().getContentAsString());
    }

    Document page(final String path) throws Exception {
      final MvcResult result = perform(get(path));
      assertThat(result.getResponse().getStatus()).as("GET " + path).isEqualTo(200);
      return Jsoup.parse(result.getResponse().getContentAsString());
    }

    MvcResult perform(final MockHttpServletRequestBuilder request) throws Exception {
      if (!cookies.isEmpty()) {
        request.cookie(cookies.values().toArray(Cookie[]::new));
      }
      final MvcResult result = mockMvc.perform(request).andReturn();
      for (final Cookie cookie : result.getResponse().getCookies()) {
        if (cookie.getMaxAge() == 0) {
          cookies.remove(cookie.getName());
        } else {
          cookies.put(cookie.getName(), cookie);
        }
      }
      return result;
    }
  }
}
