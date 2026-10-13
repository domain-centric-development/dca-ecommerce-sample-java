package dev.domaincentric.sample.ecommerce.account;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * How the login and logout endpoints of the JSON API answer: a refused password is a 401, and
 * logging out — the client discarding its token — is acknowledged without content. Runs against the
 * wired application with a database of its own.
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:login_api_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class LoginApiIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("A wrong password is refused")
  void aLoginWithTheWrongPasswordIsUnauthorized() throws Exception {
    tokenOf("ada@example.com");

    mockMvc.perform(login("ada@example.com", "Wrong1234")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Logging out answers no content")
  void loggingOutWithATokenAnswersNoContent() throws Exception {
    final String token = tokenOf("ada@example.com");

    mockMvc
        .perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));
  }

  /**
   * The token of the account for this address with the password "Secret123": registered through the
   * API, or — where another test of this class registered it already — logged in through the API.
   */
  private String tokenOf(final String email) throws Exception {
    MockHttpServletResponse response =
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"email":"%s","password":"Secret123","firstName":"Ada",\
                        "lastName":"Lovelace","dateOfBirth":"1815-12-10"}"""
                            .formatted(email)))
            .andReturn()
            .getResponse();
    if (response.getStatus() != 201) {
      response = mockMvc.perform(login(email, "Secret123")).andReturn().getResponse();
    }
    return JSON.readTree(response.getContentAsString()).get("token").asText();
  }

  private static MockHttpServletRequestBuilder login(final String email, final String password) {
    return post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            """
            {"email":"%s","password":"%s"}"""
                .formatted(email, password));
  }
}
