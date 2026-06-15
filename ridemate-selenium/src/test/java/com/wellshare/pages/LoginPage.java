package com.wellshare.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * ─────────────────────────────────────────────────────────
 *  Page Object: Login Page
 *  URL: /auth/login
 *
 *  Page Object Model (POM) separates locators from test logic.
 *  If the UI changes, only update this file — not every test.
 * ─────────────────────────────────────────────────────────
 */
public class LoginPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // ── Locators (using @FindBy annotation) ──────────────────────
    @FindBy(css = "input[formcontrolname='email']")
    private WebElement emailInput;

    @FindBy(css = "input[formcontrolname='password']")
    private WebElement passwordInput;

    @FindBy(css = "button[type='submit']")
    private WebElement loginButton;

    @FindBy(xpath="//div[contains(text(),'Invalid credentials')]")
    private WebElement errorMessage;

    @FindBy(css = "a[routerlink='/auth/signup'], a[href*='signup']")
    private WebElement signupLink;

    @FindBy(css = "a[routerlink*='forgot'], a[href*='forgot']")
    private WebElement forgotPasswordLink;

    // ── Constructor ───────────────────────────────────────────────
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    // ── Actions ───────────────────────────────────────────────────

    /** Type email into the email field */
    public void enterEmail(String email) {
        wait.until(ExpectedConditions.visibilityOf(emailInput));
        emailInput.clear();
        emailInput.sendKeys(email);
    }

    /** Type password into the password field */
    public void enterPassword(String password) {
        passwordInput.clear();
        passwordInput.sendKeys(password);
    }

    /** Click the Login button */
    public void clickLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginButton.click();
    }

    /** Full login action in one step */
    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
    }

    /** Get error message text shown on bad login */
    public String getErrorMessage() {
        wait.until(ExpectedConditions.visibilityOf(errorMessage));
        return errorMessage.getText();
    }

    /** Check if error message is displayed */
    public boolean isErrorDisplayed() {
//        System.out.println("jiojij");
//        errorMessage = driver.findElement(By.xpath());
        try {
            Thread.sleep(2000);
            System.out.println(errorMessage.getText());
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Click the Sign Up link */
    public void clickSignupLink() {
        signupLink.click();
    }

    /** Check if login button is disabled (for empty form validation) */
    public boolean isLoginButtonDisabled() {
        return !loginButton.isEnabled() ||
               loginButton.getAttribute("disabled") != null;
    }

    /** Get current page URL */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
