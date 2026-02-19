
# CaseKaro Automation — BDD + POM Framework

## Project Structure

```
CasekaroAutomation/
├── pom.xml
└── src/
    └── test/
        ├── java/
        │   ├── hooks/
        │   │   └── Hooks.java               ← Browser setup & teardown
        │   ├── pages/
        │   │   ├── HomePage.java            ← Page Object: Home
        │   │   ├── MobileCoversPage.java    ← Page Object: Mobile Covers
        │   │   ├── SearchPage.java          ← Page Object: Search Results
        │   │   ├── ProductPage.java         ← Page Object: Product Detail
        │   │   └── CartPage.java            ← Page Object: Cart
        │   ├── runner/
        │   │   └── TestRunner.java          ← Cucumber JUnit runner
        │   ├── stepdefinitions/
        │   │   └── CaseKaroSteps.java       ← Cucumber step definitions
        │   └── utils/
        │       └── PlaywrightManager.java   ← Browser lifecycle utility
        └── resources/
            └── features/
                └── CaseKaro.feature         ← BDD Feature file
```

## Technology Stack

| Tool         | Version  | Purpose                        |
|--------------|----------|--------------------------------|
| Java         | 11+      | Language                       |
| Maven        | 3.8+     | Build & dependency management  |
| Playwright   | 1.44.0   | Browser automation             |
| Cucumber     | 7.18.0   | BDD framework                  |
| JUnit 4      | 4.13.2   | Test runner                    |
| AssertJ      | 3.25.3   | Fluent assertions              |

## Changes Made

### 1. Feature File Added
- `CaseKaro.feature` written in Gherkin (Given/When/Then)
- Covers: Home, Mobile Covers, Search, Product, Cart scenarios
- Uses `Scenario Outline` for the 3 material variants

### 2. Page Object Model (POM)
- `HomePage` — navigation and logo validation
- `MobileCoversPage` — title and heading assertions
- `SearchPage` — search by query, product count, brand presence
- `ProductPage` — material selection, add to cart
- `CartPage` — item count, price, material, link extraction

### 3. Assertions Added (AssertJ)
- URL validation on home page load
- Page title containment checks
- Search result count > 0
- Negative check: Samsung NOT in Apple results
- Cart item count == 3
- SoftAssertions for price, name, link on each cart item

## How to Run

### Prerequisites
1. Java 11+
2. Maven 3.8+

### Install Playwright browsers (first time only)
```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
```

### Run all tests
```bash
mvn test
```

### Run specific tag
```bash
mvn test -Dcucumber.filter.tags="@CartValidationTest"
```

## Available Tags
- `@CaseKaro` — all tests
- `@SmokeTest` — home & mobile covers
- `@SearchTest` — Apple search & negative validation
- `@ProductTest` — product page
- `@CartTest` — add to cart (outline)
- `@CartValidationTest` — full cart validation
