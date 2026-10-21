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

### Rule: An address can belong to one account only, whatever its capitals and surrounding spaces

#### taken-address-is-refused
- Given the account "anna.schmidt@example.com", registered earlier, and another visitor with no cookies on `/register`
- When they submit a valid registration with Email "anna.schmidt@example.com"
- Then the registration page shows "This email address is already registered"
- And the visitor is not logged in
Test: dev.domaincentric.sample.ecommerce.e2e.RegistrationRefusalE2ETest#anAddressAnotherAccountHasIsRefusedAndTheVisitorStaysLoggedOut

#### taken-address-in-other-capitals-is-refused
- Given the account "anna.schmidt@example.com", registered earlier, and another visitor with no cookies on `/register`
- When they submit a valid registration with Email " Anna.Schmidt@Example.COM "
- Then the registration page shows "This email address is already registered"
Test: dev.domaincentric.sample.ecommerce.account.TakenAddressRegistrationIntegrationTest#anAddressAnotherAccountHasIsRefusedWhateverItsCapitalsAndSurroundingSpaces

### Rule: A browser that already has an account cannot register a second one

#### second-registration-from-the-same-browser-is-refused
- Given a visitor who just registered "anna.schmidt@example.com" and is logged in
- When they open `/register` and submit a valid registration with Email "anna.second@example.com"
- Then the registration page shows "You already have an account."
- And the header still shows "Welcome, anna.schmidt@example.com"
Test: dev.domaincentric.sample.ecommerce.account.SecondAccountRegistrationIntegrationTest#aBrowserThatAlreadyHasAnAccountCannotRegisterASecondOneAndStaysLoggedIn

### Rule: A refused registration shows one message and keeps everything but the passwords

#### differing-passwords-are-refused
- Given a visitor with no cookies on `/register`
- When they submit First Name "Anna", Last Name "Schmidt", Date of Birth 1990-05-17, Email "anna.schmidt@example.com", Password "Secret123" and Confirm Password "Secret124"
- Then the registration page shows "Passwords do not match"
- And First Name "Anna", Last Name "Schmidt", Date of Birth 1990-05-17 and Email "anna.schmidt@example.com" are still filled in, both password fields are empty
- And the visitor is not logged in
Test: dev.domaincentric.sample.ecommerce.e2e.RegistrationE2ETest#differingPasswordsAreRefusedAndTheFormKeepsTheRest

### Rule: The password needs at least 8 characters

#### short-password-is-refused
- Given a visitor with no cookies on `/register`
- When they submit an otherwise valid registration with Password and Confirm Password "Sec123"
- Then the registration page shows "Password must be at least 8 characters"
Test: dev.domaincentric.sample.ecommerce.account.RegistrationPageIntegrationTest#shortPasswordIsRefusedWithTheLengthRule

### Rule: The date of birth must be a real date, and not in the future

#### unreadable-date-is-refused
- Given a visitor with no cookies on `/register`
- When they submit an otherwise valid registration with Date of Birth "17.05.1990"
- Then the registration page shows "Please enter a date like 1990-05-17"
Test: dev.domaincentric.sample.ecommerce.account.RegistrationPageIntegrationTest#unreadableDateIsRefusedWithTheExpectedFormat

#### future-date-is-refused
- Given a visitor with no cookies on `/register`
- When they submit an otherwise valid registration with Date of Birth 2999-01-01
- Then the registration page shows "Please check the email address, your name and your date of birth."
Test: dev.domaincentric.sample.ecommerce.account.RegistrationPageIntegrationTest#futureDateOfBirthIsRefusedWithTheCheckYourEntriesSentence

### Rule: The address must be an email address

#### malformed-email-is-refused
- Given a visitor with no cookies on `/register`
- When they submit an otherwise valid registration with Email "anna.schmidt@example"
- Then the registration page shows "Please check the email address, your name and your date of birth."
Test: dev.domaincentric.sample.ecommerce.account.RegistrationPageIntegrationTest#malformedEmailIsRefusedWithTheCheckYourEntriesSentence

### Rule: Both names are required and at most 100 characters long

#### blank-first-name-is-refused
- Given a visitor with no cookies on `/register`
- When they submit an otherwise valid registration with First Name "   "
- Then the registration page shows "Please check the email address, your name and your date of birth."
Test: dev.domaincentric.sample.ecommerce.account.RegistrationPageIntegrationTest#blankFirstNameIsRefusedWithTheCheckYourEntriesSentence

#### overlong-last-name-is-refused
- Given a visitor with no cookies on `/register`
- When they submit an otherwise valid registration with a Last Name of 101 characters
- Then the registration page shows "Please check the email address, your name and your date of birth."
Test: dev.domaincentric.sample.ecommerce.account.RegistrationPageIntegrationTest#overlongLastNameIsRefusedWithTheCheckYourEntriesSentence

### Rule: After a restart the shop knows no account, and a logged-in browser quietly becomes a visitor

#### restart-turns-the-customer-into-a-visitor
- Given the customer "anna.schmidt@example.com" is logged in and the shop has been restarted since
- When they open the catalogue page `/products` in the same browser
- Then the page shows the catalogue without an error message
- And the header shows "Login" and "Register" instead of "Welcome, anna.schmidt@example.com"
Test: dev.domaincentric.sample.ecommerce.e2e.ShopRestartE2ETest#aCustomerLoggedInBeforeARestartBrowsesTheCatalogueAsAVisitor

