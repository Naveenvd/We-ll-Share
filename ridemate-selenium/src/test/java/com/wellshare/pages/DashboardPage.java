package com.wellshare.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object: Main Dashboard
 * URL: /dashboard
 */
public class DashboardPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // Greeting text (e.g., "Hello, Naveen!")
    @FindBy(css = ".greeting, .welcome-text, h2")
    private WebElement greetingText;

    // Stat cards
    @FindBy(css = ".stat-card")
    private WebElement statCard;

    // Sidebar nav links
    @FindBy(css = "a[routerlink='/rides'], a[href*='/rides']")
    private WebElement ridesNavLink;

    @FindBy(css = "a[routerlink='/bookings'], a[href*='/bookings']")
    private WebElement bookingsNavLink;

    @FindBy(css = "a[routerlink='/parcels'], a[href*='/parcels']")
    private WebElement parcelsNavLink;

    @FindBy(css = "a[routerlink='/profile'], a[href*='/profile']")
    private WebElement profileNavLink;

    // Mode switch button (RIDER ↔ DRIVER)
    @FindBy(css = ".mode-switch-btn, button.mode-btn, .mode-pill")
    private WebElement modeSwitchButton;

    // Hamburger menu (mobile)
    @FindBy(css = ".hamburger-btn, button.menu-btn")
    private WebElement hamburgerButton;

    // Logout button
    @FindBy(css = "button.nav-item--logout, .logout-btn")
    private WebElement logoutButton;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    /** Wait until dashboard is loaded by checking greeting is visible */
    public boolean isDashboardLoaded() {
        try {
            wait.until(ExpectedConditions.urlContains("/dashboard"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getGreetingText() {
        wait.until(ExpectedConditions.visibilityOf(greetingText));
        return greetingText.getText();
    }

    public void clickRidesNav()    { ridesNavLink.click(); }
    public void clickBookingsNav() { bookingsNavLink.click(); }
    public void clickParcelsNav()  { parcelsNavLink.click(); }
    public void clickProfileNav()  { profileNavLink.click(); }

    public void clickModeSwitch()  {
        wait.until(ExpectedConditions.elementToBeClickable(modeSwitchButton));
        modeSwitchButton.click();
    }

    public void clickLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(logoutButton));
        logoutButton.click();
    }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }
}
