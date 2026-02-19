package pages;

import com.microsoft.playwright.Page;

public class MobileCoversPage {

    private final Page page;
    private static final String MOBILE_COVERS_URL =
            "https://casekaro.com/pages/phone-cases-by-model";

    // Locators
    private static final String PAGE_HEADING = "h1, h2";

    public MobileCoversPage(Page page) {
        this.page = page;
    }

    public void navigateTo() {
        page.navigate(MOBILE_COVERS_URL);
        page.waitForLoadState();
    }

    public String getTitle() {
        return page.title();
    }

    public boolean isPageHeadingVisible() {
        return page.locator(PAGE_HEADING).first().isVisible();
    }

    public String getPageHeadingText() {
        return page.locator(PAGE_HEADING).first().innerText();
    }
}
