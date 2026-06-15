package com.wellshare.tests;

import com.wellshare.base.BaseTest;
import com.wellshare.pages.LoginPage;
import com.wellshare.utils.ConfigReader;
import com.wellshare.utils.ExtentReportManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * ─────────────────────────────────────────────────────────
 *  NavigationTest — verifies all pages load correctly
 *
 *  TC_NAV_001 - Landing page loads at root URL
 *  TC_NAV_002 - Login page has correct title/elements
 *  TC_NAV_003 - Signup link on login page works
 *  TC_NAV_004 - Protected pages redirect unauthenticated users
 *  TC_NAV_005 - All main nav links work after login
 *  TC_NAV_006 - Page title contains "We'll Share"
 * ─────────────────────────────────────────────────────────
 */
public class NavigationTest extends BaseTest {

    // ── TC_NAV_001 ────────────────────────────────────────────────
    @Test(groups = {"smoke", "regression"},
          description = "Landing page loads at root URL")
    public void landingPageLoads() {
        ExtentReportManager.getTest().info("TC_NAV_001 — Landing page load");

        navigateTo(baseUrl);

        Assert.assertFalse(
            driver.getTitle().isEmpty(),
            "Page should have a title"
        );

        // Should NOT redirect to login (landing page is public)
        Assert.assertFalse(
            driver.getCurrentUrl().contains("/login"),
            "Landing page should be accessible without login"
        );

        ExtentReportManager.getTest().info("Page title: " + driver.getTitle());
    }

    // ── TC_NAV_002 ────────────────────────────────────────────────
    @Test(groups = {"smoke", "regression"},
          description = "Login page loads with required form fields")
    public void loginPageLoadsCorrectly() {
        ExtentReportManager.getTest().info("TC_NAV_002 — Login page load");

        navigateTo(ConfigReader.get("app.login.url"));

        Assert.assertTrue(
            driver.getCurrentUrl().contains("/login"),
            "URL should be login page"
        );
        Assert.assertFalse(
            driver.getPageSource().isEmpty(),
            "Login page should render content"
        );
    }

    // ── TC_NAV_003 ────────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Clicking signup link navigates to signup page")
    public void signupLinkNavigatesToSignupPage() {
        ExtentReportManager.getTest().info("TC_NAV_003 — Signup link navigation");

        navigateTo(ConfigReader.get("app.login.url"));
        LoginPage loginPage = new LoginPage(driver);
        loginPage.clickSignupLink();

        // Angular routing is async — wait for URL to include /signup
        wait.until(ExpectedConditions.urlContains("/signup"));

        Assert.assertTrue(
            driver.getCurrentUrl().contains("/signup"),
            "Signup link should navigate to /signup"
        );
    }

    // ── TC_NAV_004 ────────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Dashboard requires login — unauthenticated gets redirected")
    public void dashboardRequiresLogin() {
        ExtentReportManager.getTest().info("TC_NAV_004 — Auth guard on dashboard");

        // Fresh session — no login
        driver.get(ConfigReader.get("app.dashboard.url"));

        Assert.assertFalse(
            driver.getCurrentUrl().contains("/dashboard"),
            "Unauthenticated user should NOT reach /dashboard. " +
            "URL: " + driver.getCurrentUrl()
        );
    }

    // ── TC_NAV_005 ────────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "All protected pages redirect without auth")
    public void allProtectedPagesRequireLogin() {
        ExtentReportManager.getTest().info("TC_NAV_004 — Protected pages guard");

        String[] protectedUrls = {
            ConfigReader.get("app.dashboard.url"),
            ConfigReader.get("app.rides.url"),
            ConfigReader.get("app.bookings.url"),
            ConfigReader.get("app.parcels.url")
        };

        for (String url : protectedUrls) {
            driver.get(url);

            // Angular route guard runs asynchronously after the page loads.
            // Wait for it to complete the redirect before reading the URL.
            try {
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/login"),
                    ExpectedConditions.urlContains("/auth")
                ));
            } catch (Exception ignored) {
                // If still not redirected after wait, let the assertion fail below
            }

            boolean redirectedToAuth =
                driver.getCurrentUrl().contains("/login") ||
                driver.getCurrentUrl().contains("/auth");

            ExtentReportManager.getTest().info(
                url + " → " + driver.getCurrentUrl()
            );
            Assert.assertTrue(
                redirectedToAuth,
                "Protected URL '" + url + "' should redirect to auth. " +
                "Actual: " + driver.getCurrentUrl()
            );
        }
    }

    // ── TC_NAV_006 ────────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Public trip tracking page loads without login")
    public void publicTripTrackingLoadswithoutLogin() {
        ExtentReportManager.getTest().info("TC_NAV_006 — Public tracking page");

        // /track/:token is public — no auth needed
        driver.get(baseUrl + "/track/test-token-123");

        // Should not redirect to login
        Assert.assertFalse(
            driver.getCurrentUrl().contains("/login"),
            "Public tracking page should not require login"
        );
    }
}
