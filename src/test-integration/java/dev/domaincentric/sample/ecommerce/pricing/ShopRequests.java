package dev.domaincentric.sample.ecommerce.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Role;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.HtmlUtils;

/** What the pricing tests ask of the wired shop: a product's id, its page's price, tokens. */
final class ShopRequests {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final Pattern PRICE =
      Pattern.compile("data-test=\"product-detail-price\">([^<]*)<");

  private final MockMvc mockMvc;
  private final TokenService tokenService;

  ShopRequests(final MockMvc mockMvc, final TokenService tokenService) {
    this.mockMvc = mockMvc;
    this.tokenService = tokenService;
  }

  /** Sends {@code PUT /api/products/<id>/price} with the body, under the token if one is given. */
  MvcResult changePrice(final String productId, final String body, final String token)
      throws Exception {
    final var request =
        put("/api/products/" + productId + "/price")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body);
    if (token != null) {
      request.header("Authorization", "Bearer " + token);
    }
    return mockMvc.perform(request).andReturn();
  }

  /** The price the product page shows. */
  String shownPriceOf(final String productId) throws Exception {
    final String page =
        mockMvc
            .perform(get("/products/" + productId))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    final Matcher price = PRICE.matcher(page);
    assertThat(price.find()).as("the price on the product page").isTrue();
    return HtmlUtils.htmlUnescape(price.group(1)).strip();
  }

  /** The id of the catalogue's product with this name, as the API lists it. */
  String productIdOf(final String name) throws Exception {
    final String body =
        mockMvc
            .perform(get("/api/products"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    for (final JsonNode product : JSON.readTree(body)) {
      if (name.equals(product.path("name").asText())) {
        return product.path("id").asText();
      }
    }
    throw new AssertionError("no product named " + name);
  }

  /** Registers an account through the API and returns its customer token. */
  String customerToken() throws Exception {
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
    return JSON.readTree(body).path("token").asText();
  }

  /**
   * A staff token for an account registered through the API, minted by the shop's own token
   * service: no registration hands out the staff role.
   */
  String staffToken() throws Exception {
    final String token = customerToken();
    final JsonNode claims = JSON.readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
    return tokenService.generateRegisteredToken(
        UserId.of(claims.path("sub").asText()),
        claims.path("email").asText(),
        Set.of(Role.CUSTOMER, Role.STAFF));
  }
}
