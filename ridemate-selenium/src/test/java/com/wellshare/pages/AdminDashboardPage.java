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
 * Page Object: Admin Dashboard
 * URL: /admin/dashboard
 */
public class AdminDashboardPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // Stat cards
    @FindBy(css = ".stat-card")
    private List<WebElement> statCards;

    @FindBy(css = ".stat-value")
    private List<WebElement> statValues;

    // Sidebar nav
    @FindBy(css = "a[routerlink='/admin/verifications']")
    private WebElement verificationsNavLink;

    @FindBy(css = "a[routerlink='/admin/users']")
    private WebElement usersNavLink;

    // SOS queue
    @FindBy(css = ".sos-card, .queue-card.sos")
    private List<WebElement> sosCards;

    @FindBy(css = ".ack-btn, button.acknowledge")
    private WebElement acknowledgeButton;

    // Reports queue
    @FindBy(css = ".queue-card:not(.sos-card)")
    private List<WebElement> reportCards;

    // Empty queue message
    @FindBy(css = ".empty-queue p, .empty-state p")
    private WebElement emptyQueueMessage;

    // Page title
    @FindBy(css = ".topbar-title h2, .admin-topbar h2")
    private WebElement pageTitle;

    public AdminDashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public boolean isAdminDashboardLoaded() {
        try {
            wait.until(ExpectedConditions.urlContains("/admin"));
            wait.until(ExpectedConditions.visibilityOfAllElements(statCards));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int getStatCardCount() {
        return statCards.size();
    }

    public String getPageTitle() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        return pageTitle.getText();
    }

    public void clickVerificationsNav() {
        wait.until(ExpectedConditions.elementToBeClickable(verificationsNavLink));
        verificationsNavLink.click();
    }

    public void clickUsersNav() {
        wait.until(ExpectedConditions.elementToBeClickable(usersNavLink));
        usersNavLink.click();
    }

    public int getSosAlertCount() {
        try { return sosCards.size(); }
        catch (Exception e) { return 0; }
    }

    public void acknowledgeSosAlert() {
        wait.until(ExpectedConditions.elementToBeClickable(acknowledgeButton));
        acknowledgeButton.click();
    }

    public boolean isEmptyQueueMessageVisible() {
        try { return emptyQueueMessage.isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }
}
