package pages;

import com.microsoft.playwright.Page;

public class HomePage {

    private final Page page;
    private static final String BASE_URL = "https://casekaro.com";

    // Locators
    private static final String NAV_LOGO = ".header__heading-logo";

    public HomePage(Page page) {
        this.page = page;
    }

    public void navigateTo() {
        page.navigate(BASE_URL);
        page.waitForLoadState();
    }

    public String getTitle() {
        return page.title();
    }

    public boolean isLogoVisible() {
        return page.locator(NAV_LOGO).isVisible();
    }
}
