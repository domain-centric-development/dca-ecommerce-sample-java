# account — registration

### Rule: A visitor can reach the registration from every shop page

#### header-offers-register
- Given a visitor with no cookies
- When they open the catalogue page `/products`
- Then the header shows a "Register" link to `/register`
Test: dev.domaincentric.sample.ecommerce.account.RegistrationPageIntegrationTest#headerOffersARegisterLinkToTheRegistrationPage

### Rule: The registration form asks for the owner, the address and the password

#### register-page-shows-the-form
- Given a visitor with no cookies
- When they open `/register`
- Then the page heading reads "Create Account"
- And the form has the fields "First Name", "Last Name", "Date of Birth", "Email", "Password" and "Confirm Password" and the button "Create Account"
- And it shows the hints "Your name cannot be changed later" and "Password must be at least 8 characters"
- And the name fields accept at most 100 characters and the password fields at most 72
- And a link "Login here" leads to `/login`
Test: dev.domaincentric.sample.ecommerce.account.RegistrationPageIntegrationTest#registerPageShowsTheOwnerAddressAndPasswordForm

### Rule: A successful registration logs the visitor in

#### registration-logs-the-visitor-in
- Given a visitor with no cookies on `/register`
- When they submit First Name "Anna", Last Name "Schmidt", Date of Birth 1990-05-17, Email "anna.schmidt@example.com", Password "Secret123" and Confirm Password "Secret123"
- Then they are sent to the home page `/`
- And the header shows "Welcome, anna.schmidt@example.com" instead of the "Register" link
Test: dev.domaincentric.sample.ecommerce.e2e.RegistrationE2ETest#registrationLogsTheVisitorIn

#### registration-returns-to-the-given-page
- Given a visitor with no cookies who opened `/register?returnUrl=/products`
- When they submit a valid registration for "anna.schmidt@example.com"
- Then they are sent to the catalogue page `/products`
- And the header shows "Welcome, anna.schmidt@example.com"
Test: dev.domaincentric.sample.ecommerce.account.RegistrationPageIntegrationTest#registrationReturnsToThePageTheVisitorCameFrom

#### login-link-keeps-the-return-page
- Given a visitor with no cookies
- When they open `/register?returnUrl=/products`
- Then the "Login here" link leads to `/login?returnUrl=/products`
Test: dev.domaincentric.sample.ecommerce.account.RegistrationPageIntegrationTest#loginLinkCarriesTheReturnPageOn
