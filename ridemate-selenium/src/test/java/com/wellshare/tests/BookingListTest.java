package com.wellshare.tests;

import com.wellshare.base.BaseTest;
import com.wellshare.pages.BookingsPage;
import com.wellshare.pages.LoginPage;
import com.wellshare.utils.ConfigReader;
import com.wellshare.utils.ExtentReportManager;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * ─────────────────────────────────────────────────────────
 *  BookingListTest — covers booking list page scenarios
 *
 *  TC_BOOKING_001 - Rider can view their bookings list
 *  TC_BOOKING_002 - Driver bookings page loads
 *  TC_BOOKING_003 - /bookings/my without login redirects to auth
 *  TC_BOOKING_004 - Clicking a booking opens booking detail
 * ─────────────────────────────────────────────────────────
 */
public class BookingListTest extends BaseTest {

    private BookingsPage bookingsPage;

    @BeforeMethod(alwaysRun = true)
    public void loginAndOpenBookings() {
        navigateTo(ConfigReader.get("app.login.url"));
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(ConfigReader.get("rider.email"), ConfigReader.get("rider.password"));

        // Wait for login then handle role-select if needed
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/dashboard"),
            ExpectedConditions.urlContains("/role-select")
        ));
        if (driver.getCurrentUrl().contains("/role-select")) {
            driver.findElement(By.cssSelector(".role-card--rider")).click();
            wait.until(ExpectedConditions.urlContains("/dashboard"));
        }

        navigateTo(ConfigReader.get("app.bookings.my.url"));
        bookingsPage = new BookingsPage(driver);
    }

    // ── TC_BOOKING_001 ────────────────────────────────────────────
    @Test(groups = {"smoke", "regression"},
          description = "Logged-in rider can access their bookings list")
    public void riderBookingsPageLoads() {
        ExtentReportManager.getTest().info("TC_BOOKING_001 — Rider bookings list");

        Assert.assertTrue(
            bookingsPage.isPageLoaded(),
            "Bookings page should be accessible to a logged-in rider"
        );

        int count = bookingsPage.getBookingCount();
        ExtentReportManager.getTest().info("Bookings found: " + count);
    }

    // ── TC_BOOKING_002 ────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Driver bookings page loads for logged-in user")
    public void driverBookingsPageLoads() {
        ExtentReportManager.getTest().info("TC_BOOKING_002 — Driver bookings page");

        navigateTo(ConfigReader.get("app.bookings.driver.url"));

        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/bookings"),
            ExpectedConditions.urlContains("/auth")
        ));

        Assert.assertTrue(
            driver.getCurrentUrl().contains("/bookings"),
            "Driver bookings page should load for authenticated user"
        );
    }

    // ── TC_BOOKING_003 ────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "/bookings/my without auth redirects to login")
    public void bookingsRequiresLogin() {
        ExtentReportManager.getTest().info("TC_BOOKING_003 — Auth guard on bookings");

        driver.executeScript("sessionStorage.clear();");
        driver.get(ConfigReader.get("app.bookings.my.url"));

        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/login"),
            ExpectedConditions.urlContains("/auth")
        ));

        Assert.assertTrue(
            driver.getCurrentUrl().contains("/auth") || driver.getCurrentUrl().contains("/login"),
            "Unauthenticated access to /bookings should redirect to auth"
        );
    }

    // ── TC_BOOKING_004 ────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Clicking a booking card opens the booking detail page")
    public void clickingBookingOpensDetail() {
        ExtentReportManager.getTest().info("TC_BOOKING_004 — Booking card click");

        int count = bookingsPage.getBookingCount();

        if (count == 0) {
            ExtentReportManager.getTest().info("No bookings found — skipping click test");
            // Not a failure — test account may have no bookings yet
            Assert.assertTrue(
                bookingsPage.isPageLoaded(),
                "Bookings page should still load even with no bookings"
            );
            return;
        }

        bookingsPage.clickFirstBooking();

        wait.until(ExpectedConditions.urlMatches(".*/bookings/\\d+.*"));

        Assert.assertTrue(
            driver.getCurrentUrl().matches(".*/bookings/\\d+.*"),
            "Clicking a booking card should open its detail page (URL: /bookings/{id})"
        );

        ExtentReportManager.getTest().info("Booking detail URL: " + driver.getCurrentUrl());
    }
}
