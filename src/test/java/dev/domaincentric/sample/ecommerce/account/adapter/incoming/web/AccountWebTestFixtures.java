package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.account.api.Identity;
import dev.domaincentric.sample.ecommerce.account.api.IdentityService;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewInputPort;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewQuery;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewResult;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileInputPort;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileQuery;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileResult;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test doubles and assertions shared by the tests of the account web adapter.
 *
 * <p>The doubles stand in for the input ports ({@link GetAccountOverviewInputPort}) and for the
 * Account context's published {@link IdentityService}; the JWT collaborators of the web adapter
 * ({@code JwtTokenService}, {@code JwtIdentitySession}) are used as they are, against a mock
 * response.
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

  /** Test double for the Account context's published identity service. */
  static final class TestIdentityService implements IdentityService {

    private Identity identity = Identity.anonymous(UserId.of("anonymous-user"));

    @Override
    public Identity currentIdentity() {
      return identity;
    }

    void setIdentity(final Identity identity) {
      this.identity = identity;
    }
  }
}
