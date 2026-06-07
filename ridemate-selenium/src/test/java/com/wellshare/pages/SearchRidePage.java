package com.wellshare.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object: Search Rides Page
 * URL: /rides
 */
public class SearchRidePage {

    private WebDriver driver;
    private WebDriverWait wait;

    // Search form fields
    @FindBy(css = "input[placeholder*='from'], input[formcontrolname='from'], .loc-input--from input")
    private WebElement fromInput;

    @FindBy(css = "input[placeholder*='to'], input[formcontrolname='to'], .loc-input--to input")
    private WebElement toInput;

    @FindBy(css = "input[type='date'], input[formcontrolname='date']")
    private WebElement dateInput;

    @FindBy(css = "input[formcontrolname='seats'], input[type='number']")
    private WebElement seatsInput;

    // Women only toggle
    @FindBy(css = "mat-slide-toggle, .women-only-toggle, input[formcontrolname='womenOnly']")
    private WebElement womenOnlyToggle;

    // Search button
    @FindBy(css = "button[type='submit'], .search-btn, button.search")
    private WebElement searchButton;

    // Ride result cards
    @FindBy(css = ".ride-card, .result-card, mat-card.ride")
    private List<WebElement> rideCards;

    // No results message
    @FindBy(css = ".no-results, .empty-state p, .no-rides")
    private WebElement noResultsMessage;

    // Book button on a ride card
    @FindBy(css = ".book-btn, button.book, button[color='primary']")
    private WebElement bookButton;

    public SearchRidePage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public void enterFrom(String location) {
        wait.until(ExpectedConditions.visibilityOf(fromInput));
        fromInput.clear();
        fromInput.sendKeys(location);
    }

    public void enterTo(String location) {
        toInput.clear();
        toInput.sendKeys(location);
    }

    public void enterDate(String date) {  // YYYY-MM-DD format
        dateInput.clear();
        dateInput.sendKeys(date);
    }

    public void enterSeats(String seats) {
        seatsInput.clear();
        seatsInput.sendKeys(seats);
    }

    public void toggleWomenOnly() {
        womenOnlyToggle.click();
    }

    public void clickSearch() {
        wait.until(ExpectedConditions.elementToBeClickable(searchButton));
        searchButton.click();
    }

    public int getRideResultCount() {
        try {
            wait.until(ExpectedConditions.visibilityOfAllElements(rideCards));
            return rideCards.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isNoResultsDisplayed() {
        try {
            return noResultsMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickBookOnFirstRide() {
        wait.until(ExpectedConditions.elementToBeClickable(bookButton));
        bookButton.click();
    }

    /** Full search flow in one call */
    public void searchRide(String from, String to, String date, String seats) {
        enterFrom(from);
        enterTo(to);
        enterDate(date);
        enterSeats(seats);
        clickSearch();
    }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }
}
