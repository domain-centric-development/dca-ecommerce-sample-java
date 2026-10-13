# account

### Rule: Out of the box, a page on the shop's own host may frame the shop and use it

#### a-page-on-another-port-embeds-the-shop
- Given the shop reached as `localhost` and a page served from another port of `localhost` that frames the catalogue
- When the visitor opens the first product inside the frame and presses "Add to Cart"
- Then the framed shop's cart page shows the product
Test: dev.domaincentric.sample.ecommerce.e2e.ShopInAFrameE2ETest#framesTheShopFromAnotherPortAndAddsToTheCart

### Rule: Out of the box, a page on another site cannot use the framed shop

#### a-foreign-site-cannot-add-to-the-cart
- Given the shop reached as `localhost` and a page on `127.0.0.1` that frames the catalogue
- When the visitor opens the first product inside the frame and presses "Add to Cart"
- Then the product does not reach the framed shop's cart
Test: dev.domaincentric.sample.ecommerce.e2e.ShopInAFrameE2ETest#aForeignSiteCannotAddToTheCart

### Rule: A shop started for cross-site embedding works in a foreign site's frame

#### a-shop-started-for-embedding-works-cross-site
- Given a shop started for cross-site embedding, so that its cookies are sent as `SameSite=None; Secure`, reached as `localhost`, and a page on `127.0.0.1` that frames the catalogue
- When the visitor opens the first product inside the frame and presses "Add to Cart"
- Then the framed shop's cart page shows the product
Test: dev.domaincentric.sample.ecommerce.e2e.ShopInAFrameE2ETest#aShopStartedForEmbeddingWorksCrossSite

### Rule: A shop started with framing switched off cannot be framed by any other page

#### a-shop-started-without-framing-refuses-the-frame
- Given a shop started with framing switched off and a page on another port of the shop's host that frames the catalogue
- When the visitor opens that page
- Then the shop's pages answer with `X-Frame-Options: SAMEORIGIN` and the browser shows nothing of the shop inside the frame
Test: dev.domaincentric.sample.ecommerce.e2e.ShopInAFrameE2ETest#aShopStartedWithoutFramingRefusesTheFrame

### Rule: A form post without the token of the shop's page is refused with 403

#### a-login-post-without-the-form-token-is-refused
- Given an account "ada@example.com" with password "Secret123"
- When a browser posts email "ada@example.com" and password "Secret123" to `/login` without the form token the login page carries
- Then the shop answers 403
- And the visitor is not logged in: the header still shows "Login" and "Register"
Test: dev.domaincentric.sample.ecommerce.e2e.FormTokenE2ETest#aLoginPostWithoutTheFormTokenLeavesTheVisitorLoggedOut

#### a-registration-post-without-the-form-token-is-refused
- Given no account exists for "grace@example.com"
- When a browser posts a complete, valid registration for "grace@example.com" to `/register` without the form token
- Then the shop answers 403
- And logging in as "grace@example.com" afterwards fails with "Invalid email or password"
Test: dev.domaincentric.sample.ecommerce.account.FormTokenIntegrationTest#aRegistrationPostWithoutTheFormTokenCreatesNoAccount

#### a-logout-post-without-the-form-token-is-refused
- Given a customer logged in as "ada@example.com"
- When a post to `/logout` arrives from her browser without the form token
- Then the shop answers 403
- And the header still shows "Welcome, ada@example.com"
Test: dev.domaincentric.sample.ecommerce.account.FormTokenIntegrationTest#aLogoutPostWithoutTheFormTokenKeepsTheCustomerLoggedIn
