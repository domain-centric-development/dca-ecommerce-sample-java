package dev.domaincentric.sample.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.product.application.createproduct.CreateProductCommand;
import dev.domaincentric.sample.ecommerce.product.application.createproduct.CreateProductInputPort;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import org.awaitility.Awaitility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * A product staff created, as the catalogue page shows it: created through the use case's input
 * port in the wired application, the page read as a visitor opens it. Its price and stock reach the
 * catalogue by event, so the card is awaited.
 */
// A database of its own: the page is asserted to hold the 21 seeded products and this one.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:created_product_catalogue_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("A created product on the catalogue page")
class CreatedProductOnTheCatalogueIntegrationTest {

  private static final String MUG = "Aggregate Mug";

  @Autowired private MockMvc mockMvc;

  @Autowired private CreateProductInputPort createProduct;

  @BeforeAll
  void staffCreatedTheAggregateMugWithoutAnImage() {
    createProduct.execute(
        new CreateProductCommand(
            "MUG-002",
            MUG,
            "A mug with a consistency boundary.",
            "",
            new BigDecimal("14.99"),
            "EUR",
            "Desk & Office",
            12));
    Awaitility.await()
        .atMost(Duration.ofSeconds(15))
        .until(
            () ->
                cardOf(MUG)
                    .map(
                        card ->
                            !card.select(".product-card__badge--available").isEmpty()
                                && card.selectFirst(".product-card__price")
                                    .text()
                                    .startsWith("14.99"))
                    .orElse(false));
  }

  @Test
  @DisplayName("A created product joins the catalogue")
  void aCreatedProductJoinsTheCatalogueWithItsPriceAndStock() throws Exception {
    final Document page = catalogPage();

    assertThat(page.select("[data-test=product-card]")).hasSize(22);
    final Element card = cardOf(MUG).orElseThrow(() -> new AssertionError("no card " + MUG));
    assertThat(card.selectFirst(".product-card__price").text()).isEqualTo("14.99 EUR");
    assertThat(card.selectFirst(".product-card__badge").text()).isEqualTo("In Stock");
  }

  @Test
  @DisplayName("A card without image shows the initial")
  void aCardWithoutAnImageShowsTheFirstLetterOfTheName() throws Exception {
    final Element card = cardOf(MUG).orElseThrow(() -> new AssertionError("no card " + MUG));

    assertThat(card.select(".product-card__image img")).isEmpty();
    assertThat(card.selectFirst(".product-card__image-placeholder").text()).isEqualTo("A");
  }

  private Optional<Element> cardOf(final String name) throws Exception {
    return catalogPage().select("[data-test=product-card]").stream()
        .filter(card -> name.equals(card.selectFirst("[data-test=product-card-title]").text()))
        .findFirst();
  }

  private Document catalogPage() throws Exception {
    return Jsoup.parse(
        mockMvc
            .perform(get("/products"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());
  }
}
