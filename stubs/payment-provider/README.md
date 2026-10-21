# The payment provider's stub

What the shop talks to when it is started against a payment provider on this machine: WireMock with one set of
answers per behaviour, the same in both samples. `PAYMENT_STUB` picks the set.

The shop sends `POST /payments` when the customer presses "Place Order" on the review; choosing how to pay on the
payment step sends nothing.

| `PAYMENT_STUB` | `POST /payments` answers | What the checkout does |
|---|---|---|
| `authorize` (default) | `201` with a payment reference | places the order and opens the confirmation |
| `refuse` | `402` | stays at the review: "The payment was refused. Please choose another way to pay." |
| `slow` | `201` after 5 seconds | stays at the review after 2 seconds: "The payment provider is not available right now. Please try again later." |

The contract is the one `project/tech.md` names under `## Integrations`. Every set answers only a request of that
shape — the amount a decimal string with two places, the currency three capitals, the merchant reference a string
naming the checkout; anything else gets WireMock's
`404`, which the shop treats as an unavailable provider.
