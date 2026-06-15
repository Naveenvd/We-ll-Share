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
 * Page Object: Profile Page
 * URL: /profile
 */
public class ProfilePage {

    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(css = "input[formcontrolname='name']")
    private WebElement nameInput;

    @FindBy(css = "button[type='submit'], .save-btn")
    private WebElement saveButton;

    @FindBy(css = ".mat-mdc-tab, .mdc-tab")
    private List<WebElement> tabs;

    @FindBy(css = ".mdc-snackbar__label, .mat-mdc-snack-bar-label, snack-bar-container")
    private WebElement snackBar;

    @FindBy(css = "h1, h2, .profile-name, .user-name")
    private WebElement profileHeading;

    public ProfilePage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public boolean isPageLoaded() {
        try {
            wait.until(ExpectedConditions.urlContains("/profile"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isNameVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOf(nameInput));
            return nameInput.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getNameValue() {
        wait.until(ExpectedConditions.visibilityOf(nameInput));
        return nameInput.getAttribute("value");
    }

    public void setName(String name) {
        wait.until(ExpectedConditions.visibilityOf(nameInput));
        nameInput.clear();
        nameInput.sendKeys(name);
    }

    public void clickSave() {
        wait.until(ExpectedConditions.elementToBeClickable(saveButton));
        saveButton.click();
    }

    public boolean isSnackBarVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOf(snackBar));
            return snackBar.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public int getTabCount() {
        try {
            wait.until(ExpectedConditions.visibilityOfAllElements(tabs));
            return tabs.size();
        } catch (Exception e) {
            return 0;
        }
    }
}
