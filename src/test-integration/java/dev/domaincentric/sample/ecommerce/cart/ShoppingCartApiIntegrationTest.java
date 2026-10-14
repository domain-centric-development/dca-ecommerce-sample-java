package dev.domaincentric.sample.ecommerce.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdQuery;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.Base64;
import java.util.List;
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
 * the cart in the body, with its lines, total and item count — and how they answer a caller who
 * reaches for a cart that is not theirs. Runs against the wired application with a database of its
 * own; every test registers a customer of its own, who has no cart yet.
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:shopping_cart_api_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class ShoppingCartApiIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Autowired private MockMvc mockMvc;

  @Autowired private GetCartByIdInputPort getCartById;

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

  @Test
  @DisplayName("An unknown product is not available")
  void addingAProductTheCatalogueDoesNotKnowIsNotFoundStatedAsAProblem() throws Exception {
    final String token = register("ada-adds-unknown@example.com");
    final String cartId = open(token).get("cartId").asText();

    add(token, cartId, "00000000-0000-4000-8000-000000000001", 1)
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Article not available"))
        .andExpect(
            jsonPath("$.detail")
                .value("There is no article for product 00000000-0000-4000-8000-000000000001."));
  }

  @Test
  @DisplayName("Removing a malformed product id is a bad request")
  void removingAProductIdThatIsNoUuidIsABadRequestAndLeavesTheCartAsItWas() throws Exception {
    final String token = register("ada-removes-malformed@example.com");
    final JsonNode cart = open(token);
    final String cartId = cart.get("cartId").asText();
    final String ddd = productIdOf("Domain-Driven Design");
    add(token, cartId, ddd, 1).andExpect(status().isOk());

    mockMvc
        .perform(
            delete("/api/carts/" + cartId + "/items/no-such-product")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Unacceptable value"))
        .andExpect(jsonPath("$.detail").doesNotExist());

    assertThat(linesOf(cartId, cart.get("customerId").asText())).containsExactly(ddd + " x 1");
  }

  @Test
  @DisplayName("Exactly the stock is accepted")
  void addingExactlyTheStockOfAProductPutsAllOfItIntoTheCart() throws Exception {
    final String token = register("ada-adds-the-stock@example.com");
    final String cartId = open(token).get("cartId").asText();
    final String deskModel = productIdOf("Wooden Hexagon Desk Model");

    add(token, cartId, deskModel, 8)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].productId").value(deskModel))
        .andExpect(jsonPath("$.items[0].quantity").value(8));
  }

  @Test
  @DisplayName("A quantity of zero is refused")
  void addingAQuantityOfZeroIsABadRequestAndLeavesTheCartEmpty() throws Exception {
    final String token = register("ada-adds-zero@example.com");
    final String cartId = open(token).get("cartId").asText();

    add(token, cartId, productIdOf("Domain-Driven Design"), 0).andExpect(status().isBadRequest());

    mockMvc
        .perform(get("/api/carts/" + cartId).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isEmpty());
  }

  @Test
  @DisplayName("Removing a product not in the cart")
  void removingAProductTheCartDoesNotHoldIsNotFoundStatedAsAProblem() throws Exception {
    final String token = register("ada-removes-absent@example.com");
    final String cartId = open(token).get("cartId").asText();
    add(token, cartId, productIdOf("Domain-Driven Design"), 1).andExpect(status().isOk());
    final String teamTopologies = productIdOf("Team Topologies");

    mockMvc
        .perform(
            delete("/api/carts/" + cartId + "/items/" + teamTopologies)
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Position not in cart"))
        .andExpect(
            jsonPath("$.detail").value("The cart holds no position for " + teamTopologies + "."));
  }

  @Test
  @DisplayName("Adding to another customer's cart finds nothing")
  void addingToAnotherCustomersCartAnswersNotFoundAndLeavesItAsItWas() throws Exception {
    final String ada = register("ada-foreign-add@example.com");
    final JsonNode adasCart = open(ada);
    final String cartId = adasCart.get("cartId").asText();
    final String ddd = productIdOf("Domain-Driven Design");
    add(ada, cartId, ddd, 2).andExpect(status().isOk());
    final String grace = register("grace-foreign-add@example.com");

    add(grace, cartId, productIdOf("Team Topologies"), 1).andExpect(status().isNotFound());

    assertThat(linesOf(cartId, adasCart.get("customerId").asText())).containsExactly(ddd + " x 2");
  }

  @Test
  @DisplayName("Removing from another customer's cart finds nothing")
  void removingFromAnotherCustomersCartAnswersNotFoundAndLeavesItAsItWas() throws Exception {
    final String ada = register("ada-foreign-remove@example.com");
    final JsonNode adasCart = open(ada);
    final String cartId = adasCart.get("cartId").asText();
    final String ddd = productIdOf("Domain-Driven Design");
    add(ada, cartId, ddd, 2).andExpect(status().isOk());
    final String grace = register("grace-foreign-remove@example.com");

    mockMvc
        .perform(
            delete("/api/carts/" + cartId + "/items/" + ddd)
                .header("Authorization", "Bearer " + grace))
        .andExpect(status().isNotFound());

    assertThat(linesOf(cartId, adasCart.get("customerId").asText())).containsExactly(ddd + " x 2");
  }

  @Test
  @DisplayName("Another customer's active cart finds nothing")
  void theActiveCartOfAnotherCustomersIdAnswersNotFound() throws Exception {
    final String adasCustomerId =
        open(register("ada-foreign-active@example.com")).get("customerId").asText();
    final String grace = register("grace-foreign-active@example.com");

    mockMvc
        .perform(
            get("/api/carts/customer/" + adasCustomerId + "/active")
                .header("Authorization", "Bearer " + grace))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("A read leaves no cart behind")
  void readingAMissingActiveCartLeavesNothingInTheWayOfOpeningOne() throws Exception {
    final String token = register("grace-reads-none@example.com");
    mockMvc
        .perform(
            get("/api/carts/customer/" + customerIdOf(token) + "/active")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(post("/api/carts").header("Authorization", "Bearer " + token))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.items").isEmpty())
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.itemCount").value(0));
  }

  @Test
  @DisplayName("Tokenless calls are strangers to each other")
  void eachCallWithoutATokenOpensACartForANewGuest() throws Exception {
    final JsonNode first = openWithoutToken();
    final JsonNode second = openWithoutToken();

    assertThat(second.get("customerId").asText()).isNotEqualTo(first.get("customerId").asText());
  }

  private JsonNode openWithoutToken() throws Exception {
    final String body =
        mockMvc
            .perform(post("/api/carts"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JSON.readTree(body);
  }

  /** The cart's lines as its owner reads them through the input port: "productId x quantity". */
  private List<String> linesOf(final String cartId, final String customerId) {
    return getCartById
        .execute(new GetCartByIdQuery(cartId, customerId))
        .cart()
        .orElseThrow()
        .items()
        .stream()
        .map(item -> item.productId().value() + " x " + item.quantity().value())
        .toList();
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

  /** The customer id a token carries as its subject — the id the cart routes name. */
  private static String customerIdOf(final String token) throws Exception {
    return JSON.readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1])).get("sub").asText();
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
