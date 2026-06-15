package com.wellshare.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object: Bookings List Pages
 * URL: /bookings/my  |  /bookings/driver
 */
public class BookingsPage {

    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(css = ".booking-card, mat-card")
    private List<WebElement> bookingCards;

    @FindBy(css = "h2, h1, .page-title, .section-title")
    private WebElement pageHeading;

    @FindBy(css = ".empty-state, .no-bookings, p.empty")
    private WebElement emptyState;

    public BookingsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public boolean isPageLoaded() {
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOf(pageHeading),
                ExpectedConditions.urlContains("/bookings")
            ));
            return driver.getCurrentUrl().contains("/bookings");
        } catch (Exception e) {
            return false;
        }
    }

    public int getBookingCount() {
        try {
            return bookingCards.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isEmptyStateVisible() {
        try {
            return emptyState.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickFirstBooking() {
        wait.until(ExpectedConditions.elementToBeClickable(bookingCards.get(0)));
        bookingCards.get(0).click();
    }
}
