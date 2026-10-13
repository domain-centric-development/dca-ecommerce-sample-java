# account — profile

### Rule: A logged-in customer reaches the overview from the header

#### header-offers-my-account
- Given the customer "anna.schmidt@example.com" is logged in
- When they open the catalogue page `/products`
- Then the header shows a "My Account" link to `/account`
Test: dev.domaincentric.sample.ecommerce.account.AccountOverviewIntegrationTest#headerOffersALoggedInCustomerTheWayToTheirAccount

### Rule: The overview shows the address and the last login

#### overview-shows-the-account
- Given the customer "anna.schmidt@example.com" has just logged in
- When they follow "My Account" in the header
- Then `/account` shows the heading "Welcome, anna.schmidt@example.com" and a card "Account Overview"
- And the card shows "Email" with "anna.schmidt@example.com"
- And "Last Login" shows the date and time of this login in UTC in the form dd.MM.yyyy HH:mm, for example "25.09.2026 14:05"
Test: dev.domaincentric.sample.ecommerce.e2e.AccountOverviewE2ETest#theOverviewShowsTheAddressAndThisLogin

#### overview-after-registration-shows-never
- Given a visitor who has just registered "anna.schmidt@example.com" and has never logged in
- When they open `/account`
- Then "Last Login" shows "Never"
Test: dev.domaincentric.sample.ecommerce.account.NewAccountOverviewIntegrationTest#overviewOfAnAccountThatNeverLoggedInShowsNeverAsLastLogin

### Rule: Every account page has the same left navigation

#### navigation-lists-the-account-pages
- Given the customer "anna.schmidt@example.com" is logged in
- When they open `/account`
- Then the account navigation lists, in this order, "Overview" linking to `/account`, "My Profile" linking to `/account/profile`, "Change Password" linking to `/account/change-password` and "My Orders"
- And "Overview" is marked as the current page
Test: dev.domaincentric.sample.ecommerce.account.AccountOverviewIntegrationTest#navigationListsTheAccountPagesInOrderWithTheOverviewCurrent

#### my-orders-is-not-a-link
- Given the customer "anna.schmidt@example.com" is logged in
- When they open `/account`
- Then "My Orders" is shown as disabled and is not a link
Test: dev.domaincentric.sample.ecommerce.account.AccountOverviewIntegrationTest#myOrdersIsShownDisabledAndIsNoLink

### Rule: The account area needs a login and returns to the page asked for

#### overview-sends-a-visitor-to-the-login
- Given a visitor with no cookies
- When they open `/account`
- Then they are sent to `/login?returnUrl=%2Faccount`
Test: dev.domaincentric.sample.ecommerce.account.AccountOverviewIntegrationTest#overviewSendsAVisitorWithoutCookiesToTheLogin

#### login-returns-to-the-overview
- Given the account "anna.schmidt@example.com" and a visitor who was sent from `/account` to the login page
- When they log in with "anna.schmidt@example.com" and "Secret123"
- Then `/account` opens with "Welcome, anna.schmidt@example.com"
Test: dev.domaincentric.sample.ecommerce.account.AccountOverviewIntegrationTest#loginOpensTheOverviewTheVisitorWasSentFrom

### Rule: The profile shows the fixed name and the details that can change

#### profile-shows-name-and-details
- Given the customer "anna.schmidt@example.com", registered as Anna Schmidt born 1990-05-17, is logged in
- When they follow "My Profile" in the account navigation
- Then `/account/profile` shows the heading "My Profile", a card "Basic Information" and "My Profile" marked as the current page
- And the name "Anna Schmidt" is shown as text with "Your name was set when you created your account and cannot be changed." and there is no input for a name
- And the field "Email" holds "anna.schmidt@example.com" with the hint "This address is also your login"
- And the field "Date of Birth" holds 1990-05-17 and the button reads "Save Changes"
Test: dev.domaincentric.sample.ecommerce.account.ProfileIntegrationTest#theProfileShowsTheFixedNameAndTheDetailsThatCanChange

### Rule: Saving stores email and date of birth and keeps the customer logged in

#### saving-the-profile
- Given the customer "anna.schmidt@example.com" is logged in on `/account/profile`
- When they change Email to "anna.meyer@example.com" and Date of Birth to 1990-05-18 and press "Save Changes"
- Then `/account/profile` opens again and shows "Your profile has been updated."
- And the fields hold "anna.meyer@example.com" and 1990-05-18
- And the header shows "Welcome, anna.meyer@example.com"
Test: dev.domaincentric.sample.ecommerce.e2e.ProfileE2ETest#savingStoresTheNewAddressAndDateAndKeepsTheCustomerLoggedIn

