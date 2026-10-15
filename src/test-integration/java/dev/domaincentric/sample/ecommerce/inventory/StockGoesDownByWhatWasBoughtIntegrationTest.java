package dev.domaincentric.sample.ecommerce.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.inventory.application.provisioninitialstock.ProvisionInitialStockCommand;
import dev.domaincentric.sample.ecommerce.inventory.application.provisioninitialstock.ProvisionInitialStockInputPort;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.awaitility.Awaitility;
import org.awaitility.core.ThrowingRunnable;
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
 * A placed order lowers the stock of each product by the quantity bought, as the product pages and
 * the catalogue show it: the wired application, a guest walking the checkout pages with their
 * cookies carried from request to request. The stock changes shortly after the order, so the pages
 * are read again until they show it, for at most fifteen seconds.
 */
// MockMvc requests commit, and the stock bought here must not reach the classes asserting the
// seeded stock — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:stock_goes_down_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Stock goes down by what was bought")
class StockGoesDownByWhatWasBoughtIntegrationTest {

  private static final String DESK_MODEL = "Wooden Hexagon Desk Model";
  private static final String DDD = "Domain-Driven Design";
  private static final String CLEAN_ARCHITECTURE = "Clean Architecture";
  private static final Duration SHORTLY = Duration.ofSeconds(15);

  private static final String BUYER_STEP = "/checkout/buyer";
  private static final String DELIVERY_STEP = "/checkout/delivery";
  private static final String PAYMENT_STEP = "/checkout/payment";
  private static final String REVIEW_STEP = "/checkout/review";
  private static final String CONFIRMATION = "/checkout/confirmation";

  @Autowired private MockMvc mockMvc;

  @Autowired private ProvisionInitialStockInputPort provisionInitialStock;

  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  @BeforeEach
  void aNewGuest() {
    cookies.clear();
  }

  @Test
  @DisplayName("Every bought line lowers its own stock")
  void everyBoughtLineLowersTheStockOfItsOwnProduct() throws Exception {
    stockOf(DDD, 20);
    stockOf(CLEAN_ARCHITECTURE, 35);

    addToCart(DDD);
    addToCart(CLEAN_ARCHITECTURE);
    addToCart(CLEAN_ARCHITECTURE);
    placeTheOrder();

    shortly(() -> assertThat(availabilityOf(DDD)).isEqualTo("19 units available"));
    assertThat(availabilityOf(CLEAN_ARCHITECTURE)).isEqualTo("33 units available");
  }

  @Test
  @DisplayName("Orders add up")
  void aSecondOrderLowersTheStockTheFirstLeft() throws Exception {
    stockOf(DESK_MODEL, 8);
    addToCart(DESK_MODEL);
    addToCart(DESK_MODEL);
    placeTheOrder();
    shortly(() -> assertThat(availabilityOf(DESK_MODEL)).isEqualTo("6 units available"));

    aNewGuest();
    addToCart(DESK_MODEL);
    addToCart(DESK_MODEL);
    addToCart(DESK_MODEL);
    placeTheOrder();

    shortly(() -> assertThat(availabilityOf(DESK_MODEL)).isEqualTo("3 units available"));
  }

  @Test
  @DisplayName("Buying the last units sells out")
  void buyingTheLastUnitsSellsTheProductOut() throws Exception {
    stockOf(DESK_MODEL, 8);
    for (int unit = 0; unit < 8; unit++) {
      addToCart(DESK_MODEL);
    }
    placeTheOrder();

    shortly(() -> assertThat(availabilityOf(DESK_MODEL)).isEqualTo("Currently out of stock"));
    assertThat(productPageOf(DESK_MODEL).select("[data-test=product-add-to-cart-button]"))
        .as("the product page offers no Add to Cart")
        .isEmpty();
    assertThat(cardOf(DESK_MODEL).selectFirst(".product-card__badge").text())
        .isEqualTo("Out of Stock");
  }

