package com.wellshare.tests;

import com.wellshare.base.BaseTest;
import com.wellshare.pages.DashboardPage;
import com.wellshare.pages.LoginPage;
import com.wellshare.utils.ConfigReader;
import com.wellshare.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * ─────────────────────────────────────────────────────────
 *  LoginTest — covers all login scenarios
 *
 *  Test Cases:
 *  TC_LOGIN_001 - Valid rider login → redirect to dashboard
 *  TC_LOGIN_002 - Invalid password  → error message shown
 *  TC_LOGIN_003 - Invalid email     → error message shown
 *  TC_LOGIN_004 - Empty form        → button disabled / validation
 *  TC_LOGIN_005 - Admin login       → redirects to admin dashboard
 * ─────────────────────────────────────────────────────────
 */
public class LoginTest extends BaseTest {

    private LoginPage loginPage;
    private DashboardPage dashboardPage;

    @BeforeMethod
    public void openLoginPage() {
        navigateTo(ConfigReader.get("app.login.url"));
        loginPage     = new LoginPage(driver);
        dashboardPage = new DashboardPage(driver);
    }

    // ── TC_LOGIN_001 ──────────────────────────────────────────────
    @Test(groups = {"smoke", "regression"},
          description = "Valid rider credentials redirect to dashboard")
    public void validRiderLogin() {
        ExtentReportManager.getTest().info("TC_LOGIN_001 — Valid rider login");

        loginPage.login(
            ConfigReader.get("rider.email"),
            ConfigReader.get("rider.password")
        );

        Assert.assertTrue(
            dashboardPage.isDashboardLoaded(),
            "Dashboard should load after valid login"
        );
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/dashboard"),
            "URL should contain /dashboard after login"
        );

        ExtentReportManager.getTest().info("Redirected to: " + driver.getCurrentUrl());
    }

    // ── TC_LOGIN_002 ──────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Wrong password shows error message")
    public void wrongPasswordShowsError() {
        ExtentReportManager.getTest().info("TC_LOGIN_002 — Wrong password");

        loginPage.login(
            ConfigReader.get("rider.email"),
            "WrongPassword999"
        );

        Assert.assertTrue(
            loginPage.isErrorDisplayed(),
            "Error message should be visible for wrong password"
        );
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/login"),
            "Should stay on login page"
        );
    }

    // ── TC_LOGIN_003 ──────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Non-existent email shows error message")
    public void nonExistentEmailShowsError() {
        ExtentReportManager.getTest().info("TC_LOGIN_003 — Non-existent email");

        loginPage.login("nobody@notexist.com", "Test@1234");

        Assert.assertTrue(
            loginPage.isErrorDisplayed(),
            "Error should be shown for unregistered email"
        );
    }

    // ── TC_LOGIN_004 ──────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Empty email field — form validation prevents submit")
    public void emptyEmailFieldValidation() {
        ExtentReportManager.getTest().info("TC_LOGIN_004 — Empty email validation");

        loginPage.enterPassword("Test@1234");
        // Don't enter email

        Assert.assertTrue(
            loginPage.isLoginButtonDisabled(),
            "Login button should be disabled when email is empty"
        );
    }

    // ── TC_LOGIN_005 ──────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Admin login redirects to admin dashboard")
    public void adminLoginRedirectsToAdminDashboard() {
        ExtentReportManager.getTest().info("TC_LOGIN_005 — Admin login");

        loginPage.login(
            ConfigReader.get("admin.email"),
            ConfigReader.get("admin.password")
        );

        Assert.assertTrue(
            driver.getCurrentUrl().contains("/admin"),
            "Admin user should land on /admin/dashboard"
        );
    }
}
