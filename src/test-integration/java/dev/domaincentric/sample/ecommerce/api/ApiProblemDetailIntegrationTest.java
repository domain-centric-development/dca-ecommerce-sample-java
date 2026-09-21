package dev.domaincentric.sample.ecommerce.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * How a refused request reads over the API.
 *
 * <p>Every refusal is a problem document (RFC 9457): the status carries the kind of failure, the
 * title names it, the detail is the wording of the layer that refused. What is being checked here
 * is that the status follows the failure and not the base type — a stock keeping unit the catalog
 * already holds is a conflict, a malformed one is a bad request, and both come out of the same
 * endpoint that used to answer {@code 400} with an empty body for either (ADR-037).
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:api_problem_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class ApiProblemDetailIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Autowired private MockMvc mockMvc;

  @Autowired private TokenService tokenService;

  @Test
  @DisplayName("A stock keeping unit the catalog already holds is a conflict, stated as a problem")
  void aDuplicateStockKeepingUnitIsAConflict() throws Exception {
    final String staff = staffTokenFor(register("api-problem-staff@example.com"));
    final String sku = "API-DUP-" + Long.toHexString(System.nanoTime()).toUpperCase(Locale.ROOT);

    mockMvc
        .perform(
            post("/api/products")
                .header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productWith(sku)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/products")
                .header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productWith(sku)))
        .andExpect(status().isConflict())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.title").value("Stock keeping unit already in use"))
        .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString(sku)));
  }

  @Test
  @DisplayName("A value the model refuses is a bad request, stated as a problem")
  void anUnacceptableValueIsABadRequest() throws Exception {
    final String staff = staffTokenFor(register("api-problem-staff-2@example.com"));

    mockMvc
        .perform(
            post("/api/products")
                .header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productWith("lower case sku")))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Unacceptable value"));
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

  private static String productWith(final String sku) {
    return """
        {"sku":"%s","name":"API Product","description":"d","imageUrl":"",\
        "price":19.99,"category":"Books","stock":5}"""
        .formatted(sku);
  }
}
