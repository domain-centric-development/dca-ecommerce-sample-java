# backoffice — access

### Rule: The back office has its own login page

#### backoffice-login-page
- Given a browser with no cookies
- When the operator opens `/backoffice/login`
- Then the page heading reads "Backoffice Login"
- And the form has the fields "Username" and "Password" and the button "Login"
Test: dev.domaincentric.sample.ecommerce.backoffice.BackofficeLoginPageIntegrationTest#loginPageShowsTheUsernameAndPasswordForm

### Rule: A successful operator login opens the event publication log

#### login-opens-the-event-log
- Given the backoffice login page and the demo operator credentials "admin" / "admin" of a development run
- When the operator logs in with username "admin" and password "admin"
- Then the event log `/backoffice/events` opens with the heading "Event Publication Log"
- And its summary shows the counters "Total Events", "Completed" and "Incomplete", the total not negative
Test: dev.domaincentric.sample.ecommerce.e2e.BackofficeLoginE2ETest#loginOpensTheEventPublicationLog

### Rule: Wrong credentials keep the operator on the login page with one message

#### wrong-password-is-refused
- Given the backoffice login page
- When the operator logs in with username "admin" and password "wrong"
- Then the login page `/backoffice/login?error=true` shows "Invalid username or password."
Test: dev.domaincentric.sample.ecommerce.backoffice.BackofficeLoginPageIntegrationTest#wrongPasswordKeepsTheOperatorOnTheLoginPageWithOneMessage

#### unknown-username-is-refused
- Given the backoffice login page
- When the operator logs in with username "operator" and password "admin"
- Then the login page shows "Invalid username or password."
Test: dev.domaincentric.sample.ecommerce.backoffice.BackofficeLoginPageIntegrationTest#unknownUsernameKeepsTheOperatorOnTheLoginPageWithOneMessage
