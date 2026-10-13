package dev.domaincentric.sample.ecommerce.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * How the registration endpoint of the JSON API answers a refusal: a problem document whose status
 * names the kind of failure, whose title names the failure and whose detail is the adapter's own
 * sentence. Runs against the wired application with a database of its own.
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:registration_api_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class RegistrationApiIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("An address already registered is a conflict")
  void anAddressAnotherAccountHoldsIsAConflictStatedAsAProblem() throws Exception {
    tokenOf("ada@example.com");

    mockMvc
        .perform(registration("ada@example.com", "Secret123", "Grace", "Hopper", "1906-12-09"))
        .andExpect(status().isConflict())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Email already registered"))
        .andExpect(
            jsonPath("$.detail").value("The email address ada@example.com is already registered."));
  }

  @Test
  @DisplayName("A weak password is refused")
  void aPasswordWithoutAnUppercaseLetterIsRefusedNamingTheRule() throws Exception {
    mockMvc
        .perform(registration("grace@example.com", "secret123", "Grace", "Hopper", "1906-12-09"))
        .andExpect(status().is(422))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Password too weak"))
        .andExpect(jsonPath("$.detail").value("Password must contain an uppercase letter."));
  }

  @Test
  @DisplayName("A malformed address is refused")
  void aMalformedAddressIsABadRequestWithoutDetail() throws Exception {
    mockMvc
        .perform(registration("grace@example", "Secret123", "Grace", "Hopper", "1906-12-09"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Unacceptable value"))
        .andExpect(jsonPath("$.detail").doesNotExist());
  }

  @Test
  @DisplayName("A caller with an account cannot register another")
  void aCallerHoldingTheTokenOfAnAccountCannotRegisterASecondOne() throws Exception {
    final String token = tokenOf("ada@example.com");

    mockMvc
        .perform(
            registration("grace@example.com", "Secret123", "Grace", "Hopper", "1906-12-09")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isConflict())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Account already exists"))
        .andExpect(jsonPath("$.detail").value("The signed-in user already has an account."));
  }

  @Test
  @DisplayName("Registering through the API hands out no cookie")
  void aRegistrationWithoutCookiesIsAnsweredWithoutSettingOne() throws Exception {
    // An address of its own: the other tests of this class register "ada@example.com" already.
    final MockHttpServletResponse response =
        mockMvc
            .perform(
                registration(
                    "ada.lovelace@example.com", "Secret123", "Ada", "Lovelace", "1815-12-10"))
            .andReturn()
            .getResponse();

    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).isEmpty();
    assertThat(response.getCookies()).isEmpty();
  }

  /**
   * The token of the account for this address: registered through the API, or — where another test
   * of this class registered it already — logged in through the API.
   */
  private String tokenOf(final String email) throws Exception {
    MockHttpServletResponse response =
        mockMvc
            .perform(registration(email, "Secret123", "Ada", "Lovelace", "1815-12-10"))
            .andReturn()
            .getResponse();
    if (response.getStatus() != 201) {
      response =
          mockMvc
              .perform(
                  post("/api/auth/login")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(
                          """
                          {"email":"%s","password":"Secret123"}"""
                              .formatted(email)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse();
    }
    return JSON.readTree(response.getContentAsString()).get("token").asText();
  }

  private static MockHttpServletRequestBuilder registration(
      final String email,
      final String password,
      final String firstName,
      final String lastName,
      final String dateOfBirth) {
    return post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            """
            {"email":"%s","password":"%s","firstName":"%s","lastName":"%s","dateOfBirth":"%s"}"""
                .formatted(email, password, firstName, lastName, dateOfBirth));
  }
}
