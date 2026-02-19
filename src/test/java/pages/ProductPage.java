package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class ProductPage {

    private final Page page;
    private static final String PRODUCT_BASE_URL = "https://casekaro.com/products/";

    // Locators
    private static final String PRODUCT_TITLE        = "h1.product__title, h1";
    private static final String MATERIAL_FIELDSET    = "fieldset";
    private static final String ADD_TO_CART_BUTTON   = "form[action='/cart/add'] button";
    private static final String CART_NOTIFICATION    = ".cart-notification, .cart-count-bubble, [id*='cart']";

    public ProductPage(Page page) {
        this.page = page;
    }

    public void navigateTo(String productHandle) {
        page.navigate(PRODUCT_BASE_URL + productHandle);
        page.waitForLoadState();
    }

    public String getProductTitle() {
        return page.locator(PRODUCT_TITLE).first().innerText().trim();
    }

    public String getPageTitle() {
        return page.title();
    }

    public boolean hasMaterialOptions() {
        Locator fieldsets = page.locator(MATERIAL_FIELDSET);
        for (int i = 0; i < fieldsets.count(); i++) {
            String text = fieldsets.nth(i).innerText().toLowerCase();
            if (text.contains("material")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Selects a material option by clicking its label inside the material fieldset.
     */
    public void selectMaterial(String material) {
        page.evaluate("""
            (material) => {
                const groups = [...document.querySelectorAll("fieldset")];
                const matGroup = groups.find(g =>
                    g.innerText.toLowerCase().includes("material"));
                if (!matGroup) {
                    throw new Error("Material fieldset not found");
                }
                const labels = [...matGroup.querySelectorAll("label")];
                const option = labels.find(l =>
                    l.innerText.trim().toLowerCase() === material.toLowerCase());
                if (!option) {
                    throw new Error("Material option '" + material + "' not found");
                }
                option.click();
            }
        """, material);
        page.waitForTimeout(2000);
    }

    /**
     * Clicks the Add to Cart button and waits briefly for the cart notification.
     */
    public void clickAddToCart() {
        Locator addToCartBtn = page.locator(ADD_TO_CART_BUTTON).first();
        addToCartBtn.waitFor();
        addToCartBtn.click();
        page.waitForTimeout(3500);
    }

    public void closeCartNotification() {
        page.keyboard().press("Escape");
        page.waitForTimeout(1500);
    }

    public boolean isCartNotificationVisible() {
        return page.locator(CART_NOTIFICATION).count() > 0;
    }
}
