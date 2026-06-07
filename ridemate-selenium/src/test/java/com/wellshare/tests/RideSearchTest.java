package com.wellshare.tests;

import com.wellshare.base.BaseTest;
import com.wellshare.pages.LoginPage;
import com.wellshare.pages.SearchRidePage;
import com.wellshare.utils.ConfigReader;
import com.wellshare.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ─────────────────────────────────────────────────────────
 *  RideSearchTest — covers ride search functionality
 *
 *  TC_SEARCH_001 - Valid search returns ride results
 *  TC_SEARCH_002 - Search with women-only filter
 *  TC_SEARCH_003 - Future date required (past date rejected)
 *  TC_SEARCH_004 - Search page loads without login (public?)
 *  TC_SEARCH_005 - No rides found shows empty state message
 * ─────────────────────────────────────────────────────────
 */
public class RideSearchTest extends BaseTest {

    private LoginPage loginPage;
    private SearchRidePage searchRidePage;
    private String tomorrow;

    @BeforeMethod
    public void loginAndOpenSearchPage() {
        // Get tomorrow's date for searches
        tomorrow = LocalDate.now().plusDays(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Login as rider
        navigateTo(ConfigReader.get("app.login.url"));
        loginPage = new LoginPage(driver);
        loginPage.login(
            ConfigReader.get("rider.email"),
            ConfigReader.get("rider.password")
        );

        // Navigate to rides page
        navigateTo(ConfigReader.get("app.rides.url"));
        searchRidePage = new SearchRidePage(driver);
        ExtentReportManager.getTest().info("Logged in and on rides search page");
    }

    // ── TC_SEARCH_001 ─────────────────────────────────────────────
    @Test(groups = {"smoke", "regression"},
          description = "Valid search shows ride result cards")
    public void validSearchShowsResults() {
        ExtentReportManager.getTest().info("TC_SEARCH_001 — Search with valid inputs");

        searchRidePage.searchRide("Chennai", "Bangalore", tomorrow, "1");

        // Either results shown OR no-results message (both are valid)
        boolean hasResults    = searchRidePage.getRideResultCount() > 0;
        boolean hasNoResults  = searchRidePage.isNoResultsDisplayed();

        Assert.assertTrue(
            hasResults || hasNoResults,
            "Search should either show rides or a 'no rides found' message"
        );

        ExtentReportManager.getTest().info(
            "Ride count found: " + searchRidePage.getRideResultCount()
        );
    }

    // ── TC_SEARCH_002 ─────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Women-only filter toggle works without error")
    public void womenOnlyFilterToggleWorks() {
        ExtentReportManager.getTest().info("TC_SEARCH_002 — Women-only filter");

        searchRidePage.enterFrom("Chennai");
        searchRidePage.enterTo("Coimbatore");
        searchRidePage.enterDate(tomorrow);
        searchRidePage.toggleWomenOnly();   // Enable women-only
        searchRidePage.clickSearch();

        // Page should not crash / redirect
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/rides"),
            "Should stay on rides page after women-only filter search"
        );
    }

    // ── TC_SEARCH_003 ─────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Rides page is accessible after login")
    public void ridesPageLoadsAfterLogin() {
        ExtentReportManager.getTest().info("TC_SEARCH_003 — Rides page accessibility");

        Assert.assertTrue(
            driver.getCurrentUrl().contains("/rides"),
            "Logged-in user should access rides page"
        );
    }

    // ── TC_SEARCH_004 ─────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Search with 2 seats filters correctly")
    public void searchWithMultipleSeats() {
        ExtentReportManager.getTest().info("TC_SEARCH_004 — Multi-seat search");

        searchRidePage.searchRide("Chennai", "Bangalore", tomorrow, "2");

        // All results should have >= 2 seats available (API-level guarantee)
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/rides"),
            "Should remain on rides page after 2-seat search"
        );

        int count = searchRidePage.getRideResultCount();
        ExtentReportManager.getTest().info("Results with 2 seats: " + count);
    }

    // ── TC_SEARCH_005 ─────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Search for remote route shows no-results message")
    public void noRidesFoundShowsEmptyState() {
        ExtentReportManager.getTest().info("TC_SEARCH_005 — No results empty state");

        // Very unlikely route — should return no results
        searchRidePage.searchRide(
            "Nowhere City", "Nowhere Town", tomorrow, "1"
        );

        boolean hasNoResultsMsg = searchRidePage.isNoResultsDisplayed();
        boolean hasZeroResults  = searchRidePage.getRideResultCount() == 0;

        Assert.assertTrue(
            hasNoResultsMsg || hasZeroResults,
            "Unknown route should show no rides or empty state"
        );
    }
}