#### own-address-in-other-capitals-is-accepted
- Given the customer "anna.schmidt@example.com" is logged in on `/account/profile`
- When they save Email "Anna.Schmidt@Example.com" with the same date of birth
- Then the profile shows "Your profile has been updated." and the field Email holds "anna.schmidt@example.com"
Test: dev.domaincentric.sample.ecommerce.account.ProfileIntegrationTest#theOwnAddressInOtherCapitalsIsSavedAsTheAddressItIs

### Rule: The new address is the login from now on

#### new-address-logs-in
- Given the customer changed the address from "anna.schmidt@example.com" to "anna.meyer@example.com" and a browser with no cookies
- When they log in with "anna.meyer@example.com" and "Secret123"
- Then the cart page opens and the header shows "Welcome, anna.meyer@example.com"
Test: dev.domaincentric.sample.ecommerce.account.ChangedAddressLoginIntegrationTest#theNewAddressLogsInAndOpensTheCart

#### old-address-no-longer-logs-in
- Given the customer changed the address from "anna.schmidt@example.com" to "anna.meyer@example.com" and a browser with no cookies
- When they log in with "anna.schmidt@example.com" and "Secret123"
- Then the login page shows "Invalid email or password"
Test: dev.domaincentric.sample.ecommerce.account.ChangedAddressLoginIntegrationTest#theOldAddressIsRefusedWithTheGenericMessage

### Rule: The profile needs a login

#### profile-sends-a-visitor-to-the-login
- Given a visitor with no cookies
- When they open `/account/profile`
- Then they are sent to `/login?returnUrl=%2Faccount%2Fprofile`
Test: dev.domaincentric.sample.ecommerce.account.ProfileIntegrationTest#theProfileSendsAVisitorWithoutCookiesToTheLogin

### Rule: An address another account uses cannot be taken over

#### taken-address-is-refused-in-the-profile
- Given the accounts "anna.schmidt@example.com" and "ben.weber@example.com", and Anna logged in on `/account/profile`
- When she saves Email "ben.weber@example.com"
- Then the profile shows "This email address is already registered"
- And the field Email still holds "ben.weber@example.com"
Test: dev.domaincentric.sample.ecommerce.e2e.ProfileE2ETest#anAddressAnotherAccountUsesIsRefusedAndStaysInTheField

### Rule: Address and date of birth must be usable

#### malformed-address-is-refused-in-the-profile
- Given the customer "anna.schmidt@example.com" is logged in on `/account/profile`
- When they save Email "anna.meyer@example"
- Then the profile shows "Please enter a valid email address."
Test: dev.domaincentric.sample.ecommerce.account.ProfileIntegrationTest#aMalformedAddressIsRefusedWithThePagesSentence

#### missing-date-is-refused-in-the-profile
- Given the customer "anna.schmidt@example.com" is logged in on `/account/profile`
- When they save an empty Date of Birth
- Then the profile shows "Please enter a date like 1990-05-17"
Test: dev.domaincentric.sample.ecommerce.account.ProfileIntegrationTest#aMissingDateOfBirthIsRefusedWithTheExpectedFormat

#### future-date-is-refused-in-the-profile
- Given the customer "anna.schmidt@example.com" is logged in on `/account/profile`
- When they save Date of Birth 2999-01-01
- Then the profile shows "Please enter a date of birth that is not in the future."
Test: dev.domaincentric.sample.ecommerce.account.ProfileIntegrationTest#aDateOfBirthInTheFutureIsRefused

#### unreadable-date-is-refused-in-the-profile
- Given the customer "anna.schmidt@example.com" is logged in on `/account/profile`
- When they save Date of Birth "17.05.1990"
- Then the profile shows "Please enter a date like 1990-05-17"
- And the field Date of Birth still holds "17.05.1990"
Test: dev.domaincentric.sample.ecommerce.account.ProfileIntegrationTest#anUnreadableDateIsRefusedAndStaysInTheField

### Rule: A refused change saves nothing, not even the valid field

#### refused-change-saves-nothing
- Given the customer "anna.schmidt@example.com", born 1990-05-17, saved Email "anna.meyer@example.com" together with Date of Birth 2999-01-01 and was refused
- When they open `/account/profile` again
- Then the fields hold "anna.schmidt@example.com" and 1990-05-17
Test: dev.domaincentric.sample.ecommerce.account.ProfileIntegrationTest#aRefusedChangeSavesNeitherTheValidAddressNorTheDate
