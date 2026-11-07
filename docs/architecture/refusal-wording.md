# Refusal wording

Every sentence a person or an API client reads when the shop refuses something, and where it comes from. The
incoming adapter words each one from the refusal's case and values — a use case returns its expected refusals as
cases of a closed result (ADR-047), named like the failure without `Exception` — or, for what is still thrown, from
the exception's type; an exception's message is for the log and never reaches a page or a problem document. A use
case result names what happened (a case, a broken rule), never in words. Both shops — this one and its twin in the other language — carry this file unchanged and
produce exactly these sentences, so the two can be compared line by line.

`{x}` is a value the refusal carries. A sentence without a closing period is one a test or a shared scenario
names as it is.

## API problem documents

The `title` names the kind of refusal, the `detail` says it with the failure's values. A catch-all answers with
its title alone. A refused request field adds `errors`: each field as the request spells it, with its reason.

| Context | Failure | Status | Title | Detail |
|---|---|---|---|---|
| Account | login refused: `CredentialsRefused`, `AccountNotAccessible` (answered with `WWW-Authenticate: Bearer`) | 401 | Authentication failed | — |
| Account | `EmailAlreadyRegistered` | 409 | Email already registered | The email address {email} is already registered. |
| Account | `AccountAlreadyExists` | 409 | Account already exists | The signed-in user already has an account. |
| Account | `PasswordTooWeak` | 422 | Password too weak | the sentence for its broken rule, see [Password rules](#password-rules) |
| Account | `AccountClosedException` (thrown) | 409 | Account closed | Account {accountId} is closed. |
| Account | `AccountNotAccessibleException` (thrown) | 403 | Account not accessible | This account cannot sign in. |
| Account | any other `DomainException` | 422 | Business rule violated | — |
| Account | a refused request field | 400 | Unacceptable value | — (`errors` names each field and why) |
| Account | argument exception | 400 | Unacceptable value | — |
| Cart | `CartNotFound` | 404 | Cart not found | There is no cart {cartId}. |
| Cart | `ArticleNotAvailable` | 422 | Article not available | There is no article for product {productId}. |
| Cart | `CartItemNotFound` | 404 | Position not in cart | The cart holds no position for {reference}. |
| Cart | `InsufficientArticleStock` | 409 | Not enough stock | Requested {requested} of product {productId}, more than is in stock. |
| Cart | `ActiveCartAlreadyExists` | 409 | Active cart already exists | Customer {customerId} already has an active cart. |
| Cart | `CartNotModifiable`, any other `DomainException` | 409 | Cart refuses the change | — |
| Cart | a refused request field | 400 | Unacceptable value | — (`errors` names each field and why) |
| Cart | argument exception | 400 | Unacceptable value | — |
| Product | `DuplicateSku` | 409 | Stock keeping unit already in use | A product with SKU {sku} already exists. |
| Product | any other `DomainException` | 422 | Business rule violated | — |
| Product | a refused request field | 400 | Unacceptable value | — (`errors` names each field and why) |
| Product | argument exception | 400 | Unacceptable value | — |
| Pricing | `ProductPriceNotFound` | 404 | Product not found | There is no product {productId}. |
| Pricing | a refused request field | 400 | Unacceptable value | — (`errors` names each field and why) |
| Pricing | argument exception | 400 | Unacceptable value | — |

## Pages

### Password rules

One sentence per part of the password policy (`PasswordRule`), used by the register page, the change-password page
and the account API alike — the rule, never the attempt.

| Broken rule | Sentence |
|---|---|
| too short | Password must be at least {MinLength} characters long. |
| too long | Password must not be longer than {MaxByteLength} bytes. |
| no uppercase letter | Password must contain an uppercase letter. |
| no lowercase letter | Password must contain a lowercase letter. |
| no digit | Password must contain a digit. |

### Login (`/account/login`)

| Refusal | Sentence |
|---|---|
| `CredentialsRefused` — wrong address or password | Invalid email or password |
| `AccountNotAccessible` — an account that cannot sign in | Account is {status} |
| `AccountNotAccessibleException` (thrown) | This account cannot sign in. |
| argument exception | Please enter your email and your password. |
| any other refusal | Signing in failed. Please try again. |

### Register (`/account/register`)

| Refusal | Sentence |
|---|---|
| confirmation differs (checked by the page) | Passwords do not match |
| password shorter than the minimum (checked by the page) | Password must be at least 8 characters |
| `EmailAlreadyRegistered` | This email address is already registered |
| `AccountAlreadyExists` | You already have an account. |
| `PasswordTooWeak` | the sentence for its broken rule, see [Password rules](#password-rules) |
| argument exception | Please check the email address, your name and your date of birth. |
| any other refusal | The account could not be created. Please try again. |

### Change password (`/account/change-password`)

| Refusal | Sentence |
|---|---|
| confirmation differs (checked by the page) | New password and confirmation do not match |
| `CurrentPasswordInvalid` | Current password is not correct |
| `NewPasswordRejected` | the sentence for the broken rule the case names, see [Password rules](#password-rules) |

### Profile (`/account/profile`)

| Refusal | Sentence |
|---|---|
| date that cannot be read (checked by the page) | Please enter a date like 1990-05-17 |
| `EmailAlreadyInUse` | This email address is already registered |
| `EmailInvalid` | Please enter a valid email address. |
| `DateOfBirthInvalid` | Please enter a date of birth that is not in the future. |

### Cart (`/cart/add-product`)

| Refusal | Sentence |
|---|---|
| `InsufficientArticleStock` | There is not enough stock to add {requested} of this product — your cart already holds {held}, and {inStock} are in stock. |
| `ArticleNotAvailable` | This product is no longer available. |
| `CartNotFound` | Your cart could not be found. |
| `CartNotModifiable` | This cart can no longer be changed. |
| argument exception | Please enter a valid quantity. |
| any other refusal | The product could not be added to the cart. Please try again. |

### Cart merge (`/cart/merge`)

| Refusal | Sentence |
|---|---|
| unknown merge option | Invalid merge option selected |

### Checkout start (`/checkout/start`)

| Refusal | Sentence |
|---|---|
| `CartNotAvailable` | Your cart could not be found. |
| `CartNotActive` | This cart can no longer be checked out. |
| `EmptyCart` (the cart is empty, or the checkout it would start holds no line) | Cannot check out an empty cart. |
| `ArticleNotAvailable` | A product in your cart is no longer available. |
| `CheckoutItemsUnavailable` | Cannot start checkout, {count} item(s) unavailable or out of stock. |
| any other refusal | The checkout could not be started. Please try again. |

### Checkout steps

Every step:

| Refusal | Sentence |
|---|---|
| no active checkout session (checked by the page) | No active checkout session found |
| `CheckoutSessionNotFound` | Checkout session not found |
| `CheckoutNotModifiable` | This checkout can no longer be changed. |

Buyer, delivery and payment step:

| Refusal | Sentence |
|---|---|
| `CheckoutStepNotCompleted`, `CheckoutStepOutOfOrder` | Please complete the previous checkout steps first. |

On the delivery and payment step, and on placing the order, this sentence is shown on the step that is still open:
the refused submission asks the domain which step that is and sends the guest there directly. The redirect hands it
the code `earlier-steps-first` in the sealed notice cookie, addressed to that step's page — not in the address and
not in the session, so any instance of the shop that answers can show it. A step that may not be shown passes no
notice on; it only redirects. Only a known code is shown; any other code is ignored.

| Step | Argument exception | Any other refusal |
|---|---|---|
| buyer (`/checkout/buyer`) | Please check the details you entered. | Your details could not be saved. Please try again. |
| delivery (`/checkout/delivery`) | Please check the delivery address you entered. | Your delivery details could not be saved. Please try again. |
| payment (`/checkout/payment`) | Please choose a way to pay. | Your payment selection could not be saved. Please try again. |

Delivery step:

| Refusal | Sentence |
|---|---|
| `ShippingOptionNotOffered` | This shipping method is not offered. Please choose another one. |

Payment step:

| Refusal | Sentence |
|---|---|
| `PaymentProviderUnavailable` (.NET shop: the step asks whether the provider takes payments; the Java shop's step asks the provider nothing) | The payment provider is not available right now. Please try again later. |
| `PaymentProviderNotFound` | This way to pay is not offered. Please choose another one. |
| `NothingToPay` | There is nothing to pay for this order. |

Placing the order (`/checkout/confirm`):

| Refusal | Sentence |
|---|---|
| `CheckoutValidation` with a line unavailable or short of stock — at the check or at the reservation | Not enough stock left for '{product}'. Please adjust your order. (the first such line, in line order) |
| `PriceChangedSinceReview` — a line's price changed since the review | The price of '{product}' has changed. Please check your order and place it again. |
| `OrderChangedSinceReview` — anything else changed since the review | Your order has changed since you reviewed it. Please check it and place it again. |
| `CheckoutStepNotCompleted` | Please complete the previous checkout steps first. |
| `CheckoutStepOutOfOrder` | Please review your order before placing it. |
| `PaymentInitiationFailed` | The payment was refused. Please choose another way to pay. |
| `PaymentProviderUnavailable` | The payment provider is not available right now. Please try again later. |
| `PaymentProviderNotFound` (Java shop; the .NET shop answers it as any other refusal) | This way to pay is not offered. Please choose another one. |
| not sent from the review's own form (checked by the page, Java shop) | Please review your order before placing it. — or, while an earlier step is still missing, Please complete the previous checkout steps first. on that step |
| any other refusal | Your order could not be placed. Please try again. |

Confirmation (`/checkout/confirmation`):

| Refusal | Sentence |
|---|---|
| no confirmed order (checked by the page) | No confirmed order found |

Two guards exist only where each step has a controller of its own (the Java shop): a session that is gone when
the page reads it a second time answers "Checkout session not found", and a confirmation page opened before the
order is placed answers "Order has not been confirmed yet". The single checkout controller of the .NET shop reads
the session once and sends a step it may not show to the step it may, without a sentence.
