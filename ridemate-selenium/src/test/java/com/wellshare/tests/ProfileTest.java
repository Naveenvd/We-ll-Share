package com.wellshare.tests;

import com.wellshare.base.BaseTest;
import com.wellshare.pages.LoginPage;
import com.wellshare.pages.ProfilePage;
import com.wellshare.utils.ConfigReader;
import com.wellshare.utils.ExtentReportManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * ─────────────────────────────────────────────────────────
 *  ProfileTest — covers user profile page scenarios
 *
 *  TC_PROFILE_001 - Logged-in user can view profile
 *  TC_PROFILE_002 - User can update their name
 *  TC_PROFILE_003 - /profile without login redirects to auth
 *  TC_PROFILE_004 - Profile page has tabs (Vehicles/Documents)
 * ─────────────────────────────────────────────────────────
 */
public class ProfileTest extends BaseTest {

    private ProfilePage profilePage;

    @BeforeMethod(alwaysRun = true)
    public void loginAndOpenProfile() {
        navigateTo(ConfigReader.get("app.login.url"));
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(ConfigReader.get("rider.email"), ConfigReader.get("rider.password"));

        // Wait for login to complete (may land on role-select or dashboard)
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/dashboard"),
            ExpectedConditions.urlContains("/role-select")
        ));
        if (driver.getCurrentUrl().contains("/role-select")) {
            driver.findElement(By.cssSelector(".role-card--rider")).click();
            wait.until(ExpectedConditions.urlContains("/dashboard"));
        }

        navigateTo(ConfigReader.get("app.profile.url"));
        profilePage = new ProfilePage(driver);
    }

    // ── TC_PROFILE_001 ────────────────────────────────────────────
    @Test(groups = {"smoke", "regression"},
          description = "Logged-in rider can access profile page and see name field")
    public void loggedInUserCanViewProfile() {
        ExtentReportManager.getTest().info("TC_PROFILE_001 — Profile page load");

        Assert.assertTrue(
            profilePage.isPageLoaded(),
            "Profile page URL should contain /profile"
        );
        Assert.assertTrue(
            profilePage.isNameVisible(),
            "Name input should be visible on the profile page"
        );

        ExtentReportManager.getTest().info("Profile loaded. Name: " + profilePage.getNameValue());
    }

    // ── TC_PROFILE_002 ────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "User can update their display name and see a success snackbar")
    public void userCanUpdateName() {
        ExtentReportManager.getTest().info("TC_PROFILE_002 — Update profile name");

        String newName = "Updated " + System.currentTimeMillis() % 1000;
        profilePage.setName(newName);
        profilePage.clickSave();

        Assert.assertTrue(
            profilePage.isSnackBarVisible(),
            "Success snackbar should appear after saving profile"
        );
    }

    // ── TC_PROFILE_003 ────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Navigating to /profile without auth redirects to login")
    public void profileRequiresLogin() {
        ExtentReportManager.getTest().info("TC_PROFILE_003 — Auth guard on /profile");

        // Open fresh browser context — navigate directly (no login in this test)
        // BaseTest setUp() creates a fresh Chrome session, but @BeforeMethod logged us in.
        // We navigate directly to profile. To simulate no-auth, clear sessionStorage first.
        ((JavascriptExecutor) driver).executeScript("sessionStorage.clear();");
        driver.get(ConfigReader.get("app.profile.url"));

        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/login"),
            ExpectedConditions.urlContains("/auth")
        ));

        Assert.assertTrue(
            driver.getCurrentUrl().contains("/auth") || driver.getCurrentUrl().contains("/login"),
            "Unauthenticated access to /profile should redirect to auth"
        );
    }

    // ── TC_PROFILE_004 ────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Profile page tabs (Vehicles / Documents) are visible")
    public void profileHasTabs() {
        ExtentReportManager.getTest().info("TC_PROFILE_004 — Profile tabs visible");

        int tabCount = profilePage.getTabCount();

        Assert.assertTrue(
            tabCount >= 2,
            "Profile page should have at least 2 tabs (e.g. Vehicles, Documents). Found: " + tabCount
        );

        ExtentReportManager.getTest().info("Tabs found: " + tabCount);
    }
}
