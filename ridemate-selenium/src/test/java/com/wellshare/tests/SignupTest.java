package com.wellshare.tests;

import com.wellshare.base.BaseTest;
import com.wellshare.pages.SignupPage;
import com.wellshare.utils.ConfigReader;
import com.wellshare.utils.ExtentReportManager;
import com.wellshare.utils.TestDataGenerator;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * ─────────────────────────────────────────────────────────
 *  SignupTest — covers user registration scenarios
 *
 *  TC_SIGNUP_001 - Valid new user signup → success
 *  TC_SIGNUP_002 - Duplicate email       → error shown
 *  TC_SIGNUP_003 - Invalid phone number  → validation error
 *  TC_SIGNUP_004 - Short password        → validation error
 *  TC_SIGNUP_005 - Missing name          → form validation
 * ─────────────────────────────────────────────────────────
 */
public class SignupTest extends BaseTest {

    private SignupPage signupPage;

    @BeforeMethod
    public void openSignupPage() {
        navigateTo(ConfigReader.get("app.signup.url"));
        signupPage = new SignupPage(driver);
    }

    // ── TC_SIGNUP_001 ─────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Valid new user can register successfully")
    public void validNewUserSignup() {
        ExtentReportManager.getTest().info("TC_SIGNUP_001 — Valid signup");

        String randomEmail = TestDataGenerator.randomEmail();
        ExtentReportManager.getTest().info("Using test email: " + randomEmail);

        signupPage.signup(
            TestDataGenerator.randomName(),
            randomEmail,
            TestDataGenerator.randomPhone(),
            TestDataGenerator.validPassword(),
            "2000-06-15"
        );

        // After signup, user should be redirected (to phone verify or dashboard)
        Assert.assertFalse(
            driver.getCurrentUrl().contains("/signup"),
            "User should be redirected away from signup after success"
        );
    }

    // ── TC_SIGNUP_002 ─────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Registering with already-used email shows error")
    public void duplicateEmailShowsError() {
        ExtentReportManager.getTest().info("TC_SIGNUP_002 — Duplicate email");

        signupPage.signup(
            "Test User",
            ConfigReader.get("rider.email"),  // already registered email
            TestDataGenerator.randomPhone(),
            TestDataGenerator.validPassword(),
            "1998-03-20"
        );

        Assert.assertTrue(
            signupPage.isErrorDisplayed(),
            "Error message should appear for duplicate email"
        );
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/signup"),
            "User should stay on signup page"
        );
    }

    // ── TC_SIGNUP_003 ─────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Invalid phone number (less than 10 digits) shows error")
    public void invalidPhoneNumberValidation() {
        ExtentReportManager.getTest().info("TC_SIGNUP_003 — Invalid phone");

        signupPage.signup(
            TestDataGenerator.randomName(),
            TestDataGenerator.randomEmail(),
            "12345",              // Invalid: only 5 digits
            TestDataGenerator.validPassword(),
            "2001-08-10"
        );

        Assert.assertTrue(
            signupPage.isErrorDisplayed() ||
            driver.getCurrentUrl().contains("/signup"),
            "Should show error or stay on page for invalid phone"
        );
    }

    // ── TC_SIGNUP_004 ─────────────────────────────────────────────
    @Test(groups = {"regression"},
          description = "Password too short triggers validation error")
    public void shortPasswordShowsValidationError() {
        ExtentReportManager.getTest().info("TC_SIGNUP_004 — Short password");

        signupPage.signup(
            TestDataGenerator.randomName(),
            TestDataGenerator.randomEmail(),
            TestDataGenerator.randomPhone(),
            "123",               // Too short password
            "1999-11-25"
        );

        Assert.assertTrue(
            signupPage.isErrorDisplayed() ||
            driver.getCurrentUrl().contains("/signup"),
            "Short password should not be accepted"
        );
    }
}
