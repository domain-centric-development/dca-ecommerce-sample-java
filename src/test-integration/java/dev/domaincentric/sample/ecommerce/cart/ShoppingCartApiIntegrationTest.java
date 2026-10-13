package dev.domaincentric.sample.ecommerce.cart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * How the cart routes of the JSON API answer a customer who opens, reads and fills their own cart:
 * the cart in the body, with its lines, total and item count. Runs against the wired application
 * with a database of its own; every test registers a customer of its own, who has no cart yet.
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:shopping_cart_api_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class ShoppingCartApiIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Opening a cart for the caller")
  void openingACartAnswersCreatedWithAnEmptyActiveCart() throws Exception {
    final String token = register("ada-opens@example.com");

    mockMvc
        .perform(post("/api/carts").header("Authorization", "Bearer " + token))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.cartId").isNotEmpty())
        .andExpect(jsonPath("$.customerId").isNotEmpty())
        .andExpect(jsonPath("$.items").isEmpty())
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.itemCount").value(0));
  }

  @Test
  @DisplayName("A second open cart is a conflict")
  void openingASecondCartIsAConflictStatedAsAProblem() throws Exception {
    final String token = register("ada-opens-twice@example.com");
    open(token);

    mockMvc
        .perform(post("/api/carts").header("Authorization", "Bearer " + token))
        .andExpect(status().isConflict())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Active cart already exists"));
  }

  @Test
  @DisplayName("The caller's active cart by customer id")
  void theActiveCartOfTheCallersCustomerIdIsTheCartTheyOpened() throws Exception {
    final String token = register("ada-active@example.com");
    final JsonNode cart = open(token);

    mockMvc
        .perform(
            get("/api/carts/customer/" + cart.get("customerId").asText() + "/active")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cartId").value(cart.get("cartId").asText()))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  @DisplayName("Adding the same product again adds up")
  void addingAProductTheCartHoldsRaisesItsLinesQuantity() throws Exception {
    final String token = register("ada-adds-again@example.com");
    final String cartId = open(token).get("cartId").asText();
    final String ddd = productIdOf("Domain-Driven Design");
    add(token, cartId, ddd, 2).andExpect(status().isOk());

    add(token, cartId, ddd, 1)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].productId").value(ddd))
        .andExpect(jsonPath("$.items[0].quantity").value(3))
        .andExpect(jsonPath("$.total").value(164.97));
  }

  @Test
  @DisplayName("Reading the own cart")
  void readingTheOwnCartAnswersItsLinesTotalAndItemCount() throws Exception {
    final String token = register("ada-reads@example.com");
    final String cartId = open(token).get("cartId").asText();
    add(token, cartId, productIdOf("Domain-Driven Design"), 2).andExpect(status().isOk());
    add(token, cartId, productIdOf("Team Topologies"), 1).andExpect(status().isOk());

    mockMvc
        .perform(get("/api/carts/" + cartId).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(2))
        .andExpect(jsonPath("$.total").value(139.97))
        .andExpect(jsonPath("$.currency").value("EUR"))
        .andExpect(jsonPath("$.itemCount").value(2));
  }

  @Test
  @DisplayName("Removing a product removes its line")
  void removingAProductTakesItsWholeLineOut() throws Exception {
    final String token = register("ada-removes@example.com");
    final String cartId = open(token).get("cartId").asText();
    final String ddd = productIdOf("Domain-Driven Design");
    final String teamTopologies = productIdOf("Team Topologies");
    add(token, cartId, ddd, 3).andExpect(status().isOk());
    add(token, cartId, teamTopologies, 1).andExpect(status().isOk());

    mockMvc
        .perform(
            delete("/api/carts/" + cartId + "/items/" + ddd)
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].productId").value(teamTopologies))
        .andExpect(jsonPath("$.items[0].quantity").value(1))
        .andExpect(jsonPath("$.itemCount").value(1));
  }

  private ResultActions add(
      final String token, final String cartId, final String productId, final int quantity)
      throws Exception {
    return mockMvc.perform(
        post("/api/carts/" + cartId + "/items")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {"productId":"%s","quantity":%d}"""
                    .formatted(productId, quantity)));
  }

  /** Opens a cart through the API and returns its body. */
  private JsonNode open(final String token) throws Exception {
    final String body =
        mockMvc
            .perform(post("/api/carts").header("Authorization", "Bearer " + token))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JSON.readTree(body);
  }

  /** The id of the catalogue's product with this name, as the API lists it. */
  private String productIdOf(final String name) throws Exception {
    final String body =
        mockMvc
            .perform(get("/api/products"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    for (final JsonNode product : JSON.readTree(body)) {
      if (name.equals(product.get("name").asText())) {
        return product.get("id").asText();
      }
    }
    throw new AssertionError("no product named " + name);
  }

  /** Registers an account through the API and returns its token. */
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
    return JSON.readTree(body).get("token").asText();
  }
}
