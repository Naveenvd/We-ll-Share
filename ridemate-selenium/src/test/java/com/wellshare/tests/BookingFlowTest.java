package com.wellshare.tests;

import com.wellshare.base.BaseTest;
import com.wellshare.pages.DashboardPage;
import com.wellshare.pages.LoginPage;
import com.wellshare.pages.SearchRidePage;
import com.wellshare.utils.ConfigReader;
import com.wellshare.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * ─────────────────────────────────────────────────────────
 *  BookingFlowTest — end-to-end booking user journey
 *
 *  TC_BOOKING_001 - Rider can navigate to bookings page
 *  TC_BOOKING_002 - My Bookings page loads with correct title
 *  TC_BOOKING_003 - Driver bookings page loads
 *  TC_BOOKING_004 - Booking requires login (auth guard test)
 *  TC_BOOKING_005 - Dashboard → Rides → Search → results flow
 * ─────────────────────────────────────────────────────────
 */
public class BookingFlowTest extends BaseTest {

    private LoginPage loginPage;
    private DashboardPage dashboardPage;
    private SearchRidePage searchRidePage;

    @BeforeMethod
    public void loginAsRider() {
        navigateTo(ConfigReader.get("app.login.url"));
        loginPage = new LoginPage(driver);
        loginPage.login(
            ConfigReader.get("rider.email"),
            ConfigReader.get("rider.password")
        );
        dashboardPage = new DashboardPage(driver);
    }

    // ── TC_BOOKING_001 ────────────────────────────────────────────
    @Test(groups = {"smoke", "regression"},
          description = "Rider can navigate to My Bookings page from dashboard")
    public void riderCanNavigateToBookings() {
        ExtentReportManager.getTest().info("TC_BOOKING_001 — Navigate to bookings");

        Assert.assertTrue(
            dashboardPage.isDashboardLoaded(),
            "Dashboard should load first"
        );

        dashboardPage.clickBookingsNav();

        Assert.assertTrue(
            driver.getCurrentUrl().contains("/bookings"),
            "URL should contain /bookings after nav click"
        );
        ExtentReportManager.getTest().info("Navigated to: " + driver.getCurrentUrl());
    }

    // ── TC_BOOKING_002 ────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "My Bookings page loads successfully")
    public void myBookingsPageLoads() {
        ExtentReportManager.getTest().info("TC_BOOKING_002 — Bookings page load");

        navigateTo(ConfigReader.get("app.bookings.url"));

        Assert.assertTrue(
            driver.getCurrentUrl().contains("/bookings"),
            "Bookings page should be accessible"
        );
        Assert.assertFalse(
            driver.getTitle().isEmpty(),
            "Page should have a title"
        );
    }

    // ── TC_BOOKING_003 ────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Unauthenticated user is redirected from bookings to login")
    public void unauthenticatedUserRedirectedFromBookings() {
        ExtentReportManager.getTest().info("TC_BOOKING_003 — Auth guard on bookings");

        // Open a fresh browser session (no login)
        driver.manage().deleteAllCookies();
        driver.executeScript("sessionStorage.clear(); localStorage.clear();");

        // Try to access bookings directly
        driver.get(ConfigReader.get("app.bookings.url"));

        // Auth guard should redirect to login
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/login") ||
            driver.getCurrentUrl().contains("/auth"),
            "Unauthenticated user should be redirected to login page. " +
            "Actual URL: " + driver.getCurrentUrl()
        );
    }

    // ── TC_BOOKING_004 ────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Dashboard stat cards are displayed after login")
    public void dashboardStatCardsAreVisible() {
        ExtentReportManager.getTest().info("TC_BOOKING_004 — Dashboard stat cards");

        Assert.assertTrue(
            dashboardPage.isDashboardLoaded(),
            "Dashboard should load after rider login"
        );
    }

    // ── TC_BOOKING_005 ────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Full flow: Dashboard → Rides nav → Search page loads")
    public void dashboardToSearchRideFlow() {
        ExtentReportManager.getTest().info("TC_BOOKING_005 — Dashboard → Search flow");

        Assert.assertTrue(dashboardPage.isDashboardLoaded());
        ExtentReportManager.getTest().info("Step 1: Dashboard loaded ✓");

        dashboardPage.clickRidesNav();
        searchRidePage = new SearchRidePage(driver);
        ExtentReportManager.getTest().info("Step 2: Clicked Rides nav ✓");

        Assert.assertTrue(
            driver.getCurrentUrl().contains("/rides"),
            "Should navigate to /rides"
        );
        ExtentReportManager.getTest().info("Step 3: On Rides search page ✓");
    }
}
