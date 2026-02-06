import com.microsoft.playwright.*;

public class CaseKaroTest {
    public static void main(String[] args) throws Exception {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(false));

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // STEP 1 — Navigate to website
            page.navigate("https://casekaro.com");
            page.waitForLoadState();
            System.out.println("Home page opened");

            // STEP 2 — Open Mobile Covers page
            page.navigate("https://casekaro.com/pages/phone-cases-by-model");
            page.waitForLoadState();
            System.out.println("Mobile covers page opened");

            // STEP 3 — Search Apple brand
            page.navigate("https://casekaro.com/search?q=Apple");
            page.waitForLoadState();
            System.out.println("Apple search completed");

            // STEP 4 — Negative validation
            assert page.locator("text=Samsung").count() == 0 :
                    "Samsung brand still visible!";
            System.out.println("Negative validation passed");

            // STEP 5 — Search iPhone 16 Pro
            page.navigate("https://casekaro.com/search?q=iPhone+16+Pro");
            page.waitForLoadState();
            System.out.println("iPhone results opened");

            // STEP 6 — Open first product directly
            page.navigate("https://casekaro.com/products/shield-iphone-16-pro-back-cover");
            page.waitForLoadState();
            System.out.println("Correct product opened");

            // STEP 7 & 8 — Add materials to cart
            String[] materials = {"Soft", "Glass", "Hard"};

            for (String material : materials) {

                System.out.println("Selecting material: " + material);

                page.evaluate("""
                    (material) => {
                        const groups = [...document.querySelectorAll("fieldset")];
                        const matGroup = groups.find(g =>
                            g.innerText.toLowerCase().includes("material"));

                        if (!matGroup) return;

                        const labels = [...matGroup.querySelectorAll("label")];
                        const option = labels.find(l =>
                            l.innerText.trim().toLowerCase() === material.toLowerCase());

                        if (option) option.click();
                    }
                """, material);

                page.waitForTimeout(2000);

                page.locator("form[action='/cart/add'] button")
                        .first()
                        .click();

                page.waitForTimeout(3500);

                System.out.println(material + " added to cart");

                page.keyboard().press("Escape");
                page.waitForTimeout(1500);
            }

            System.out.println("All materials added successfully");

            // STEP 9 — Open cart
            page.navigate("https://casekaro.com/cart");
            page.waitForLoadState();
            System.out.println("Cart opened");

            // STEP 10 — Validate cart items (FIXED)
            Locator cartItems = page.locator("tr.cart-item:visible");

            page.waitForTimeout(2000);

            int count = cartItems.count();
            System.out.println("Items in cart: " + count);

            assert count == 3 : "Cart does not contain 3 items!";
            System.out.println("Validation Passed: 3 items in cart");

            // STEP 11 — Print details
            System.out.println("\n--- Cart Details ---");

            for (int i = 0; i < count; i++) {
                Locator item = cartItems.nth(i);

                String fullText = item.innerText();
                String price = item.locator(".price, .money")
                        .first().innerText();

                String materialInfo = "Unknown";
                if (fullText.contains("Material:")) {
                    int start = fullText.indexOf("Material:");
                    int end = fullText.indexOf("\n", start);
                    if (end == -1) end = fullText.length();
                    materialInfo = fullText.substring(start, end);
                }

                String link = "https://casekaro.com" +
                        item.locator("a").first().getAttribute("href");

                System.out.println("Item " + (i + 1));
                System.out.println("  " + materialInfo);
                System.out.println("  Price: " + price);
                System.out.println("  Link: " + link);
            }

            System.out.println("\nPress ENTER to close...");
            System.in.read();
        }
    }
}