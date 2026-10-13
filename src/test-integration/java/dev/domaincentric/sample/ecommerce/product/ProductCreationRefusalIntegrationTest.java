package dev.domaincentric.sample.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Who may not create a product over the REST API, and how the wired application says so: a stranger
 * is challenged, a customer is forbidden, each as a problem document.
 */
// A database of its own: the catalogue is asserted to hold exactly the 21 seeded products.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:product_creation_refusal_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Creating a product is refused")
class ProductCreationRefusalIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  private static final String VALID_PRODUCT =
      """
      {"sku":"MUG-002","name":"Aggregate Mug","description":"A mug with a consistency boundary.",\
      "imageUrl":"","price":14.99,"category":"Desk & Office","stock":12}""";

  @Autowired private MockMvc mockMvc;

  @Autowired private GetAllProductsInputPort getAllProducts;

  @Test
  @DisplayName("A stranger is challenged")
  void aClientWithoutATokenIsChallengedAndNothingIsCreated() throws Exception {
    final MvcResult result =
        mockMvc
            .perform(
                post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(VALID_PRODUCT))
            .andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(401);
    assertThat(result.getResponse().getHeader(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");
    assertThat(result.getResponse().getContentType()).startsWith("application/problem+json");
    assertThat(JSON.readTree(result.getResponse().getContentAsString()).path("title").asText())
        .isEqualTo("Unauthorized");
    assertThat(getAllProducts.execute(new GetAllProductsQuery()).products()).hasSize(21);
  }

  @Test
  @DisplayName("A customer is forbidden")
  void aCustomersTokenIsForbidden() throws Exception {
    final String customer = register("ada@example.com");

    final MvcResult result =
        mockMvc
            .perform(
                post("/api/products")
                    .header("Authorization", "Bearer " + customer)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(VALID_PRODUCT))
            .andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(403);
    assertThat(result.getResponse().getContentType()).startsWith("application/problem+json");
    assertThat(JSON.readTree(result.getResponse().getContentAsString()).path("title").asText())
        .isEqualTo("Forbidden");
  }

  /** Registers the customer account through the API and returns its token. */
  private String register(final String email) throws Exception {
    final String body =
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"email":"%s","password":"Secret123","firstName":"Ada",\
                        "lastName":"Lovelace","dateOfBirth":"1815-12-10"}"""
                            .formatted(email)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    final JsonNode token = JSON.readTree(body).path("token");
    return token.asText();
  }
}
