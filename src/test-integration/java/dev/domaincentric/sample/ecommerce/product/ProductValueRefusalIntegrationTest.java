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
 * A value of a new product the shop refuses over the REST API — a malformed stock keeping unit, a
 * blank or overlong name, an overlong description or image URL — and how the wired application says
 * so: a bad request, as a problem document whose title names the refusal.
 */
// A database of its own: the catalogue is asserted to hold exactly the 21 seeded products.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:product_value_refusal_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("A product's values are refused")
class ProductValueRefusalIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Autowired private MockMvc mockMvc;

  @Autowired private TokenService tokenService;

  @Autowired private GetAllProductsInputPort getAllProducts;

  @Test
  @DisplayName("A lower-case SKU is refused")
  void aLowerCaseSkuIsAnUnacceptableValueWithoutDetail() throws Exception {
    final MvcResult result =
        create(
            """
            {"sku":"mug-002","name":"Aggregate Mug","price":14.99,\
            "category":"Desk & Office","stock":12}""");

    assertUnacceptableValueWithoutDetail(result);
  }

  @Test
  @DisplayName("An overlong name is refused")
  void aNameOfMoreThan255CharactersIsAnUnacceptableValueWithoutDetail() throws Exception {
    final MvcResult result =
        create(
            """
            {"sku":"LONG-001","name":"%s","price":9.99,"category":"Books","stock":1}"""
                .formatted("A".repeat(256)));

    assertUnacceptableValueWithoutDetail(result);
  }

  @Test
  @DisplayName("A blank name is refused")
  void aBlankNameIsABadRequestAndNothingIsCreated() throws Exception {
    final MvcResult result =
        create(
            """
            {"sku":"BLANK-001","name":"   ","price":9.99,"category":"Books","stock":1}""");

    assertThat(result.getResponse().getStatus()).isEqualTo(400);
    assertThat(getAllProducts.execute(new GetAllProductsQuery()).products()).hasSize(21);
  }

  @Test
  @DisplayName("An overlong description is refused")
  void aDescriptionOfMoreThan2000CharactersIsAnUnacceptableValueWithoutDetail() throws Exception {
    final MvcResult result =
        create(
            """
            {"sku":"DESC-001","name":"Long Story","description":"%s","price":9.99,\
            "category":"Books","stock":1}"""
                .formatted("A".repeat(2001)));

    assertUnacceptableValueWithoutDetail(result);
  }

  @Test
  @DisplayName("An overlong image URL is refused")
  void anImageUrlOfMoreThan2000CharactersIsAnUnacceptableValueWithoutDetail() throws Exception {
    final String imageUrl =
        "https://example.com/" + "a".repeat(2001 - "https://example.com/".length());

    final MvcResult result =
        create(
            """
            {"sku":"IMG-001","name":"Big Picture","imageUrl":"%s","price":9.99,\
            "category":"Books","stock":1}"""
                .formatted(imageUrl));

    assertUnacceptableValueWithoutDetail(result);
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
