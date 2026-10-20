# cart — cartoversight

### Rule: Only staff list every cart; a stranger is challenged, a customer is forbidden

#### staff-list-every-cart
- Given Ada's cart holds "Domain-Driven Design" × 2 and a client holding a staff token
- When it sends `GET /api/carts`
- Then the shop answers 200 with `carts`, one of which has Ada's `cartId`, her `customerId`, `status` "ACTIVE", `itemCount` 1, `total` 109.98 and `currency` "EUR"
Test: dev.domaincentric.sample.ecommerce.e2e.ShoppingCartApiE2ETest#staffSeeEveryCartWithItsOwnerStatusAndTotal

#### a-stranger-cannot-list-carts
- Given a client without a token
- When it sends `GET /api/carts`
- Then the shop answers 401 with the header `WWW-Authenticate: Bearer`
Test: dev.domaincentric.sample.ecommerce.api.ApiAuthorizationIntegrationTest#listingEveryCartNeedsStaff

#### a-customer-cannot-list-carts
- Given a client holding the customer token of Ada
- When it sends `GET /api/carts`
- Then the shop answers 403
Test: dev.domaincentric.sample.ecommerce.api.ApiAuthorizationIntegrationTest#listingEveryCartNeedsStaff
