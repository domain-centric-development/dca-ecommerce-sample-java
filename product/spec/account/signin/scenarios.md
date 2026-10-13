# account — signin

### Rule: A visitor can reach the login from every shop page

#### header-offers-login
- Given a visitor with no cookies
- When they open the catalogue page `/products`
- Then the header shows a "Login" link to `/login` next to the "Register" link
Test: dev.domaincentric.sample.ecommerce.account.LoginPageIntegrationTest#headerOffersALoginLinkNextToTheRegisterLink

#### login-page-shows-the-form
- Given a visitor with no cookies
- When they open `/login`
- Then the page heading reads "Login"
- And the form has the fields "Email" and "Password" and the button "Login"
- And a link "Register here" leads to `/register`
Test: dev.domaincentric.sample.ecommerce.account.LoginPageIntegrationTest#loginPageShowsTheEmailAndPasswordForm

### Rule: A successful login starts the session and opens the cart, or the page the customer came for

#### login-opens-the-cart
- Given the account "anna.schmidt@example.com" with password "Secret123", registered earlier, and a browser with no cookies and an empty cart
- When the visitor logs in on `/login` with "anna.schmidt@example.com" and "Secret123"
- Then the cart page `/cart` opens and shows "Welcome back!"
- And the header shows "Welcome, anna.schmidt@example.com" and a "Logout" button instead of "Login" and "Register"
Test: dev.domaincentric.sample.ecommerce.e2e.LoginE2ETest#loginOpensTheCartAndGreetsTheCustomer

#### login-returns-to-the-given-page
- Given the account "anna.schmidt@example.com" and a browser with no cookies that opened `/login?returnUrl=/products`
- When the visitor logs in with "anna.schmidt@example.com" and "Secret123"
- Then the catalogue page `/products` opens
Test: dev.domaincentric.sample.ecommerce.account.LoginPageIntegrationTest#loginReturnsToThePageTheVisitorCameFrom

#### register-link-keeps-the-return-page
- Given a visitor with no cookies
- When they open `/login?returnUrl=/products`
- Then the "Register here" link leads to `/register?returnUrl=/products`
Test: dev.domaincentric.sample.ecommerce.account.LoginPageIntegrationTest#registerLinkCarriesTheReturnPageOn

### Rule: Logging out ends the session and says so

#### logout-confirms-on-the-login-page
- Given the customer "anna.schmidt@example.com" is logged in
- When they press "Logout" in the header
- Then the login page `/login?logout=true` opens and shows "You have been successfully logged out."
- And the header shows "Login" and "Register" again
Test: dev.domaincentric.sample.ecommerce.account.LoginPageIntegrationTest#logoutEndsTheSessionAndConfirmsOnTheLoginPage

### Rule: Every failed login shows the same message and keeps the typed email

#### wrong-password-is-refused
- Given the account "anna.schmidt@example.com" with password "Secret123" and a browser with no cookies on `/login`
- When the visitor logs in with "anna.schmidt@example.com" and "Secret999"
- Then the login page shows "Invalid email or password"
- And the Email field still holds "anna.schmidt@example.com" and the Password field is empty
- And the header still shows "Login" and "Register"
Test: dev.domaincentric.sample.ecommerce.e2e.LoginE2ETest#aWrongPasswordIsRefusedWithTheGenericMessageAndKeepsTheEmail

#### unknown-address-is-refused
- Given no account for "nobody@example.com" and a browser with no cookies on `/login`
- When the visitor logs in with "nobody@example.com" and "Secret123"
- Then the login page shows "Invalid email or password"
Test: dev.domaincentric.sample.ecommerce.account.FailedLoginIntegrationTest#anAddressWithoutAnAccountIsRefusedWithTheGenericMessage

#### malformed-address-is-refused
- Given a browser with no cookies on `/login`
- When the visitor logs in with "anna.schmidt@example" and "Secret123"
- Then the login page shows "Invalid email or password"
Test: dev.domaincentric.sample.ecommerce.account.FailedLoginIntegrationTest#aMalformedAddressIsRefusedWithTheGenericMessage
