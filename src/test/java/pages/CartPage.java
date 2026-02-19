package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CartPage {

    private final Page page;
    private static final String CART_URL = "https://casekaro.com/cart";

    // DOM Locators (used as fallback)
    private static final String CART_ITEM_ROW = "tr.cart-item, .cart-item, [data-cart-item]";
    private static final String ITEM_PRICE    = ".price, .money";
    private static final String ITEM_LINK     = "a";
    private static final String CART_EMPTY_MSG = "text=Your cart is empty";

    public CartPage(Page page) {
        this.page = page;
    }

    public void navigateTo() {
        page.navigate(CART_URL);
        page.waitForLoadState();
        page.waitForTimeout(2000);
    }

    /**
     * Clears the Shopify cart reliably using the /cart/clear.js AJAX API.
     * We first navigate to the store's home page so the Shopify session cookie
     * is set, then call the clear endpoint.
     */
    public void clearCart() {
        // Navigate to home first to ensure session cookie is active
        page.navigate("https://casekaro.com");
        page.waitForLoadState();
        page.waitForTimeout(1000);

        // Call Shopify's cart clear API
        page.evaluate("""
            () => fetch('/cart/clear.js', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            }).then(r => r.json()).then(d => console.log('Cart cleared:', d))
        """);
        page.waitForTimeout(2000);
        System.out.println("Cart cleared via Shopify API");
    }

    /**
     * Returns the number of UNIQUE line items in the cart using Shopify's /cart.js API.
     * This is far more reliable than counting DOM elements which vary by theme.
     */
    public int getCartItemCount() {
        // Use Shopify's cart.js API — guaranteed to return accurate line item count
        Object result = page.evaluate("""
            () => fetch('/cart.js')
                      .then(r => r.json())
                      .then(data => data.items.length)
        """);
        int count = result != null ? ((Number) result).intValue() : 0;
        System.out.println("Cart item count from Shopify API: " + count);
        return count;
    }

    public String getTitle() {
        return page.title();
    }

    public boolean isCartEmpty() {
        return getCartItemCount() == 0;
    }

    /**
     * Fetches cart item details using Shopify's /cart.js API for reliable data extraction.
     * Falls back to DOM parsing if needed.
     */
    @SuppressWarnings("unchecked")
    public List<CartItemDetails> getCartItems() {
        List<CartItemDetails> items = new ArrayList<>();

        try {
            // Fetch all cart items via Shopify cart.js API
            Object rawItems = page.evaluate("""
                () => fetch('/cart.js')
                          .then(r => r.json())
                          .then(data => data.items.map(item => ({
                              title:   item.product_title,
                              variant: item.variant_title,
                              price:   (item.price / 100).toFixed(2),
                              url:     item.url,
                              props:   item.properties
                          })))
            """);

            if (rawItems instanceof List) {
                List<Map<String, Object>> apiItems = (List<Map<String, Object>>) rawItems;
                for (int i = 0; i < apiItems.size(); i++) {
                    Map<String, Object> item = apiItems.get(i);

                    String title   = String.valueOf(item.getOrDefault("title", "Unknown"));
                    String variant = String.valueOf(item.getOrDefault("variant", ""));
                    String price   = "₹" + item.getOrDefault("price", "0");
                    String url     = "https://casekaro.com" + item.getOrDefault("url", "");

                    // Build material info from variant and properties
                    String materialInfo = title;
                    if (!variant.isEmpty() && !variant.equals("null")) {
                        materialInfo += " | " + variant;
                    }

                    // Check properties (where Material: is stored)
                    Object props = item.get("props");
                    if (props instanceof Map) {
                        Map<String, Object> properties = (Map<String, Object>) props;
                        for (Map.Entry<String, Object> entry : properties.entrySet()) {
                            if (entry.getKey().toLowerCase().contains("material")) {
                                materialInfo += " | Material: " + entry.getValue();
                            }
                        }
                    }

                    items.add(new CartItemDetails(i + 1, materialInfo, price, url));
                }
            }
        } catch (Exception e) {
            System.out.println("API fetch failed, falling back to DOM: " + e.getMessage());
            // DOM fallback
            Locator rows = page.locator(CART_ITEM_ROW);
            int count = rows.count();
            for (int i = 0; i < count; i++) {
                Locator row  = rows.nth(i);
                String fullText = row.innerText();

                String price = "";
                Locator priceLocator = row.locator(ITEM_PRICE);
                if (priceLocator.count() > 0) price = priceLocator.first().innerText().trim();

                String materialInfo = "Unknown";
                if (fullText.contains("Material:")) {
                    int start = fullText.indexOf("Material:");
                    int end   = fullText.indexOf("\n", start);
                    if (end == -1) end = fullText.length();
                    materialInfo = fullText.substring(start, end).trim();
                }

                String link = "";
                Locator linkLocator = row.locator(ITEM_LINK);
                if (linkLocator.count() > 0) {
                    String href = linkLocator.first().getAttribute("href");
                    if (href != null && !href.isEmpty()) link = "https://casekaro.com" + href;
                }

                items.add(new CartItemDetails(i + 1, materialInfo, price, link));
            }
        }

        return items;
    }

    public void printCartDetails(List<CartItemDetails> items) {
        System.out.println("\n--- Cart Details ---");
        for (CartItemDetails item : items) {
            System.out.println("Item " + item.getIndex());
            System.out.println("  " + item.getMaterialInfo());
            System.out.println("  Price: " + item.getPrice());
            System.out.println("  Link : " + item.getLink());
        }
        System.out.println("--------------------\n");
    }

    // Inner DTO class
    public static class CartItemDetails {
        private final int    index;
        private final String materialInfo;
        private final String price;
        private final String link;

        public CartItemDetails(int index, String materialInfo, String price, String link) {
            this.index        = index;
            this.materialInfo = materialInfo;
            this.price        = price;
            this.link         = link;
        }

        public int    getIndex()        { return index; }
        public String getMaterialInfo() { return materialInfo; }
        public String getPrice()        { return price; }
        public String getLink()         { return link; }
    }
}
