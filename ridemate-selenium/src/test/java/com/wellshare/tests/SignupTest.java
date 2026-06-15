package com.wellshare.tests;

import com.wellshare.base.BaseTest;
import com.wellshare.pages.SignupPage;
import com.wellshare.utils.ConfigReader;
import com.wellshare.utils.ExtentReportManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * ─────────────────────────────────────────────────────────
 *  SignupTest — covers user registration scenarios
 *
 *  TC_SIGNUP_001 - Valid new user signs up successfully
 *  TC_SIGNUP_002 - Duplicate email shows error
 *  TC_SIGNUP_003 - Empty required fields keep button disabled
 * ─────────────────────────────────────────────────────────
 */
public class SignupTest extends BaseTest {

    private SignupPage signupPage;

    @BeforeMethod(alwaysRun = true)
    public void openSignupPage() {
        navigateTo(ConfigReader.get("app.signup.url"));
        signupPage = new SignupPage(driver);
    }

    // ── TC_SIGNUP_001 ─────────────────────────────────────────────
    @Test(groups = {"smoke", "regression"},
          description = "Valid new user can register and moves away from signup page")
    public void validSignupRedirectsAwayFromSignup() {
        ExtentReportManager.getTest().info("TC_SIGNUP_001 — Valid signup");

        // Generate a unique email so it never conflicts with existing users
        String uniqueEmail = "testuser" + System.currentTimeMillis() + "@wellshare-test.com";

        signupPage.enterName("Test User");
        signupPage.enterEmail(uniqueEmail);
        signupPage.enterPhone("9" + String.valueOf(System.currentTimeMillis()).substring(4, 13));
        signupPage.enterPassword("Test@1234");
        signupPage.enterConfirmPassword("Test@1234");
        signupPage.selectGender("MALE");
        signupPage.enterDob("1998-06-15");
        signupPage.clickRegister();

        // After signup, Angular redirects to phone verification or login — either means success
        wait.until(ExpectedConditions.not(
            ExpectedConditions.urlContains("/signup")
        ));

        Assert.assertFalse(
            driver.getCurrentUrl().contains("/signup"),
            "Should redirect away from /signup after valid registration"
        );

        ExtentReportManager.getTest().info("Redirected to: " + driver.getCurrentUrl());
    }

    // ── TC_SIGNUP_002 ─────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Registering with existing email shows error message")
    public void duplicateEmailShowsError() {
        ExtentReportManager.getTest().info("TC_SIGNUP_002 — Duplicate email");

        signupPage.enterName("Duplicate User");
        signupPage.enterEmail(ConfigReader.get("rider.email")); // already in DB
        signupPage.enterPhone("9876543210");
        signupPage.enterPassword("Test@1234");
        signupPage.enterConfirmPassword("Test@1234");
        signupPage.selectGender("FEMALE");
        signupPage.enterDob("1995-01-01");
        signupPage.clickRegister();

        Assert.assertTrue(
            signupPage.isErrorDisplayed(),
            "Error message should appear for duplicate email"
        );
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/signup"),
            "Should stay on signup page when email is taken"
        );
    }

    // ── TC_SIGNUP_003 ─────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Empty form keeps register button disabled")
    public void emptyFormKeepsButtonDisabled() {
        ExtentReportManager.getTest().info("TC_SIGNUP_003 — Empty form validation");

        // Don't fill anything — just check button state
        Assert.assertTrue(
            signupPage.isRegisterButtonDisabled(),
            "Register button should be disabled when form is empty"
        );
    }
}
