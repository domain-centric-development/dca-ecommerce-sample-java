package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewInputPort;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewQuery;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewResult;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileInputPort;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileQuery;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileResult;
import dev.domaincentric.sample.ecommerce.account.application.shared.IdentitySession;
import dev.domaincentric.sample.ecommerce.account.application.shared.TokenService;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Test doubles and assertions shared by the tests of the account web adapter.
 *
 * <p>All doubles depend on ports only ({@link GetAccountOverviewInputPort}, {@link
 * IdentityProvider}), never on an outgoing adapter such as the JWT identity provider.
 */
final class AccountWebTestFixtures {

  private AccountWebTestFixtures() {}

  /**
   * Reads a query parameter from a Spring {@code redirect:} view name.
   *
   * @param viewName the returned view name, e.g. {@code redirect:/login?returnUrl=%2Faccount}
   * @param name the query parameter name
   * @return the decoded parameter value, {@code null} if the parameter is absent
   */
  static String queryParameter(final String viewName, final String name) {
    final int queryStart = viewName.indexOf('?');
    assertTrue(queryStart > 0, "expected query parameters in redirect: " + viewName);
    final Map<String, String> parameters = new HashMap<>();
    for (final String pair : viewName.substring(queryStart + 1).split("&")) {
      final int separator = pair.indexOf('=');
      if (separator > 0) {
        parameters.put(
            URLDecoder.decode(pair.substring(0, separator), StandardCharsets.UTF_8),
            URLDecoder.decode(pair.substring(separator + 1), StandardCharsets.UTF_8));
      }
    }
    return parameters.get(name);
  }

  /** Test double for the account overview input port, recording the queries it receives. */
  static final class TestGetAccountOverview implements GetAccountOverviewInputPort {

    private final List<GetAccountOverviewQuery> receivedQueries = new ArrayList<>();
    private GetAccountOverviewResult result = GetAccountOverviewResult.notFound();

    @Override
    public GetAccountOverviewResult execute(final GetAccountOverviewQuery query) {
      receivedQueries.add(query);
      return result;
    }

    void setResult(final GetAccountOverviewResult result) {
      this.result = result;
    }

    List<GetAccountOverviewQuery> receivedQueries() {
      return List.copyOf(receivedQueries);
    }
  }

  /** Test double for the get profile input port, recording the queries it receives. */
  static final class TestGetProfile implements GetProfileInputPort {

    private final List<GetProfileQuery> receivedQueries = new ArrayList<>();
    private GetProfileResult result = GetProfileResult.notFound();

    @Override
    public GetProfileResult execute(final GetProfileQuery query) {
      receivedQueries.add(query);
      return result;
    }

    void setResult(final GetProfileResult result) {
      this.result = result;
    }

    List<GetProfileQuery> receivedQueries() {
      return List.copyOf(receivedQueries);
    }
  }

  /** Test double for the token service, recording the claims each issued token was built from. */
  static final class TestTokenService implements TokenService {

    private final List<IssuedToken> issuedTokens = new ArrayList<>();

    @Override
    public String generateRegisteredToken(
        final UserId userId, final String email, final Set<String> roles) {
      final IssuedToken token = new IssuedToken(userId, email, Set.copyOf(roles));
      issuedTokens.add(token);
      return "token-for-" + email;
    }

    List<IssuedToken> issuedTokens() {
      return List.copyOf(issuedTokens);
    }

    /** The claims a token was generated for. */
    record IssuedToken(UserId userId, String email, Set<String> roles) {}
  }

  /** Test double for the identity session, recording the tokens it was handed. */
  static final class TestIdentitySession implements IdentitySession {

    private final List<String> setTokens = new ArrayList<>();
    private int clearCount;

    @Override
    public void setRegisteredIdentity(final String token) {
      setTokens.add(token);
    }

    @Override
    public void logOut() {
      clearCount++;
    }

    List<String> setTokens() {
      return List.copyOf(setTokens);
    }

    int clearCount() {
      return clearCount;
    }
  }

  /** Test double for an identity, depending on the port only. */
  record TestIdentity(
      UserId userId, IdentityProvider.IdentityType type, Optional<String> email, Set<String> roles)
      implements IdentityProvider.Identity {

    static TestIdentity anonymous(final UserId userId) {
      return new TestIdentity(userId, TestIdentityType.ANONYMOUS, Optional.empty(), Set.of());
    }

    static TestIdentity registered(final UserId userId, final String email) {
      return new TestIdentity(
          userId, TestIdentityType.REGISTERED, Optional.of(email), Set.of(ROLE_CUSTOMER));
    }
  }

  /** Identity types used by {@link TestIdentity}. */
  enum TestIdentityType implements IdentityProvider.IdentityType {
    ANONYMOUS,
    REGISTERED;

    @Override
    public boolean isAnonymous() {
      return this == ANONYMOUS;
    }

    @Override
    public boolean isRegistered() {
      return this == REGISTERED;
    }
  }

  /** Test double for the identity provider. */
  static final class TestIdentityProvider implements IdentityProvider {

    private Identity identity = TestIdentity.anonymous(UserId.of("anonymous-user"));

    @Override
    public Identity getCurrentIdentity() {
      return identity;
    }

    void setIdentity(final Identity identity) {
      this.identity = identity;
    }
  }
}
