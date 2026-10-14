package dev.domaincentric.sample.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsQuery;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Role;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * A price or an initial stock of a new product the shop refuses over the REST API — nothing, a
 * fraction of a cent that rounds to nothing, more than the highest amount, less than no stock — and
 * how the wired application says so: a bad request, as a problem document where the refusal comes
 * from the domain.
 */
// A database of its own: the catalogue is asserted to hold exactly the 21 seeded products.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:product_price_stock_refusal_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("A product's price and stock are refused")
class ProductPriceAndStockRefusalIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Autowired private MockMvc mockMvc;

  @Autowired private TokenService tokenService;

  @Autowired private GetAllProductsInputPort getAllProducts;

  @Test
  @DisplayName("A price of zero is refused")
  void aPriceOfZeroIsABadRequestAndNothingIsCreated() throws Exception {
    final MvcResult result =
        create(
            """
            {"sku":"FREE-001","name":"Free Mug","price":0,"category":"Desk & Office","stock":5}""");

    assertThat(result.getResponse().getStatus()).isEqualTo(400);
    assertThat(getAllProducts.execute(new GetAllProductsQuery()).products()).hasSize(21);
  }

  @Test
  @DisplayName("A price that rounds to zero is refused")
  void aPriceThatRoundsToZeroIsAnUnacceptableValueWithoutDetail() throws Exception {
    final MvcResult result =
        create(
            """
            {"sku":"TINY-001","name":"Tiny Mug","price":0.001,"category":"Desk & Office",\
            "stock":5}""");

    assertUnacceptableValueWithoutDetail(result);
  }

  @Test
  @DisplayName("A price above the highest is refused")
  void aPriceAboveTheHighestAmountIsAnUnacceptableValueWithoutDetail() throws Exception {
    final MvcResult result =
        create(
            """
            {"sku":"MAX-002","name":"Too Precious Model","price":1000000000000.00,\
            "category":"Modeling","stock":1}""");

    assertUnacceptableValueWithoutDetail(result);
  }

  @Test
  @DisplayName("A negative initial stock is refused")
  void aNegativeInitialStockIsABadRequestAndNothingIsCreated() throws Exception {
    final MvcResult result =
        create(
            """
            {"sku":"PIN-003","name":"Owed Pin","price":12.99,"category":"Stickers & Pins",\
            "stock":-1}""");

    assertThat(result.getResponse().getStatus()).isEqualTo(400);
    assertThat(getAllProducts.execute(new GetAllProductsQuery()).products()).hasSize(21);
  }

  private static void assertUnacceptableValueWithoutDetail(final MvcResult result)
      throws Exception {
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
    assertThat(result.getResponse().getContentType()).startsWith("application/problem+json");
    final JsonNode problem = JSON.readTree(result.getResponse().getContentAsString());
    assertThat(problem.path("title").asText()).isEqualTo("Unacceptable value");
    assertThat(problem.has("detail")).as("the problem carries a detail: %s", problem).isFalse();
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
        Set.of(Role.CUSTOMER, Role.STAFF));
  }
}
