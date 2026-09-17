package dev.domaincentric.sample.ecommerce.account.infrastructure;

import dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security.JwtAuthenticationFilter;
import dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security.JwtProperties;
import dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security.ShopAccessDeniedHandler;
import dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security.ShopAuthenticationEntryPoint;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Security configuration for the application.
 *
 * <p>This configuration sets up JWT-based authentication with the following features:
 *
 * <ul>
 *   <li>Stateless session management (no HTTP sessions)
 *   <li>JWT filter for extracting/creating identity tokens
 *   <li>BCrypt password encoding for account passwords
 *   <li>CSRF protection for browser forms via a cookie-backed token (stateless, {@code XSRF-TOKEN}
 *       cookie + {@code _csrf} form field). {@code /api/**} and {@code /mcp/**} are exempt because
 *       {@link JwtAuthenticationFilter} authenticates them by Bearer header only — cookies are
 *       never read there, so a cross-site request cannot borrow the browser's session (ADR-035).
 *       The H2 console is exempt as a development tool. {@code SameSite=Lax} on the identity cookie
 *       is defence in depth, not the CSRF strategy.
 * </ul>
 *
 * <p><b>Authentication Flow:</b>
 *
 * <ol>
 *   <li>Request arrives at server
 *   <li>JwtAuthenticationFilter extracts/creates JWT from cookie
 *   <li>Identity is placed in SecurityContext
 *   <li>Controllers access identity via IdentityProvider
 * </ol>
 *
 * <p><b>URL Security:</b>
 *
 * <ul>
 *   <li>Public: everything the shop shows a visitor — the catalog, the cart, the checkout
 *   <li>Staff only: {@code @PreAuthorize} on the route that crosses a customer's data, such as
 *       listing every cart or creating a product
 *   <li>A stranger is challenged, not forbidden: the JWT filter marks a visitor as an anonymous
 *       authentication, so a gate answers {@code 401} to them and {@code 403} to a registered
 *       caller without the role (ADR-036)
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfiguration {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtProperties jwtProperties;
  private final ShopAuthenticationEntryPoint authenticationEntryPoint;
  private final ShopAccessDeniedHandler accessDeniedHandler;

  public SecurityConfiguration(
      final JwtAuthenticationFilter jwtAuthenticationFilter,
      final JwtProperties jwtProperties,
      final ShopAuthenticationEntryPoint authenticationEntryPoint,
      final ShopAccessDeniedHandler accessDeniedHandler) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.jwtProperties = jwtProperties;
    this.authenticationEntryPoint = authenticationEntryPoint;
    this.accessDeniedHandler = accessDeniedHandler;
  }

  @Bean
  @Order(2)
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    http
        // Stateless session management - no HTTP sessions
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // CSRF: cookie-backed token (no HTTP session to store it in); every writing Pug form
        // carries it via CsrfTokenModelAdvice. Bearer-only endpoints are exempt (ADR-035).
        // The token cookie follows the identity cookie's policy: a form inside a foreign frame
        // sends the token only if the cookie carrying it is allowed to travel there too.
        .csrf(
            csrf ->
                csrf.csrfTokenRepository(csrfTokenRepository())
                    .ignoringRequestMatchers("/api/**", "/mcp/**", "/h2-console/**"))

        // Configure authorization rules
        .authorizeHttpRequests(
            authorize ->
                authorize
                    // Public endpoints
                    .requestMatchers("/")
                    .permitAll()
                    .requestMatchers("/products/**")
                    .permitAll()
                    .requestMatchers("/api/products/**")
                    .permitAll()
                    .requestMatchers("/auth/**")
                    .permitAll()
                    .requestMatchers("/login", "/register", "/logout")
                    .permitAll()

                    // H2 console (development only)
                    .requestMatchers("/h2-console/**")
                    .permitAll()

                    // Actuator health endpoint
                    .requestMatchers("/actuator/health", "/actuator/info")
                    .permitAll()

                    // MCP endpoints
                    .requestMatchers("/mcp/**")
                    .permitAll()

                    // Static resources
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico")
                    .permitAll()

                    // Error dispatch (Spring Boot forwards to /error, e.g. for the
                    // Pug4j debug error page) — without this the error page is a bare 403
                    .dispatcherTypeMatchers(DispatcherType.ERROR)
                    .permitAll()

                    // Everything else is open to a visitor: the shop is browsable, and a cart
                    // belongs to whoever holds it, registered or not. What a visitor may not do is
                    // guarded where it is decided — @PreAuthorize on the route, or the use case
                    // asking its question scoped to the caller. A blanket authenticated() here
                    // would guard nothing anyway, because every request carries an identity.
                    .anyRequest()
                    .permitAll())

        // Refusals: a stranger is challenged (401 with WWW-Authenticate on the API, the login form
        // in the browser), a caller who lacks a role is forbidden (403) — ADR-036.
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))

        // Disable Spring Security's default LogoutFilter — logout is handled by
        // LogoutPageController via IdentitySession.logOut(), which clears the session cookie and
        // rotates the visitor identity rather than deleting it (ADR-029)
        .logout(logout -> logout.disable())

        // Framing is refused unless the shop is configured as embeddable
        // (app.security.jwt.allow-framing). Its own switch, because framing is about the origin —
        // where the port counts — while the cookie policy is about the site, where it does not: a
        // slide deck on another port needs framing allowed and nothing else. The .NET twin does the
        // same in Program.cs.
        .headers(
            headers ->
                headers.frameOptions(
                    frame -> {
                      if (jwtProperties.allowFraming()) {
                        frame.disable();
                      } else {
                        frame.sameOrigin();
                      }
                    }))

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * BCrypt password encoder for secure password hashing.
   *
   * <p>BCrypt with strength 12 provides:
   *
   * <ul>
   *   <li>Automatic salt generation
   *   <li>2^12 = 4096 iterations
   *   <li>Timing-safe comparison
   *   <li>OWASP-recommended security level
   * </ul>
   *
   * @return the password encoder bean
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  /**
   * Prevents the JWT filter from being auto-registered as a Servlet filter.
   *
   * <p>Without this, Spring Boot registers the {@code @Component}-annotated filter as both a
   * Security chain filter (via {@code addFilterBefore}) AND a standalone Servlet filter. The
   * standalone registration applies to ALL requests, including those handled by other security
   * chains (e.g., the backoffice form-login chain).
   *
   * @param filter the JWT filter bean
   * @return registration bean with auto-registration disabled
   */
  @Bean
  public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
      final JwtAuthenticationFilter filter) {
    final FilterRegistrationBean<JwtAuthenticationFilter> registration =
        new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

  /**
   * The CSRF token cookie, readable by the form script ({@code HttpOnly} off) and carrying the same
   * {@code SameSite} and {@code Secure} policy as the identity cookie. Without that the token is
   * withheld exactly where the identity still travels — inside a foreign frame — and every form
   * POST fails as a missing token rather than as a refused one.
   */
  private CookieCsrfTokenRepository csrfTokenRepository() {
    final CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    repository.setCookieCustomizer(
        cookie -> cookie.sameSite(jwtProperties.sameSite()).secure(jwtProperties.secureCookies()));
    return repository;
  }
}