#### account-pages-send-to-the-login-after-a-restart
- Given the customer "anna.schmidt@example.com" was logged in and the shop has been restarted since
- When they open `/account` in the same browser
- Then they are sent to `/login?returnUrl=%2Faccount`
Test: dev.domaincentric.sample.ecommerce.account.RestartedShopIntegrationTest#anAccountPageSendsABrowserWithASessionFromBeforeTheRestartToTheLogin

#### old-credentials-are-refused-after-a-restart
- Given the account "anna.schmidt@example.com" with password "Secret123" existed before the shop was restarted
- When the visitor logs in with "anna.schmidt@example.com" and "Secret123"
- Then the login page shows "Invalid email or password"
Test: dev.domaincentric.sample.ecommerce.account.RestartedShopIntegrationTest#credentialsOfAnAccountFromBeforeTheRestartAreRefused

### Rule: Registering through the API creates an account and hands out its token

#### registering-hands-out-a-token
- Given no account exists for "ada@example.com"
- When a client sends `POST /api/auth/register` with `{"email":"ada@example.com","password":"Secret123","firstName":"Ada","lastName":"Lovelace","dateOfBirth":"1815-12-10"}`
- Then the shop answers 201
- And the body has `success` true, a non-empty `token` and `email` "ada@example.com"
Test: dev.domaincentric.sample.ecommerce.e2e.RegistrationApiE2ETest#registeringAnAccountAnswersCreatedWithItsToken

### Rule: Registration refusals are problem documents

#### an-address-already-registered-is-a-conflict
- Given an account "ada@example.com"
- When a client sends `POST /api/auth/register` with email "ada@example.com", password "Secret123", first name "Grace", last name "Hopper" and date of birth "1906-12-09"
- Then the shop answers 409 with an `application/problem+json` body
- And its title is "Email already registered" and its detail "The email address ada@example.com is already registered."
Test: dev.domaincentric.sample.ecommerce.account.RegistrationApiIntegrationTest#anAddressAnotherAccountHoldsIsAConflictStatedAsAProblem

#### a-weak-password-is-refused
- Given no account exists for "grace@example.com"
- When a client registers "grace@example.com" through the API with password "secret123"
- Then the shop answers 422 with an `application/problem+json` body
- And its title is "Password too weak" and its detail "Password must contain an uppercase letter."
Test: dev.domaincentric.sample.ecommerce.account.RegistrationApiIntegrationTest#aPasswordWithoutAnUppercaseLetterIsRefusedNamingTheRule

#### a-malformed-address-is-refused
- Given no account exists for "grace@example"
- When a client registers "grace@example" through the API with password "Secret123"
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail
Test: dev.domaincentric.sample.ecommerce.account.RegistrationApiIntegrationTest#aMalformedAddressIsABadRequestWithoutDetail

#### a-caller-with-an-account-cannot-register-another
- Given a client holding the token of the account "ada@example.com"
- When it sends `POST /api/auth/register` for "grace@example.com" with `Authorization: Bearer <that token>`
- Then the shop answers 409 with an `application/problem+json` body
- And its title is "Account already exists" and its detail "The signed-in user already has an account."
Test: dev.domaincentric.sample.ecommerce.account.RegistrationApiIntegrationTest#aCallerHoldingTheTokenOfAnAccountCannotRegisterASecondOne

### Rule: The page a successful registration leads to says once "Account created successfully! Welcome!"

#### registration-says-the-account-was-created
- Given a visitor with no cookies on `/register`
- When they submit First Name "Anna", Last Name "Schmidt", Date of Birth 1990-05-17, Email "anna.schmidt@example.com",
  Password "Secret123" and Confirm Password "Secret123"
- Then the home page `/` opens and shows "Account created successfully! Welcome!"
Test: dev.domaincentric.sample.ecommerce.e2e.RegistrationE2ETest#theHomePageARegistrationLeadsToSaysTheAccountWasCreated

#### the-page-returned-to-says-the-account-was-created
- Given a visitor with no cookies who opened `/register?returnUrl=/products`
- When they submit a valid registration for "anna.schmidt@example.com"
- Then the catalogue page `/products` opens and shows "Account created successfully! Welcome!"
Test: dev.domaincentric.sample.ecommerce.account.RegistrationNoticeIntegrationTest#thePageTheVisitorCameFromSaysTheAccountWasCreated

### Rule: A return page outside the shop is not followed; the registration then leads to the home page

#### an-outside-return-page-leads-home-after-registering
- Given a visitor with no cookies who opened `/register?returnUrl=https://elsewhere.example/`
- When they submit a valid registration for "anna.schmidt@example.com"
- Then the home page `/` opens and shows "Account created successfully! Welcome!"
Test: dev.domaincentric.sample.ecommerce.e2e.RegistrationE2ETest#aRegistrationLeadsToTheHomePageInsteadOfAnotherSitesReturnPage

#### a-scheme-relative-return-page-leads-home-after-registering
- Given a visitor with no cookies who opened `/register?returnUrl=//elsewhere.example/`
- When they submit a valid registration for "anna.schmidt@example.com"
- Then the home page `/` opens and shows "Account created successfully! Welcome!"
Test: dev.domaincentric.sample.ecommerce.account.RegistrationPageIntegrationTest#aRegistrationLeadsToTheHomePageInsteadOfASchemeRelativeReturnPage

### Rule: The "Login here" link carries a return page on only when it is a page of the shop

#### the-login-link-drops-an-outside-return-page
- Given a visitor with no cookies
- When they open `/register?returnUrl=https://elsewhere.example/`
- Then the "Login here" link leads to `/login`
Test: dev.domaincentric.sample.ecommerce.account.RegistrationPageIntegrationTest#loginLinkLeadsToTheLoginWithoutAReturnPageOfAnotherSite
