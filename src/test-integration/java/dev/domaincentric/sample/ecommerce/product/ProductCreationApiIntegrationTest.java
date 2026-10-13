package dev.domaincentric.sample.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.product.application.createproduct.CreateProductCommand;
import dev.domaincentric.sample.ecommerce.product.application.createproduct.CreateProductInputPort;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;
import org.awaitility.Awaitility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * What staff leave out when they create a product, and what a visitor then sees: the request read
 * by the REST resource of the wired application, the product page rendered from what was stored.
 */
// A database of its own: the products created here never reach a test that counts the seeded range.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:product_creation_api_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Creating a product")
class ProductCreationApiIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Autowired private MockMvc mockMvc;

  @Autowired private TokenService tokenService;

  @Autowired private CreateProductInputPort createProduct;

  @Test
  @DisplayName("Description and image may be left out")
  void aProductCreatedWithoutDescriptionAndImageCarriesEmptyOnes() throws Exception {
    final MvcResult result =
        mockMvc
            .perform(
                post("/api/products")
                    .header("Authorization", "Bearer " + staffToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"sku":"TOTE-001","name":"Bounded Context Tote","price":19.99,\
                        "category":"Apparel","stock":10}"""))
            .andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(201);
    final JsonNode product = JSON.readTree(result.getResponse().getContentAsString());
    assertThat(product.has("description")).as("the body names a description").isTrue();
    assertThat(product.path("description").asText()).isEmpty();
    assertThat(product.has("imageUrl")).as("the body names an image").isTrue();
    assertThat(product.path("imageUrl").asText()).isEmpty();
  }

  @Test
  @DisplayName("A product page without description")
  void aProductPageWithoutDescriptionSaysThatNoneIsAvailable() throws Exception {
    final String id =
        createProduct
            .execute(
                new CreateProductCommand(
                    "TOTE-002",
                    "Bounded Context Tote",
                    "",
                    "",
                    new BigDecimal("19.99"),
                    "EUR",
                    "Apparel",
                    10))
            .productId();
    Awaitility.await()
        .atMost(Duration.ofSeconds(15))
        .until(
            () ->
                mockMvc.perform(get("/products/" + id)).andReturn().getResponse().getStatus()
                    == 200);

    final Document page =
        Jsoup.parse(
            mockMvc
                .perform(get("/products/" + id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

    assertThat(page.selectFirst(".product-detail__description p").text())
        .isEqualTo("No description available.");
  }

  /**
   * A staff token for an account registered through the API. No registration path hands the role
   * out, so the test mints the token the way an operator provisioning tool would.
   */
  private String staffToken() throws Exception {
    final String body =
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"email":"api-tote-staff@example.com","password":"Secret123",\
                        "firstName":"Ada","lastName":"Lovelace","dateOfBirth":"1815-12-10"}"""))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    final JsonNode claims =
        JSON.readTree(
            Base64.getUrlDecoder()
                .decode(JSON.readTree(body).path("token").asText().split("\\.")[1]));
    return tokenService.generateRegisteredToken(
        UserId.of(claims.path("sub").asText()),
        claims.path("email").asText(),
        Set.of(IdentityProvider.Identity.ROLE_CUSTOMER, IdentityProvider.Identity.ROLE_STAFF));
  }
}
