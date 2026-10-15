# checkout — delivery

### Rule: The delivery step asks for an address and a shipping method

#### delivery-step-shows-the-form
- Given a guest who submitted the buyer step
- When the guest opens the delivery step
- Then it shows under "Shipping Address" the fields "Street Address", "Address Line 2 (Optional)", "City",
  "State/Province (Optional)", "Postal Code" and "Country"
- And Street Address, City, Postal Code and Country are required, Address Line 2 and State/Province are not
- And a "Back" link and a "Continue to Payment" button
Test: dev.domaincentric.sample.ecommerce.checkout.ChooseTheDeliveryAddressIntegrationTest#theDeliveryStepAsksForTheAddressWithFourRequiredFieldsABackLinkAndAButton

#### four-shipping-methods-are-offered
- Given a guest who submitted the buyer step
- When the guest opens the delivery step
- Then it shows under "Shipping Method" four choices, each with name, estimate and price:
  "Standard Shipping", "5-7 business days", "4.99 EUR"; "Express Shipping", "2-3 business days", "9.99 EUR";
  "Overnight Shipping", "Next business day", "19.99 EUR"; "Free Shipping", "7-10 business days", "0.00 EUR"
- And one of them must be chosen before the form can be sent
Test: dev.domaincentric.sample.ecommerce.checkout.ChooseTheDeliveryAddressIntegrationTest#theDeliveryStepOffersFourShippingMethodsOneOfWhichMustBeChosen

#### delivery-step-summarises-the-order
- Given a guest who started the checkout with "Domain-Driven Design" once and submitted the buyer step
- When the guest opens the delivery step
- Then its "Order Summary" shows "Domain-Driven Design x1 54.99 EUR" and Subtotal "54.99 EUR"
Test: dev.domaincentric.sample.ecommerce.checkout.ChooseTheDeliveryAddressIntegrationTest#theDeliveryStepSummarisesTheOrderWithItsSubtotal

#### address-and-shipping-open-the-payment-step
- Given a guest on the delivery step
- When the guest submits Street Address "Hauptstrasse 1", City "Berlin", Postal Code "10115", Country
  "Germany", leaves Address Line 2 and State/Province empty and chooses "Standard Shipping"
- Then the payment step opens at `/checkout/payment`
Test: dev.domaincentric.sample.ecommerce.e2e.ChooseTheDeliveryAddressE2ETest#anAddressAndAShippingMethodOpenThePaymentStep

### Rule: A blank required address field keeps the guest on the delivery step with a message

#### blank-street-is-refused
- Given a guest on the delivery step
- When the guest submits Street Address "   " (spaces only), City "Berlin", Postal Code "10115", Country
  "Germany" and "Standard Shipping"
- Then the delivery step opens again with the error "Please check the delivery address you entered."
Test: dev.domaincentric.sample.ecommerce.checkout.ChooseTheDeliveryAddressIntegrationTest#aStreetOfSpacesOnlyOpensTheDeliveryStepAgainWithTheRefusal

#### blank-city-is-refused
- Given a guest on the delivery step
- When the guest submits Street Address "Hauptstrasse 1", City "   " (spaces only), Postal Code "10115",
  Country "Germany" and "Standard Shipping"
- Then the delivery step opens again with the error "Please check the delivery address you entered."
Test: dev.domaincentric.sample.ecommerce.checkout.ChooseTheDeliveryAddressIntegrationTest#aCityOfSpacesOnlyOpensTheDeliveryStepAgainWithTheRefusal

#### blank-postal-code-is-refused
- Given a guest on the delivery step
- When the guest submits Street Address "Hauptstrasse 1", City "Berlin", Postal Code "   " (spaces only),
  Country "Germany" and "Standard Shipping"
- Then the delivery step opens again with the error "Please check the delivery address you entered."
Test: dev.domaincentric.sample.ecommerce.checkout.ChooseTheDeliveryAddressIntegrationTest#aPostalCodeOfSpacesOnlyOpensTheDeliveryStepAgainWithTheRefusal

#### blank-country-is-refused
- Given a guest on the delivery step
- When the guest submits Street Address "Hauptstrasse 1", City "Berlin", Postal Code "10115", Country "   "
  (spaces only) and "Standard Shipping"
- Then the delivery step opens again with the error "Please check the delivery address you entered."
Test: dev.domaincentric.sample.ecommerce.checkout.ChooseTheDeliveryAddressIntegrationTest#aCountryOfSpacesOnlyOpensTheDeliveryStepAgainWithTheRefusal

### Rule: The shipping price is the chosen method's price as the delivery step offers it, whatever the form carries

#### a-shipping-price-sent-with-the-form-is-not-taken
- Given a guest who started the checkout with "Domain-Driven Design" once and is on the delivery step
- When the delivery form is sent with Street Address "Hauptstrasse 1", City "Berlin", Postal Code "10115", Country
  "Germany" and "Overnight Shipping", carrying the shipping price "0.00 EUR"
- Then the payment step opens and shows Subtotal "54.99 EUR", Shipping "19.99 EUR" and Total "74.98 EUR"
Test: dev.domaincentric.sample.ecommerce.e2e.ChooseTheDeliveryAddressE2ETest#theShippingPriceIsTheOneTheDeliveryStepOffersWhateverTheFormCarries
