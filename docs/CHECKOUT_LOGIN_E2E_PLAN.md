# Checkout Fix, Login Integration, and E2E Tests Plan

## Overview

Fix the 403 error on checkout, add login/register options to checkout flow, and create Playwright E2E tests.

## Problem Analysis

**403 Error Root Cause**: Templates submit to `/checkout/{sessionId}/buyer` but controllers expect `/checkout/buyer` (without session ID). The controllers derive session from JWT identity, making session IDs in URLs unnecessary.

---

## Phase 1: Fix 403 Error on Checkout Templates

**Files to Modify:**

| File | Changes |
|------|---------|
| `templates/checkout/buyer.pug` | Line 19: Change form action to `/checkout/buyer` |
| `templates/checkout/delivery.pug` | Line 19: form action to `/checkout/delivery`; Line 65: back link to `/checkout/buyer` |
| `templates/checkout/payment.pug` | Line 19: form action to `/checkout/payment`; Line 38: back link to `/checkout/delivery` |
| `templates/checkout/review.pug` | Lines 24,39,45,66,72,85,116,117: Change all URLs from `/checkout/${session.sessionId()}/...` to `/checkout/...` |

---

## Phase 2: Login/Register Options on Buyer Page

**Concept**: Add options for "Continue as Guest" vs "Login" vs "Register" on buyer page.

**Modify `templates/checkout/buyer.pug`:**

Add auth options section before form:
```pug
//- Authentication Options
if !identity || identity.type().name() == 'ANONYMOUS'
  .auth-options
    h3 How would you like to checkout?
    .auth-buttons
      a.btn.btn-outline(href="/login?returnUrl=/checkout/buyer") Login
      a.btn.btn-outline(href="/register?returnUrl=/checkout/buyer") Register
    .divider
      span or continue as guest
else
  .welcome-back
    p Logged in as #{identity.email().orElse('')}
```

Pre-fill email from identity:
```pug
input#email(... value=(identity && identity.email().isPresent() ? identity.email().get() : (session.buyerInfo() ? session.buyerInfo().email() : '')))
```

**Modify `BuyerInfoPageController.java`:**
```java
// In showBuyerInfoForm() method, add:
model.addAttribute("identity", identity);
```

---

## Phase 3: Playwright Java E2E Tests

**Add to build.gradle:**
```groovy
// Apply e2e test configuration
apply from: 'gradle/test-e2e.gradle'
```

**Create gradle/test-e2e.gradle:**
```groovy
sourceSets {
    testE2e {
        java.srcDir 'src/test-e2e/java'
        resources.srcDir 'src/test-e2e/resources'
        compileClasspath += sourceSets.main.output
        runtimeClasspath += sourceSets.main.output
    }
}

configurations {
    testE2eImplementation.extendsFrom testImplementation
    testE2eRuntimeOnly.extendsFrom testRuntimeOnly
}

dependencies {
    testE2eImplementation 'com.microsoft.playwright:playwright:1.40.0'
}

tasks.register('test-e2e', Test) {
    description = 'Runs E2E tests with Playwright'
    group = 'verification'
    testClassesDirs = sourceSets.testE2e.output.classesDirs
    classpath = sourceSets.testE2e.runtimeClasspath
    useJUnitPlatform()

    // Run headed for debugging
    systemProperty 'playwright.headed', System.getProperty('playwright.headed', 'false')
}
```

**Create directory structure:**
```
src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/
├── CheckoutGuestE2ETest.java
├── CheckoutLoginE2ETest.java
└── BaseE2ETest.java
```

**BaseE2ETest.java:**
```java
package dev.domaincentric.sample.ecommerce.e2e;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

public abstract class BaseE2ETest {
    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;

    protected static final String BASE_URL = "http://localhost:8080";

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        boolean headed = Boolean.getBoolean("playwright.headed");
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(!headed)
        );
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void createContext() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }
}
```

**Test scenarios:**
1. `CheckoutGuestE2ETest.java` - Full guest checkout flow
2. `CheckoutLoginE2ETest.java` - Register during checkout, email pre-fill

---

## Phase 4: IntelliJ Run Configurations

**Create `.run/E2E Tests.run.xml`:**
```xml
<component name="ProjectRunConfigurationManager">
  <configuration name="E2E Tests" type="GradleRunConfiguration" factoryName="Gradle">
    <ExternalSystemSettings>
      <option name="executionName" />
      <option name="externalProjectPath" value="$PROJECT_DIR$" />
      <option name="externalSystemIdString" value="GRADLE" />
      <option name="scriptParameters" value="-Dplaywright.headed=true" />
      <option name="taskDescriptions"><list /></option>
      <option name="taskNames"><list><option value="test-e2e" /></list></option>
      <option name="vmOptions" />
    </ExternalSystemSettings>
    <method v="2" />
  </configuration>
</component>
```

**Create `.run/E2E Tests (Headless).run.xml`:**
```xml
<component name="ProjectRunConfigurationManager">
  <configuration name="E2E Tests (Headless)" type="GradleRunConfiguration" factoryName="Gradle">
    <ExternalSystemSettings>
      <option name="executionName" />
      <option name="externalProjectPath" value="$PROJECT_DIR$" />
      <option name="externalSystemIdString" value="GRADLE" />
      <option name="scriptParameters" value="" />
      <option name="taskDescriptions"><list /></option>
      <option name="taskNames"><list><option value="test-e2e" /></list></option>
      <option name="vmOptions" />
    </ExternalSystemSettings>
    <method v="2" />
  </configuration>
</component>
```

**Note:** App must be running before E2E tests. Start with `./gradlew bootRun` or "EcommerceSampleApplication" run config.

---

## Critical Files

| Path | Action |
|------|--------|
| `src/main/resources/templates/checkout/buyer.pug` | Fix URL + add auth options |
| `src/main/resources/templates/checkout/delivery.pug` | Fix URLs |
| `src/main/resources/templates/checkout/payment.pug` | Fix URLs |
| `src/main/resources/templates/checkout/review.pug` | Fix URLs (8 occurrences) |
| `checkout/adapter/incoming/web/checkoutcompletion/BuyerInfoPageController.java` | Add identity to model |
| `build.gradle` | Apply test-e2e.gradle |
| `gradle/test-e2e.gradle` | New - Playwright Java config |
| `src/test-e2e/java/.../e2e/` | New - E2E test classes |
| `.run/E2E Tests.run.xml` | IntelliJ Gradle run config |

---

## Verification

1. **Build**: `./gradlew build`
2. **Manual test**:
   - Start app: `./gradlew bootRun`
   - Add product to cart
   - Click checkout → should go to `/checkout/buyer` (no session ID)
   - Fill form → click Continue to Delivery → should NOT get 403
   - Complete full checkout flow
3. **Auth flow test**:
   - As anonymous, go to buyer page → see Login/Register options
   - Click Login → redirect to `/login?returnUrl=/checkout/buyer`
   - Login → return to buyer page with email pre-filled
4. **E2E tests**:
   ```bash
   # First, start the application
   ./gradlew bootRun &

   # Then run E2E tests (headed)
   ./gradlew test-e2e -Dplaywright.headed=true

   # Or headless
   ./gradlew test-e2e
   ```
