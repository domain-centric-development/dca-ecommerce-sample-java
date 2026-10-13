package dev.domaincentric.sample.ecommerce.account;

import static dev.domaincentric.sample.ecommerce.account.Visitor.element;
import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.account.Visitor.Element;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The account overview a logged-in customer opens, rendered by the wired application: the way to it
 * from the header, the account navigation it shows, and the login it asks a visitor for.
 */
// MockMvc requests commit, and the shared H2 database outlives the Spring context — so this class
// gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:account_overview_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Account overview")
class AccountOverviewIntegrationTest {

  private static final String EMAIL = "anna.schmidt@example.com";
  private static final String PASSWORD = "Secret123";

  private static final Pattern NAV_ITEM =
      Pattern.compile("<(a|span)([^>]*\\bdata-test=\"account-nav-[^\"]*\"[^>]*)>([^<]*)<");

  /** The class shares one database, so the account is registered once for all its tests. */
  private static boolean accountRegistered;

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Header offers my account")
  void headerOffersALoggedInCustomerTheWayToTheirAccount() throws Exception {
    final Visitor customer = loggedInCustomer();

    final Element myAccount = element(customer.page("/products"), "nav-my-account-link");

    assertThat(myAccount.text()).isEqualTo("My Account");
    assertThat(myAccount.attribute("href")).isEqualTo("/account");
  }

  @Test
  @DisplayName("Navigation lists the account pages")
  void navigationListsTheAccountPagesInOrderWithTheOverviewCurrent() throws Exception {
    final String overview = loggedInCustomer().page("/account");

    final List<Element> items = navigationItems(overview);
    assertThat(items)
        .extracting(Element::text)
        .containsExactly("Overview", "My Profile", "Change Password", "My Orders");
    assertThat(items.get(0).attribute("href")).isEqualTo("/account");
    assertThat(items.get(1).attribute("href")).isEqualTo("/account/profile");
    assertThat(items.get(2).attribute("href")).isEqualTo("/account/change-password");
    assertThat(items)
        .filteredOn(item -> item.has("aria-current"))
        .extracting(Element::text)
        .as("the item marked as the current page")
        .containsExactly("Overview");
    assertThat(items.get(0).attribute("aria-current")).isEqualTo("page");
  }

  @Test
  @DisplayName("My orders is not a link")
  void myOrdersIsShownDisabledAndIsNoLink() throws Exception {
    final Element orders = element(loggedInCustomer().page("/account"), "account-nav-orders");

    assertThat(orders.text()).isEqualTo("My Orders");
    assertThat(orders.tag()).as("the orders item is no link").isNotEqualTo("a");
    assertThat(orders.has("href")).as("the orders item carries no href").isFalse();
    assertThat(orders.attribute("aria-disabled")).isEqualTo("true");
  }

  @Test
  @DisplayName("Overview sends a visitor to the login")
  void overviewSendsAVisitorWithoutCookiesToTheLogin() throws Exception {
    final Visitor visitor = new Visitor(mockMvc);

    assertThat(visitor.redirectOf("/account")).isEqualTo("/login?returnUrl=%2Faccount");
  }

  @Test
  @DisplayName("Login returns to the overview")
  void loginOpensTheOverviewTheVisitorWasSentFrom() throws Exception {
    registerTheAccount();
    final Visitor visitor = new Visitor(mockMvc);
    final String loginPage = visitor.redirectOf("/account");

    final String afterLogin = visitor.logIn(loginPage, EMAIL, PASSWORD);

    assertThat(afterLogin).isEqualTo("/account");
    assertThat(element(visitor.page(afterLogin), "account-greeting").text())
        .isEqualTo("Welcome, " + EMAIL);
  }

  /** A browser of its own in which the scenario's customer has logged in. */
  private Visitor loggedInCustomer() throws Exception {
    registerTheAccount();
    final Visitor customer = new Visitor(mockMvc);
    customer.logIn("/login", EMAIL, PASSWORD);
    return customer;
  }

  /** Registers the scenario's account from a browser of its own, once for this database. */
  private void registerTheAccount() throws Exception {
    if (accountRegistered) {
      return;
    }
    new Visitor(mockMvc).register(EMAIL, PASSWORD);
    accountRegistered = true;
  }

  private static List<Element> navigationItems(final String page) {
    final Matcher matcher = NAV_ITEM.matcher(page);
    final List<Element> items = new ArrayList<>();
    while (matcher.find()) {
      items.add(new Element(matcher.group(1), matcher.group(2), matcher.group(3).strip()));
    }
    return items;
  }
}
