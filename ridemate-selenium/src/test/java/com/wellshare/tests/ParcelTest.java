package com.wellshare.tests;

import com.wellshare.base.BaseTest;
import com.wellshare.pages.LoginPage;
import com.wellshare.utils.ConfigReader;
import com.wellshare.utils.ExtentReportManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * ─────────────────────────────────────────────────────────
 *  ParcelTest — covers parcel sender/receiver flow
 *
 *  TC_PARCEL_001 - Parcels page loads after login
 *  TC_PARCEL_002 - Unauthenticated user redirected from parcels
 *  TC_PARCEL_003 - Post Parcel page is accessible
 *  TC_PARCEL_004 - Driver parcels (deliveries) page loads
 *  TC_PARCEL_005 - Parcel search page accessible to logged-in user
 * ─────────────────────────────────────────────────────────
 */
public class ParcelTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod
    public void loginAsRider() {
        navigateTo(ConfigReader.get("app.login.url"));
        loginPage = new LoginPage(driver);
        loginPage.login(
            ConfigReader.get("rider.email"),
            ConfigReader.get("rider.password")
        );
        ExtentReportManager.getTest().info("Logged in as rider: " + ConfigReader.get("rider.email"));
    }

    // ── TC_PARCEL_001 ─────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Parcels page loads for logged-in user")
    public void parcelsPageLoadsForLoggedInUser() {
        ExtentReportManager.getTest().info("TC_PARCEL_001 — Parcels page load");

        navigateTo(ConfigReader.get("app.parcels.url"));

        Assert.assertTrue(
            driver.getCurrentUrl().contains("/parcels"),
            "Parcels page should be accessible. URL: " + driver.getCurrentUrl()
        );
        Assert.assertFalse(
            driver.getPageSource().isEmpty(),
            "Parcels page should render content"
        );
        ExtentReportManager.getTest().pass("Parcels page loaded at: " + driver.getCurrentUrl());
    }

    // ── TC_PARCEL_002 ─────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Unauthenticated user is blocked from parcels page")
    public void unauthenticatedUserBlockedFromParcels() {
        ExtentReportManager.getTest().info("TC_PARCEL_002 — Auth guard on parcels");

        // Clear session to simulate unauthenticated state
        driver.manage().deleteAllCookies();
        ((JavascriptExecutor) driver).executeScript("sessionStorage.clear(); localStorage.clear();");

        driver.get(ConfigReader.get("app.parcels.url"));

        boolean redirected =
            driver.getCurrentUrl().contains("/login") ||
            driver.getCurrentUrl().contains("/auth");

        Assert.assertTrue(
            redirected,
            "Unauthenticated user should be redirected. Actual URL: " + driver.getCurrentUrl()
        );
        ExtentReportManager.getTest().pass("Auth guard working — redirected to: " + driver.getCurrentUrl());
    }

    // ── TC_PARCEL_003 ─────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Post Parcel page is accessible to logged-in rider")
    public void postParcelPageIsAccessible() {
        ExtentReportManager.getTest().info("TC_PARCEL_003 — Post Parcel page");

        navigateTo(baseUrl + "/post-parcel");

        // Should stay on post-parcel page (not redirect to login)
        boolean onPage =
            driver.getCurrentUrl().contains("/post-parcel") ||
            driver.getCurrentUrl().contains("/parcels");

        Assert.assertTrue(
            onPage,
            "Post parcel page should be accessible. URL: " + driver.getCurrentUrl()
        );
        ExtentReportManager.getTest().pass("Post Parcel page loaded at: " + driver.getCurrentUrl());
    }

    // ── TC_PARCEL_004 ─────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Driver parcels (deliveries) page loads")
    public void driverParcelsPageLoads() {
        ExtentReportManager.getTest().info("TC_PARCEL_004 — Driver parcels page");

        navigateTo(baseUrl + "/driver-parcels");

        boolean onPage =
            driver.getCurrentUrl().contains("/driver-parcels") ||
            driver.getCurrentUrl().contains("/parcels") ||
            driver.getCurrentUrl().contains("/dashboard");

        Assert.assertTrue(
            onPage,
            "Driver parcels page should load. URL: " + driver.getCurrentUrl()
        );
        ExtentReportManager.getTest().pass("Driver parcels page at: " + driver.getCurrentUrl());
    }

    // ── TC_PARCEL_005 ─────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Page title is present on parcels page")
    public void parcelPageHasTitle() {
        ExtentReportManager.getTest().info("TC_PARCEL_005 — Parcels page title check");

        navigateTo(ConfigReader.get("app.parcels.url"));

        String title = driver.getTitle();
        ExtentReportManager.getTest().info("Page title: " + title);

        Assert.assertFalse(
            title.isEmpty(),
            "Parcels page should have a document title"
        );
        ExtentReportManager.getTest().pass("Title found: " + title);
    }
}
