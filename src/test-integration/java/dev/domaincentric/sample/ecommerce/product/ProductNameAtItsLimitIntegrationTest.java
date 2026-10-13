package dev.domaincentric.sample.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
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
import org.springframework.test.web.servlet.MvcResult;

/** A product whose name is exactly as long as a name may be is created over the REST API. */
// A database of its own: the product created here must not reach a catalogue other tests count.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:product_name_limit_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("A product name at its limit")
class ProductNameAtItsLimitIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Autowired private MockMvc mockMvc;

  @Autowired private TokenService tokenService;

  @Test
  @DisplayName("A name of 255 characters is accepted")
  void aNameOf255CharactersIsAccepted() throws Exception {
    final MvcResult result =
        mockMvc
            .perform(
                post("/api/products")
                    .header("Authorization", "Bearer " + staffToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"sku":"LONG-002","name":"%s","price":9.99,"category":"Books","stock":1}"""
                            .formatted("A".repeat(255))))
            .andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(201);
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
                        {"email":"name-limit-staff@example.com","password":"Secret123",\
                        "firstName":"Ada","lastName":"Lovelace","dateOfBirth":"1815-12-10"}"""))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    final String token = JSON.readTree(body).path("token").asText();
    final JsonNode claims = JSON.readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
    return tokenService.generateRegisteredToken(
        UserId.of(claims.path("sub").asText()),
        claims.path("email").asText(),
        Set.of(IdentityProvider.Identity.ROLE_CUSTOMER, IdentityProvider.Identity.ROLE_STAFF));
  }
}
