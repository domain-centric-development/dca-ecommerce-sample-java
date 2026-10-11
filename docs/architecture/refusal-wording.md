# Refusal wording

Every sentence a person or an API client reads when the shop refuses something, and where it comes from. The
incoming adapter words each one from the failure's type and values; the exception's message is for the log and
never reaches a page or a problem document. A use case result names what happened (an outcome, a broken rule),
never in words. Both shops — this one and its twin in the other language — carry this file unchanged and
produce exactly these sentences, so the two can be compared line by line.

`{x}` is a value the failure carries. A sentence without a closing period is one a test or a shared scenario
names as it is.

## API problem documents

The `title` names the kind of refusal, the `detail` says it with the failure's values. A catch-all answers with
its title alone.

| Context | Failure | Status | Title | Detail |
|---|---|---|---|---|
| Account | `EmailAlreadyRegisteredException` | 409 | Email already registered | The email address {email} is already registered. |
| Account | `AccountAlreadyExistsException` | 409 | Account already exists | The signed-in user already has an account. |
| Account | `PasswordTooWeakException` | 422 | Password too weak | the sentence for its broken rule, see [Password rules](#password-rules) |
| Account | `AccountClosedException` | 409 | Account closed | Account {accountId} is closed. |
| Account | `AccountNotAccessibleException` | 403 | Account not accessible | This account cannot sign in. |
| Account | any other `UseCaseException` | 422 | Request cannot be served | — |
| Account | any other `DomainException` | 422 | Business rule violated | — |
| Account | argument exception | 400 | Unacceptable value | — |
| Cart | `CartNotFoundException` | 404 | Cart not found | There is no cart {cartId}. |
| Cart | `ArticleNotAvailableException` | 404 | Article not available | There is no article for product {productId}. |
| Cart | `CartItemNotFoundException` | 404 | Position not in cart | The cart holds no position for {reference}. |
| Cart | `InsufficientArticleStockException` | 409 | Not enough stock | Requested {requested} of product {productId}, more than is in stock. |
| Cart | `ActiveCartAlreadyExistsException` | 409 | Active cart already exists | Customer {customerId} already has an active cart. |
| Cart | any other `UseCaseException` | 422 | Request cannot be served | — |
| Cart | any other `DomainException` | 409 | Cart refuses the change | — |
| Cart | argument exception | 400 | Unacceptable value | — |
| Product | `DuplicateSkuException` | 409 | Stock keeping unit already in use | A product with SKU {sku} already exists. |
| Product | any other `UseCaseException` | 422 | Request cannot be served | — |
| Product | any other `DomainException` | 422 | Business rule violated | — |
| Product | argument exception | 400 | Unacceptable value | — |

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
| `AccountNotAccessibleException` | This account cannot sign in. |
| argument exception | Please enter your email and your password. |
| any other refusal | Signing in failed. Please try again. |
| wrong address or password (worded by the authentication use case) | Invalid email or password |
| account that cannot sign in (worded by the authentication use case) | Account is {status} |

### Register (`/account/register`)

| Refusal | Sentence |
|---|---|
| confirmation differs (checked by the page) | Passwords do not match |
| password shorter than the minimum (checked by the page) | Password must be at least 8 characters |
| `EmailAlreadyRegisteredException` | This email address is already registered |
| `AccountAlreadyExistsException` | You already have an account. |
| `PasswordTooWeakException` | the sentence for its broken rule, see [Password rules](#password-rules) |
| argument exception | Please check the email address, your name and your date of birth. |
| any other refusal | The account could not be created. Please try again. |

### Change password (`/account/change-password`)

| Refusal | Sentence |
|---|---|
| confirmation differs (checked by the page) | New password and confirmation do not match |
| outcome: current password invalid | Current password is not correct |
| outcome: new password rejected | the sentence for the broken rule the result names, see [Password rules](#password-rules) |

### Profile (`/account/profile`)

| Refusal | Sentence |
|---|---|
| date that cannot be read (checked by the page) | Please enter a date like 1990-05-17 |
| outcome: email already in use | This email address is already registered |
| outcome: email invalid | Please enter a valid email address. |
| outcome: date of birth invalid | Please enter a date of birth that is not in the future. |

### Cart (`/cart/add-product`)

| Refusal | Sentence |
|---|---|
| `InsufficientArticleStockException` | There is not enough stock to add {requested} of this product. |
| `ArticleNotAvailableException` | This product is no longer available. |
| `CartNotFoundException` | Your cart could not be found. |
| `CartNotModifiableException` | This cart can no longer be changed. |
| argument exception | Please enter a valid quantity. |
| any other refusal | The product could not be added to the cart. Please try again. |

### Cart merge (`/cart/merge`)

| Refusal | Sentence |
|---|---|
| unknown merge option | Invalid merge option selected |

### Checkout start (`/checkout/start`)

| Refusal | Sentence |
|---|---|
| `CartNotAvailableException` | Your cart could not be found. |
| `CartNotActiveException` | This cart can no longer be checked out. |
| `EmptyCartException`, `EmptyCheckoutException` | Cannot check out an empty cart. |
| `ArticleNotAvailableException` | A product in your cart is no longer available. |
| `CheckoutItemsUnavailableException` | Cannot start checkout, {count} item(s) unavailable or out of stock. |
| any other refusal | The checkout could not be started. Please try again. |

### Checkout steps

Every step:

| Refusal | Sentence |
|---|---|
| no active checkout session (checked by the page) | No active checkout session found |
| `CheckoutSessionNotFoundException` | Checkout session not found |
| `CheckoutNotModifiableException` | This checkout can no longer be changed. |

Buyer, delivery and payment step:

| Refusal | Sentence |
|---|---|
| `CheckoutStepNotCompletedException`, `CheckoutStepOutOfOrderException` | Please complete the previous checkout steps first. |

| Step | Argument exception | Any other refusal |
|---|---|---|
| buyer (`/checkout/buyer`) | Please check the details you entered. | Your details could not be saved. Please try again. |
| delivery (`/checkout/delivery`) | Please check the delivery address you entered. | Your delivery details could not be saved. Please try again. |
| payment (`/checkout/payment`) | Please choose a way to pay. | Your payment selection could not be saved. Please try again. |

Payment step:

| Refusal | Sentence |
|---|---|
| `PaymentInitiationFailedException` | The payment was refused. Please choose another way to pay. |
| `PaymentProviderUnavailableException` | The payment provider is not available right now. Please try again later. |
| `PaymentProviderNotFoundException` | This way to pay is not offered. Please choose another one. |
| `NothingToPayException` | There is nothing to pay for this order. |

Placing the order (`/checkout/confirm`):

| Refusal | Sentence |
|---|---|
| `CheckoutValidationException` | Cannot place the order, {count} item(s) unavailable or out of stock. |
| `CheckoutStepNotCompletedException` | Please complete the previous checkout steps first. |
| `CheckoutStepOutOfOrderException` | Please review your order before placing it. |
| any other refusal | Your order could not be placed. Please try again. |

Confirmation (`/checkout/confirmation`):

| Refusal | Sentence |
|---|---|
| no confirmed order (checked by the page) | No confirmed order found |

Two guards exist only where each step has a controller of its own (the Java shop): a session that is gone when
the page reads it a second time answers "Checkout session not found", and a confirmation page opened before the
order is placed answers "Order has not been confirmed yet". The single checkout controller of the .NET shop reads
the session once and sends a step it may not show to the step it may, without a sentence.
