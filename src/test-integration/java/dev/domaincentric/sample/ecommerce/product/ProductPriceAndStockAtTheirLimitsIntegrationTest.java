package dev.domaincentric.sample.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * A product created over the REST API at the edge of what a price and an initial stock may be — the
 * highest price, no stock at all — and its card on the catalogue page as a visitor opens it. Price
 * and stock reach the catalogue by event, so the card is awaited.
 */
// A database of its own: the products created here must not reach a catalogue other tests count.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:product_price_stock_limits_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("A product's price and stock at their limits")
class ProductPriceAndStockAtTheirLimitsIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Autowired private MockMvc mockMvc;

  @Autowired private TokenService tokenService;

  @Test
  @DisplayName("The highest price is accepted")
  void theHighestPriceIsAcceptedAndShownOnTheCard() throws Exception {
    final MvcResult result =
        create(
            """
            {"sku":"MAX-001","name":"Priceless Model","price":999999999999.99,\
            "category":"Modeling","stock":1}""");

    assertThat(result.getResponse().getStatus()).isEqualTo(201);
    Awaitility.await()
        .atMost(Duration.ofSeconds(15))
        .untilAsserted(
            () ->
                assertThat(cardOf("Priceless Model").selectFirst(".product-card__price").text())
                    .isEqualTo("999999999999.99 EUR"));
  }

  @Test
  @DisplayName("An initial stock of zero is accepted")
  void anInitialStockOfZeroIsAcceptedAndTheCardShowsOutOfStock() throws Exception {
    final MvcResult result =
        create(
            """
            {"sku":"PIN-002","name":"Sold Out Pin","price":12.99,"category":"Stickers & Pins",\
            "stock":0}""");

    assertThat(result.getResponse().getStatus()).isEqualTo(201);
    Awaitility.await()
        .atMost(Duration.ofSeconds(15))
        .untilAsserted(
            () -> {
              final Element card = cardOf("Sold Out Pin");
              assertThat(card.selectFirst(".product-card__price").text()).isEqualTo("12.99 EUR");
              assertThat(card.selectFirst(".product-card__badge").text()).isEqualTo("Out of Stock");
            });
  }

  private Element cardOf(final String name) throws Exception {
    return Jsoup.parse(
            mockMvc
                .perform(get("/products"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString())
        .select("[data-test=product-card]")
        .stream()
        .filter(card -> name.equals(card.selectFirst("[data-test=product-card-title]").text()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no card " + name));
  }

  private MvcResult create(final String product) throws Exception {
    return mockMvc
        .perform(
            post("/api/products")
                .header("Authorization", "Bearer " + staffToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(product))
        .andReturn();
  }

  /**
   * A staff token for an account registered through the API, minted by the shop's own token
   * service: no registration hands out the staff role.
   */
  private String staffToken() throws Exception {
    final String body =
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"email":"%s@example.com","password":"Secret123","firstName":"Ada",\
                        "lastName":"Lovelace","dateOfBirth":"1815-12-10"}"""
                            .formatted(UUID.randomUUID())))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    final String token = JSON.readTree(body).path("token").asText();
    final JsonNode claims = JSON.readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
    return tokenService.generateRegisteredToken(
        UserId.of(claims.path("sub").asText()),
        claims.path("email").asText(),
        Set.of(IdentityProvider.Identity.ROLE_CUSTOMER, IdentityProvider.Identity.ROLE_STAFF));
  }
}
