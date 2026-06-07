package com.wellshare.tests;

import com.wellshare.base.BaseTest;
import com.wellshare.pages.AdminDashboardPage;
import com.wellshare.pages.LoginPage;
import com.wellshare.utils.ConfigReader;
import com.wellshare.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * ─────────────────────────────────────────────────────────
 *  AdminTest — covers the admin console
 *
 *  TC_ADMIN_001 - Admin login lands on admin dashboard
 *  TC_ADMIN_002 - Admin dashboard has 7 stat cards
 *  TC_ADMIN_003 - Navigate to verification queue
 *  TC_ADMIN_004 - Navigate to user management page
 *  TC_ADMIN_005 - Rider login cannot access admin routes
 * ─────────────────────────────────────────────────────────
 */
public class AdminTest extends BaseTest {

    private LoginPage loginPage;
    private AdminDashboardPage adminPage;

    @BeforeMethod
    public void loginAsAdmin() {
        navigateTo(ConfigReader.get("app.login.url"));
        loginPage = new LoginPage(driver);
        loginPage.login(
            ConfigReader.get("admin.email"),
            ConfigReader.get("admin.password")
        );
        adminPage = new AdminDashboardPage(driver);
    }

    // ── TC_ADMIN_001 ──────────────────────────────────────────────
    @Test(groups = {"smoke", "regression"},
          description = "Admin login lands on admin dashboard")
    public void adminLoginLandsOnAdminDashboard() {
        ExtentReportManager.getTest().info("TC_ADMIN_001 — Admin login");

        Assert.assertTrue(
            adminPage.isAdminDashboardLoaded(),
            "Admin should land on /admin/dashboard"
        );
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/admin"),
            "URL should contain /admin"
        );
    }

    // ── TC_ADMIN_002 ──────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Admin dashboard displays 7 stat cards")
    public void adminDashboardHasSevenStatCards() {
        ExtentReportManager.getTest().info("TC_ADMIN_002 — Stat card count");

        adminPage.isAdminDashboardLoaded();
        int cardCount = adminPage.getStatCardCount();

        ExtentReportManager.getTest().info("Stat cards found: " + cardCount);
        Assert.assertEquals(
            cardCount, 7,
            "Admin dashboard should show exactly 7 stat cards " +
            "(Users, PendingVerify, Verified, Rides, Parcels, SOS, Reports)"
        );
    }

    // ── TC_ADMIN_003 ──────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Admin can navigate to verification queue")
    public void adminCanNavigateToVerificationQueue() {
        ExtentReportManager.getTest().info("TC_ADMIN_003 — Verification queue nav");

        adminPage.isAdminDashboardLoaded();
        adminPage.clickVerificationsNav();

        Assert.assertTrue(
            driver.getCurrentUrl().contains("/verifications"),
            "URL should contain /verifications"
        );
    }

    // ── TC_ADMIN_004 ──────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Admin can navigate to user management page")
    public void adminCanNavigateToUserManagement() {
        ExtentReportManager.getTest().info("TC_ADMIN_004 — User management nav");

        adminPage.isAdminDashboardLoaded();
        adminPage.clickUsersNav();

        Assert.assertTrue(
            driver.getCurrentUrl().contains("/users"),
            "URL should contain /users"
        );
    }

    // ── TC_ADMIN_005 ──────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Regular rider cannot access admin routes")
    public void regularRiderCannotAccessAdminRoutes() {
        ExtentReportManager.getTest().info("TC_ADMIN_005 — Admin guard test");

        // Logout by clearing session, then login as rider
        driver.manage().deleteAllCookies();
        driver.executeScript("sessionStorage.clear();");

        navigateTo(ConfigReader.get("app.login.url"));
        LoginPage loginPageRider = new LoginPage(driver);
        loginPageRider.login(
            ConfigReader.get("rider.email"),
            ConfigReader.get("rider.password")
        );

        // Now try to access admin route
        driver.get(ConfigReader.get("app.admin.url"));

        // Admin guard should block and redirect
        Assert.assertFalse(
            driver.getCurrentUrl().contains("/admin/dashboard"),
            "Regular rider should NOT access admin dashboard. " +
            "URL: " + driver.getCurrentUrl()
        );
    }
}
