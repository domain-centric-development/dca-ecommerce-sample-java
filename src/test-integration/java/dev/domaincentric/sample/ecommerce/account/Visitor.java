package dev.domaincentric.sample.ecommerce.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.util.HtmlUtils;

/**
 * One browser against the wired application: it keeps the cookies the responses set and sends them
 * along, and fills the shop's forms with the {@code _csrf} and {@code returnUrl} the page carries.
 */
final class Visitor {

  private static final Pattern CSRF_FIELD = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");
  private static final Pattern RETURN_URL_FIELD =
      Pattern.compile("name=\"returnUrl\" value=\"([^\"]*)\"");

  private final MockMvc mockMvc;
  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  Visitor(final MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  /** Registers the account on the registration page; the shop signs this visitor in. */
  void register(final String email, final String password) throws Exception {
    final String form = page("/register");
    keep(
        mockMvc
            .perform(
                withCookies(post("/register"))
                    .param("_csrf", text(CSRF_FIELD, form))
                    .param("firstName", "Anna")
                    .param("lastName", "Schmidt")
                    .param("dateOfBirth", "1990-05-17")
                    .param("email", email)
                    .param("password", password)
                    .param("confirmPassword", password))
            .andExpect(status().is3xxRedirection())
            .andReturn()
            .getResponse());
  }

  /**
   * Submits the login form of the given page with the credentials; returns where the shop sends the
   * visitor after the login, the cart's merge page followed as a browser would.
   */
  String logIn(final String loginPage, final String email, final String password) throws Exception {
    final String form = page(loginPage);
    final var request =
        withCookies(post("/login"))
            .param("_csrf", text(CSRF_FIELD, form))
            .param("email", email)
            .param("password", password);
    final Matcher returnUrl = RETURN_URL_FIELD.matcher(form);
    if (returnUrl.find()) {
      request.param("returnUrl", HtmlUtils.htmlUnescape(returnUrl.group(1)));
    }
    final MockHttpServletResponse loggedIn =
        mockMvc.perform(request).andExpect(status().is3xxRedirection()).andReturn().getResponse();
    keep(loggedIn);
    return redirectOf(loggedIn.getRedirectedUrl());
  }

  /**
   * Submits the login form of the given page with credentials the shop refuses; returns the page it
   * shows again.
   */
  String refusedLogIn(final String loginPage, final String email, final String password)
      throws Exception {
    final String form = page(loginPage);
    final MockHttpServletResponse refused =
        mockMvc
            .perform(
                withCookies(post("/login"))
                    .param("_csrf", text(CSRF_FIELD, form))
                    .param("email", email)
                    .param("password", password))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    keep(refused);
    return refused.getContentAsString();
  }

  /** Keeps a cookie this browser holds from an earlier visit, and sends it from now on. */
  void holds(final Cookie cookie) {
    cookies.put(cookie.getName(), cookie);
  }

  /** Opens a path that answers with a redirect; returns where it leads. */
  String redirectOf(final String path) throws Exception {
    final MockHttpServletResponse response =
        mockMvc
            .perform(withCookies(get(URI.create(path))))
            .andExpect(status().is3xxRedirection())
            .andReturn()
            .getResponse();
    keep(response);
    return response.getRedirectedUrl();
  }

  /** A page as the browser gets it. */
  String page(final String path) throws Exception {
    final MockHttpServletResponse response =
        mockMvc
            .perform(withCookies(get(URI.create(path))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    keep(response);
    return response.getContentAsString();
  }

  /** Sends the cookies collected so far, as the browser would; a fresh visitor sends none. */
  private MockHttpServletRequestBuilder withCookies(final MockHttpServletRequestBuilder request) {
    return cookies.isEmpty() ? request : request.cookie(cookies.values().toArray(Cookie[]::new));
  }

  private void keep(final MockHttpServletResponse response) {
    for (final Cookie cookie : response.getCookies()) {
      if (cookie.getMaxAge() == 0) {
        cookies.remove(cookie.getName());
      } else {
        cookies.put(cookie.getName(), cookie);
      }
    }
  }

  static String text(final Pattern pattern, final String html) {
    final Matcher matcher = pattern.matcher(html);
    assertThat(matcher.find()).as("%s in the rendered page", pattern).isTrue();
    return HtmlUtils.htmlUnescape(matcher.group(1)).strip();
  }

  /** The first element of the page with the given {@code data-test}: its tag, attributes, text. */
  static Element element(final String page, final String dataTest) {
    final Matcher matcher =
        Pattern.compile(
                "<(\\w+)([^>]*\\bdata-test=\"" + Pattern.quote(dataTest) + "\"[^>]*)>([^<]*)")
            .matcher(page);
    assertThat(matcher.find()).as("element %s in the rendered page", dataTest).isTrue();
    return new Element(
        matcher.group(1), matcher.group(2), HtmlUtils.htmlUnescape(matcher.group(3)).strip());
  }

  /** One rendered element: its tag, its attributes as written and its text. */
  record Element(String tag, String attributes, String text) {

    boolean has(final String name) {
      return Pattern.compile("\\b" + Pattern.quote(name) + "=\"").matcher(attributes).find();
    }

    String attribute(final String name) {
      final Matcher matcher =
          Pattern.compile("\\b" + Pattern.quote(name) + "=\"([^\"]*)\"").matcher(attributes);
      assertThat(matcher.find()).as("attribute %s on %s", name, attributes).isTrue();
      return HtmlUtils.htmlUnescape(matcher.group(1));
    }
  }
}
