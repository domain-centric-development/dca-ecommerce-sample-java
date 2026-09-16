package dev.domaincentric.sample.ecommerce.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.account.application.shared.TokenService;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.util.Base64;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Who may reach what over the REST API. The JWT filter gives <i>every</i> request an authentication
 * — an anonymous identity is still an authentication — so {@code anyRequest().authenticated()} in
 * the security configuration guards nothing here; the resources do, and this is where that is held
 * to.
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:api_authz_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class ApiAuthorizationIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Autowired private MockMvc mockMvc;

  @Autowired private TokenService tokenService;

  @Test
  @DisplayName("Reading the catalog is public, creating a product needs the staff role")
  void catalogIsPublicButCreatingNeedsStaff() throws Exception {
    mockMvc.perform(get("/api/products")).andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/products").contentType(MediaType.APPLICATION_JSON).content(newProduct()))
        .andExpect(status().isForbidden());

    final String customer = register("api-catalog-customer@example.com");
    mockMvc
        .perform(
            post("/api/products")
                .header("Authorization", "Bearer " + customer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newProduct()))
        .andExpect(status().isForbidden());

    final String staff = staffTokenFor(register("api-catalog-staff@example.com"));
    mockMvc
        .perform(
            post("/api/products")
                .header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newProduct()))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("Listing every cart needs the staff role")
  void listingEveryCartNeedsStaff() throws Exception {
    mockMvc.perform(get("/api/carts")).andExpect(status().isForbidden());

    final String customer = register("api-cart-lister@example.com");
    mockMvc
        .perform(get("/api/carts").header("Authorization", "Bearer " + customer))
        .andExpect(status().isForbidden());

    final String staff = staffTokenFor(register("api-cart-operator@example.com"));
    mockMvc
        .perform(get("/api/carts").header("Authorization", "Bearer " + staff))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("A cart route only ever answers with the caller's own cart")
  void aCartRouteOnlyAnswersWithTheCallersOwnCart() throws Exception {
    final String mine = register("api-cart-owner@example.com");
    final String theirs = register("api-cart-stranger@example.com");

    final String created =
        mockMvc
            .perform(post("/api/carts").header("Authorization", "Bearer " + mine))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    final String cartId = JSON.readTree(created).get("cartId").asText();

    mockMvc
        .perform(get("/api/carts/" + cartId).header("Authorization", "Bearer " + mine))
        .andExpect(status().isOk());

    // Not 403: telling a stranger they are forbidden confirms the id exists.
    mockMvc
        .perform(get("/api/carts/" + cartId).header("Authorization", "Bearer " + theirs))
        .andExpect(status().isNotFound());
    mockMvc
        .perform(
            post("/api/carts/" + cartId + "/checkout").header("Authorization", "Bearer " + theirs))
        .andExpect(status().isNotFound());
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

  /**
   * A staff token for an account that already exists. No registration path hands the role out, so
   * the test mints the token the way an operator provisioning tool would.
   */
  private String staffTokenFor(final String customerToken) throws Exception {
    final JsonNode claims = claimsOf(customerToken);
    return tokenService.generateRegisteredToken(
        UserId.of(claims.get("sub").asText()),
        claims.get("email").asText(),
        Set.of(IdentityProvider.Identity.ROLE_CUSTOMER, IdentityProvider.Identity.ROLE_STAFF));
  }

  private static JsonNode claimsOf(final String jwt) throws Exception {
    return JSON.readTree(Base64.getUrlDecoder().decode(jwt.split("\\.")[1]));
  }

  private static String newProduct() {
    return """
        {"sku":"API-%s","name":"API Product","description":"d","imageUrl":"",\
        "price":19.99,"category":"Books","stock":5}"""
        .formatted(Long.toHexString(System.nanoTime()).toUpperCase(java.util.Locale.ROOT));
  }
}