  /** Waits until the assertion holds, for at most fifteen seconds. */
  private static void shortly(final ThrowingRunnable assertion) {
    Awaitility.await().atMost(SHORTLY).untilAsserted(assertion);
  }

  /** The stock of the named product, set before the guest arrives. */
  private void stockOf(final String productName, final int units) throws Exception {
    final String href = cardOf(productName).selectFirst("a[data-test=view-product]").attr("href");
    provisionInitialStock.execute(
        new ProvisionInitialStockCommand(href.substring(href.lastIndexOf('/') + 1), units));
  }

  /** The value the product page of the named product shows under "Availability". */
  private String availabilityOf(final String productName) throws Exception {
    return productPageOf(productName).select(".product-detail__meta-item").stream()
        .filter(i -> "Availability".equals(i.selectFirst(".product-detail__meta-label").text()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no meta item labelled Availability"))
        .selectFirst(".product-detail__meta-value")
        .text();
  }

  private Document productPageOf(final String productName) throws Exception {
    return page(cardOf(productName).selectFirst("a[data-test=view-product]").attr("href"));
  }

  private Element cardOf(final String productName) throws Exception {
    return page("/products").select("[data-test=product-card]").stream()
        .filter(c -> productName.equals(c.selectFirst("[data-test=product-card-title]").text()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no card titled " + productName));
  }

  /** Presses "Add to Cart" on the product page of the product with that name. */
  private void addToCart(final String productName) throws Exception {
    submit(productPageOf(productName).selectFirst("[data-test=product-add-to-cart-form]"));
  }

  /**
   * The guest starts the checkout from the cart, submits the buyer step, the delivery step with
   * "Standard Shipping", chooses "Mock Payment (Test)" and presses "Place Order" on the review.
   */
  private void placeTheOrder() throws Exception {
    final MvcResult started = submit(page("/cart").selectFirst("[data-test=cart-checkout-form]"));
    assertThat(started.getResponse().getRedirectedUrl()).isEqualTo(BUYER_STEP);

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
    assertThat(perform(delivery).getResponse().getRedirectedUrl()).isEqualTo(PAYMENT_STEP);

    final Element paymentForm = page(PAYMENT_STEP).selectFirst("[data-test=payment-form]");
    final MockHttpServletRequestBuilder payment =
        MockMvcRequestBuilders.post(paymentForm.attr("action"));
    for (final Element field : paymentForm.select("input[name]:not([type=radio])")) {
      payment.param(field.attr("name"), field.val());
    }
    final Element provider =
        paymentForm.select("[data-test=payment-provider-option]").stream()
            .filter(o -> "Mock Payment (Test)".equals(o.selectFirst(".option-card__label").text()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no provider named Mock Payment (Test)"));
    payment.param("providerId", provider.selectFirst("input[name=providerId]").val());
    assertThat(perform(payment).getResponse().getRedirectedUrl()).isEqualTo(REVIEW_STEP);

    final MvcResult placed =
        submit(page(REVIEW_STEP).selectFirst("[data-test=review-confirm-form]"));
    assertThat(placed.getResponse().getRedirectedUrl()).isEqualTo(CONFIRMATION);
  }

  /** Submits a form with the values its fields carry, as the browser would. */
  private MvcResult submit(final Element form) throws Exception {
    final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(form.attr("action"));
    for (final Element field : form.select("input[name]")) {
      request.param(field.attr("name"), field.val());
    }
    final MvcResult result = perform(request);
    assertThat(result.getResponse().getStatus()).as("POST " + form.attr("action")).isEqualTo(302);
    return result;
  }

  private Document page(final String path) throws Exception {
    final MvcResult result = perform(get(path));
    assertThat(result.getResponse().getStatus()).as("GET " + path).isEqualTo(200);
    return Jsoup.parse(result.getResponse().getContentAsString());
  }

  private MvcResult perform(final MockHttpServletRequestBuilder request) throws Exception {
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
