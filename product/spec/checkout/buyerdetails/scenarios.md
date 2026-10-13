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
