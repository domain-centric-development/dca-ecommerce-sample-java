# checkout — buyerdetails

### Rule: The buyer step asks for email, first name, last name and phone number

#### buyer-step-shows-the-form
- Given a guest who started the checkout with "Domain-Driven Design" in the cart
- When the guest opens the buyer step
- Then it shows the heading "Buyer Information"
- And the fields "Email Address", "First Name", "Last Name" and "Phone Number", all empty and all required
- And a "Continue to Delivery" button
Test: dev.domaincentric.sample.ecommerce.checkout.EnterBuyerDetailsIntegrationTest#theBuyerStepAsksForEmailNamesAndPhoneAllEmptyAndRequired

#### valid-details-open-the-delivery-step
- Given a guest on the buyer step
- When the guest submits Email Address "jane@example.com", First Name "Jane", Last Name "Doe" and Phone
  Number "+49 30 1234567"
- Then the delivery step opens at `/checkout/delivery`
Test: dev.domaincentric.sample.ecommerce.e2e.EnterBuyerDetailsE2ETest#validDetailsOpenTheDeliveryStep

### Rule: Invalid buyer details keep the guest on the buyer step with a message

#### invalid-email-and-missing-phone-keep-the-buyer-step
- Given a guest on the buyer step
- When the guest submits Email Address "not-an-email", First Name "Jane", Last Name "Doe" and no Phone Number
- Then the buyer step stays open and shows a validation error
- And the delivery step is not reached
Test: dev.domaincentric.sample.ecommerce.e2e.EnterBuyerDetailsE2ETest#anInvalidEmailAndAMissingPhoneKeepTheGuestOnTheBuyerStep

#### email-without-at-sign-is-refused
- Given a guest on the buyer step
- When the buyer form is sent past the browser's own checks with Email Address "jane.example.com", First Name
  "Jane", Last Name "Doe" and Phone Number "+49 30 1234567"
- Then the buyer step opens again with the error "Please check the details you entered."
Test: dev.domaincentric.sample.ecommerce.checkout.EnterBuyerDetailsIntegrationTest#anEmailWithoutAnAtSignOpensTheBuyerStepAgainWithTheRefusal

#### blank-first-name-is-refused
- Given a guest on the buyer step
- When the guest submits Email Address "jane@example.com", First Name "   " (spaces only), Last Name "Doe" and
  Phone Number "+49 30 1234567"
- Then the buyer step opens again with the error "Please check the details you entered."
Test: dev.domaincentric.sample.ecommerce.checkout.EnterBuyerDetailsIntegrationTest#aFirstNameOfSpacesOnlyOpensTheBuyerStepAgainWithTheRefusal

#### blank-phone-is-refused
- Given a guest on the buyer step
- When the guest submits Email Address "jane@example.com", First Name "Jane", Last Name "Doe" and Phone
  Number "   " (spaces only)
- Then the buyer step opens again with the error "Please check the details you entered."
Test: dev.domaincentric.sample.ecommerce.checkout.EnterBuyerDetailsIntegrationTest#aPhoneNumberOfSpacesOnlyOpensTheBuyerStepAgainWithTheRefusal

### Rule: A guest on the buyer step is offered to log in or register

#### guest-is-offered-login-and-registration
- Given a guest whose cart holds "Domain-Driven Design"
- When they press "Proceed to Checkout" on the cart page
- Then the buyer step `/checkout/buyer` says "Already have an account? Log in to speed up checkout."
- And it offers a "Login" button to `/login?returnUrl=/checkout/buyer` and a "Register" button to `/register?returnUrl=/checkout/buyer`
- And the "Email Address" field is empty
Test: dev.domaincentric.sample.ecommerce.checkout.BuyerStepKnowsWhoIsBuyingIntegrationTest#aGuestOnTheBuyerStepIsOfferedToLogInOrRegister

### Rule: A logged-in customer sees whom they are logged in as, with the account's email filled in

#### logged-in-customer-sees-who-is-buying
- Given a guest whose cart holds "Domain-Driven Design", who opened the buyer step and then registered on `/register` with "anna.schmidt@example.com"
- When they open the buyer step again
- Then it says "Logged in as anna.schmidt@example.com" instead of offering a login or a registration
- And the "Email Address" field holds "anna.schmidt@example.com"
Test: dev.domaincentric.sample.ecommerce.e2e.BuyerStepKnowsWhoIsBuyingE2ETest#aCustomerWhoRegisteredSeesTheirAccountOnTheBuyerStep

#### customer-orders-with-the-account-email
- Given the customer "anna.schmidt@example.com" is logged in, added "Domain-Driven Design" and pressed "Proceed to Checkout"
- And they kept the filled-in email on the buyer step, entered "Anna", "Schmidt" and "+49 30 1234567", and completed the delivery and payment steps
- When they press "Place Order" on the review step
- Then the confirmation page reads "Your order has been placed successfully"
- And it shows "anna.schmidt@example.com" under "Contact Information"
Test: dev.domaincentric.sample.ecommerce.checkout.BuyerStepKnowsWhoIsBuyingIntegrationTest#aLoggedInCustomerWhoKeepsTheFilledInEmailOrdersWithIt

### Rule: A guest who registers in the middle of the checkout finishes it logged in

#### guest-registers-mid-checkout-and-orders
- Given a guest whose cart holds "Domain-Driven Design", who pressed "Proceed to Checkout", followed "Register" on the buyer step and registered with "anna.schmidt@example.com"
- And back on the buyer step they kept the filled-in email, entered "Anna", "Schmidt" and "+49 30 1234567", and completed the delivery and payment steps
- When they press "Place Order" on the review step
- Then the confirmation page reads "Your order has been placed successfully"
- And it shows "anna.schmidt@example.com" under "Contact Information"
- And the header shows "Welcome, anna.schmidt@example.com"
Test: dev.domaincentric.sample.ecommerce.checkout.BuyerStepKnowsWhoIsBuyingIntegrationTest#aGuestWhoRegistersFromTheBuyerStepFinishesTheOrderLoggedIn
