@CaseKaro
Feature: CaseKaro E-Commerce Product and Cart Validation

  Background:
    Given the user has opened the CaseKaro website

  @SmokeTest
  Scenario: Navigate to Mobile Covers page
    When the user navigates to the Mobile Covers by Model page
    Then the page title should contain "Phone Cases"

  @SearchTest
  Scenario: Search for Apple brand products
    When the user searches for "Apple"
    Then the search results should display Apple products
    And the search results should not display "Samsung" brand

  @ProductTest
  Scenario: Open iPhone 16 Pro product page
    When the user searches for "iPhone 16 Pro"
    And the user opens the product "shield-iphone-16-pro-back-cover"
    Then the product page title should contain "iPhone 16 Pro"
    And the product should have material options available

  @CartTest
  Scenario Outline: Add product with different materials to cart
    When the user navigates to product "shield-iphone-16-pro-back-cover"
    And the user selects material "<material>"
    And the user clicks Add to Cart
    Then the cart notification should appear

    Examples:
      | material |
      | Soft     |
      | Glass    |
      | Hard     |

  @CartValidationTest
  Scenario: Validate cart contains all 3 material variants
    Given the user has added all 3 material variants to the cart
    When the user opens the cart page
    Then the cart should contain exactly 3 items
    And each cart item should display a product name
    And each cart item should display a price
    And each cart item should have a product link
