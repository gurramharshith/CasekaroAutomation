package stepdefinitions;

import io.cucumber.java.en.*;
import org.assertj.core.api.SoftAssertions;
import pages.*;
import utils.PlaywrightManager;
import com.microsoft.playwright.Page;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseKaroSteps {

    private Page page;
    private HomePage homePage;
    private MobileCoversPage mobileCoversPage;
    private SearchPage searchPage;
    private ProductPage productPage;
    private CartPage cartPage;

    // ─── Background ──────────────────────────────────────────────────────────

    @Given("the user has opened the CaseKaro website")
    public void theUserHasOpenedTheCaseKaroWebsite() {
        page             = PlaywrightManager.getPage();
        homePage         = new HomePage(page);
        mobileCoversPage = new MobileCoversPage(page);
        searchPage       = new SearchPage(page);
        productPage      = new ProductPage(page);
        cartPage         = new CartPage(page);

        homePage.navigateTo();
        System.out.println("Home page opened: " + homePage.getTitle());

        assertThat(page.url())
                .as("Home page URL should contain casekaro.com")
                .contains("casekaro.com");
    }

    // ─── Mobile Covers Page ───────────────────────────────────────────────────

    @When("the user navigates to the Mobile Covers by Model page")
    public void theUserNavigatesToMobileCoversPage() {
        mobileCoversPage.navigateTo();
        System.out.println("Mobile Covers page opened: " + mobileCoversPage.getTitle());
    }

    @Then("the page title should contain {string}")
    public void thePageTitleShouldContain(String expectedTitleFragment) {
        String actualTitle = mobileCoversPage.getTitle();
        assertThat(actualTitle)
                .as("Page title should contain: " + expectedTitleFragment)
                .containsIgnoringCase(expectedTitleFragment);
        System.out.println("✓ Page title assertion passed: " + actualTitle);
    }

    // ─── Search ───────────────────────────────────────────────────────────────

    @When("the user searches for {string}")
    public void theUserSearchesFor(String query) {
        searchPage.searchFor(query);
        System.out.println("Search completed for: " + query);
    }

    @Then("the search results should display Apple products")
    public void theSearchResultsShouldDisplayAppleProducts() {
        assertThat(searchPage.getProductCount())
                .as("Search results should contain at least one product for Apple")
                .isGreaterThan(0);
        System.out.println("✓ Apple search results count: " + searchPage.getProductCount());
    }

    @Then("the search results should not display {string} brand")
    public void theSearchResultsShouldNotDisplayBrand(String brand) {
        // Only check product TITLE/HEADING elements — descriptions say "Compatible with Samsung"
        // even on Apple pages, so checking full card text gives false positives
        boolean brandInTitles = searchPage.isBrandNameInProductTitles(brand);
        assertThat(brandInTitles)
                .as("Brand '" + brand + "' should NOT appear in product titles on Apple search results")
                .isFalse();
        System.out.println("✓ Negative validation passed: '" + brand + "' not found in product titles");
    }

    // ─── Product Page ─────────────────────────────────────────────────────────

    @When("the user opens the product {string}")
    public void theUserOpensTheProduct(String productHandle) {
        productPage.navigateTo(productHandle);
        System.out.println("Product page opened: " + productPage.getPageTitle());
    }

    @Then("the product page title should contain {string}")
    public void theProductPageTitleShouldContain(String expectedFragment) {
        String actualTitle = productPage.getPageTitle();
        assertThat(actualTitle)
                .as("Product page title should contain: " + expectedFragment)
                .containsIgnoringCase(expectedFragment);
        System.out.println("✓ Product title assertion passed: " + actualTitle);
    }

    @Then("the product should have material options available")
    public void theProductShouldHaveMaterialOptions() {
        assertThat(productPage.hasMaterialOptions())
                .as("Product page should have material options (fieldset)")
                .isTrue();
        System.out.println("✓ Material options are available on the product page");
    }

    // ─── Add to Cart (Scenario Outline) ──────────────────────────────────────

    @When("the user navigates to product {string}")
    public void theUserNavigatesToProduct(String productHandle) {
        productPage.navigateTo(productHandle);
        System.out.println("Navigated to product: " + productHandle);
    }

    @When("the user selects material {string}")
    public void theUserSelectsMaterial(String material) {
        productPage.selectMaterial(material);
        System.out.println("Selected material: " + material);
    }

    @When("the user clicks Add to Cart")
    public void theUserClicksAddToCart() {
        productPage.clickAddToCart();
        System.out.println("Add to Cart clicked");
    }

    @Then("the cart notification should appear")
    public void theCartNotificationShouldAppear() {
        // Cart notification or updated cart count should be present
        assertThat(productPage.isCartNotificationVisible())
                .as("Cart notification or cart icon should be visible after adding item")
                .isTrue();
        productPage.closeCartNotification();
        System.out.println("✓ Cart notification appeared and dismissed");
    }

    // ─── Cart Validation ──────────────────────────────────────────────────────

    @Given("the user has added all 3 material variants to the cart")
    public void theUserHasAddedAll3MaterialVariants() {
        // Clear any leftover cart items from previous scenarios in this test run
        // Shopify's server-side cart persists across browser sessions on same network
        cartPage.clearCart();

        String[] materials = {"Soft", "Glass", "Hard"};
        for (String material : materials) {
            productPage.navigateTo("shield-iphone-16-pro-back-cover");
            productPage.selectMaterial(material);
            productPage.clickAddToCart();
            productPage.closeCartNotification();
            System.out.println("Added to cart: " + material);
        }
        System.out.println("All 3 material variants added to cart");
    }

    @When("the user opens the cart page")
    public void theUserOpensTheCartPage() {
        // Wait a moment to let Shopify sync the cart server-side before we query
        page.waitForTimeout(1500);
        cartPage.navigateTo();
        System.out.println("Cart page opened: " + cartPage.getTitle());
    }

    @Then("the cart should contain exactly {int} items")
    public void theCartShouldContainExactlyItems(int expectedCount) {
        // getCartItemCount() uses Shopify's /cart.js API — reliable regardless of page DOM
        int actualCount = cartPage.getCartItemCount();
        assertThat(actualCount)
                .as("Cart should contain exactly " + expectedCount + " items (via Shopify /cart.js API)")
                .isEqualTo(expectedCount);
        System.out.println("✓ Cart item count assertion passed: " + actualCount + " items");
    }

    @Then("each cart item should display a product name")
    public void eachCartItemShouldDisplayProductName() {
        List<CartPage.CartItemDetails> items = cartPage.getCartItems();
        SoftAssertions softly = new SoftAssertions();

        for (CartPage.CartItemDetails item : items) {
            softly.assertThat(item.getMaterialInfo())
                    .as("Cart item " + item.getIndex() + " should have material info")
                    .isNotBlank();
        }
        softly.assertAll();
        System.out.println("✓ All cart items have product/material names");
    }

    @Then("each cart item should display a price")
    public void eachCartItemShouldDisplayPrice() {
        List<CartPage.CartItemDetails> items = cartPage.getCartItems();
        SoftAssertions softly = new SoftAssertions();

        for (CartPage.CartItemDetails item : items) {
            softly.assertThat(item.getPrice())
                    .as("Cart item " + item.getIndex() + " should have a price")
                    .isNotBlank();
        }
        softly.assertAll();
        cartPage.printCartDetails(items);
        System.out.println("✓ All cart items have prices");
    }

    @Then("each cart item should have a product link")
    public void eachCartItemShouldHaveProductLink() {
        List<CartPage.CartItemDetails> items = cartPage.getCartItems();
        SoftAssertions softly = new SoftAssertions();

        for (CartPage.CartItemDetails item : items) {
            softly.assertThat(item.getLink())
                    .as("Cart item " + item.getIndex() + " should have a product link")
                    .isNotBlank()
                    .startsWith("https://casekaro.com");
        }
        softly.assertAll();
        System.out.println("✓ All cart items have valid product links");
    }
}
