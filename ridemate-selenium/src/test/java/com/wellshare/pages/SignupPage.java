package com.wellshare.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object: Signup Page
 * URL: /auth/signup
 */
public class SignupPage {

    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(css = "input[formcontrolname='name']")
    private WebElement nameInput;

    @FindBy(css = "input[formcontrolname='email']")
    private WebElement emailInput;

    @FindBy(css = "input[formcontrolname='phone']")
    private WebElement phoneInput;

    @FindBy(css = "input[formcontrolname='password']")
    private WebElement passwordInput;

    @FindBy(css = "input[formcontrolname='confirmPassword'], input[formcontrolname='confirm']")
    private WebElement confirmPasswordInput;

    @FindBy(css = "mat-select[formcontrolname='gender'], select[formcontrolname='gender']")
    private WebElement genderDropdown;

    @FindBy(css = "input[formcontrolname='dob']")
    private WebElement dobInput;

    @FindBy(css = "button[type='submit']")
    private WebElement registerButton;

    @FindBy(css = "mat-error, .error-message, .field-error")
    private WebElement errorMessage;

    @FindBy(css = ".success-message, .snack-bar-success")
    private WebElement successMessage;

    public SignupPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public void enterName(String name) {
        wait.until(ExpectedConditions.visibilityOf(nameInput));
        nameInput.clear();
        nameInput.sendKeys(name);
    }

    public void enterEmail(String email) {
        emailInput.clear();
        emailInput.sendKeys(email);
    }

    public void enterPhone(String phone) {
        phoneInput.clear();
        phoneInput.sendKeys(phone);
    }

    public void enterPassword(String password) {
        passwordInput.clear();
        passwordInput.sendKeys(password);
    }

    public void enterConfirmPassword(String password) {
        confirmPasswordInput.clear();
        confirmPasswordInput.sendKeys(password);
    }

    public void enterDob(String dob) {  // format: YYYY-MM-DD
        dobInput.clear();
        dobInput.sendKeys(dob);
    }

    public void clickRegister() {
        wait.until(ExpectedConditions.elementToBeClickable(registerButton));
        registerButton.click();
    }

    /** Full signup in one call */
    public void signup(String name, String email, String phone,
                       String password, String dob) {
        enterName(name);
        enterEmail(email);
        enterPhone(phone);
        enterPassword(password);
        enterConfirmPassword(password);
        enterDob(dob);
        clickRegister();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public boolean isErrorDisplayed() {
        try {
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
