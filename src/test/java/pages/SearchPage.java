package pages;

import com.microsoft.playwright.Page;

import java.util.Map;

public class SearchPage {

    private final Page page;
    private static final String SEARCH_BASE_URL = "https://casekaro.com/search?q=";

    // Locators
    private static final String PRODUCT_CARDS   = ".product-item, .grid__item, .card-wrapper";
    private static final String NO_RESULTS_TEXT = "text=No results found";

    // Tracks last searched query for API-based brand check
    private String lastQuery = "";

    public SearchPage(Page page) {
        this.page = page;
    }

    public void searchFor(String query) {
        this.lastQuery = query;
        page.navigate(SEARCH_BASE_URL + query.replace(" ", "+"));
        page.waitForLoadState();
    }

    public String getTitle() {
        return page.title();
    }

    public int getProductCount() {
        return page.locator(PRODUCT_CARDS).count();
    }

    /**
     * Uses Shopify's /search/suggest.js API to check whether a brand name appears
     * in the actual product TITLES returned for the current search query.
     *
     * WHY API instead of DOM selectors:
     *  - DOM approach: CSS class names vary per Shopify theme, full card text includes
     *    "Compatible with Samsung" in descriptions → unreliable false positives.
     *  - API approach: Reads directly from Shopify's search index, returns only
     *    product titles — zero risk of description/footer/nav text pollution.
     *    This endpoint is standard on ALL Shopify stores.
     */
    @SuppressWarnings("unchecked")
    public boolean isBrandNameInProductTitles(String brandName) {
        String queryParam = lastQuery.isEmpty() ? "Apple" : lastQuery;

        Object result = page.evaluate(
            """
            ([query, brand]) =>
                fetch('/search/suggest.js?q=' + encodeURIComponent(query)
                      + '&resources[type]=product&resources[limit]=100')
                .then(r => r.json())
                .then(data => {
                    const products = (data.resources && data.resources.results
                                      && data.resources.results.products) || [];
                    const b = brand.toLowerCase();
                    return products.some(p => p.title.toLowerCase().includes(b));
                })
            """,
            new Object[]{queryParam, brandName}
        );

        boolean found = Boolean.TRUE.equals(result);
        System.out.println("Shopify API brand check — '" + brandName
                + "' in product titles for query '" + queryParam + "': " + found);
        return found;
    }

    public boolean isNoResultsVisible() {
        return page.locator(NO_RESULTS_TEXT).count() > 0;
    }

    public String getPageSource() {
        return page.content();
    }
}
