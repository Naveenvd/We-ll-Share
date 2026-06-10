package com.wellshare.base;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.wellshare.utils.ConfigReader;
import com.wellshare.utils.ExtentReportManager;
import com.wellshare.utils.ScreenshotUtil;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * ─────────────────────────────────────────────────────────
 *  BaseTest — parent class for ALL test classes
 *
 *  Responsibilities:
 *  1. Launch and quit browser before/after each test
 *  2. Set implicit and explicit waits
 *  3. Take screenshot on failure
 *  4. Log results to ExtentReports
 * ─────────────────────────────────────────────────────────
 */
public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl = ConfigReader.get("base.url");

    // ── Before Suite: initialize the report ────────────────────────
    @BeforeSuite
    public void initReport() {
        ExtentReportManager.getInstance();
    }

    // ── Before Each Test: open browser ─────────────────────────────
    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        String testName = "Test-" + Thread.currentThread().getId() + "-" + System.currentTimeMillis();

        String browser  = ConfigReader.get("browser").toLowerCase();
        boolean headless = ConfigReader.getBoolean("headless");

        // Setup driver based on config
        switch (browser) {
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver();
            }
            default -> {
                // Chrome (default)
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                if (headless) {
                    options.addArguments("--headless=new");
                    options.addArguments("--window-size=1920,1080");
                }
                options.addArguments("--disable-notifications");
                options.addArguments("--no-sandbox");
                driver = new ChromeDriver(options);
            }
        }

        // Waits
        driver.manage().timeouts().implicitlyWait(
            Duration.ofSeconds(ConfigReader.getInt("implicit.wait"))
        );
        driver.manage().timeouts().pageLoadTimeout(
            Duration.ofSeconds(ConfigReader.getInt("page.load.timeout"))
        );
        wait = new WebDriverWait(driver,
            Duration.ofSeconds(ConfigReader.getInt("explicit.wait"))
        );

        // Maximize window
        driver.manage().window().maximize();

        // Create ExtentReport test node
        ExtentTest extentTest = ExtentReportManager.getInstance()
            .createTest(testName);
        ExtentReportManager.setTest(extentTest);
    }

    // ── After Each Test: capture result + quit browser ─────────────
    @AfterMethod
    public void tearDown(ITestResult result) {
        ExtentTest extentTest = ExtentReportManager.getTest();

        if (result.getStatus() == ITestResult.FAILURE) {
            // Take screenshot on failure
            String screenshotPath = ScreenshotUtil.capture(
                driver, result.getMethod().getMethodName()
            );
            extentTest.fail("❌ Test FAILED: " + result.getThrowable().getMessage());
            extentTest.addScreenCaptureFromPath(screenshotPath);

        } else if (result.getStatus() == ITestResult.SUCCESS) {
            extentTest.log(Status.PASS, "✅ Test PASSED");

        } else {
            extentTest.log(Status.SKIP, "⚠️ Test SKIPPED");
        }

        if (driver != null) driver.quit();
    }

    // ── After Suite: write the HTML report ─────────────────────────
    @AfterSuite
    public void flushReport() {
        ExtentReportManager.flush();
        System.out.println("📊 Report generated at: reports/WellShare_TestReport.html");
    }

    // ── Helper: navigate to a URL ───────────────────────────────────
    protected void navigateTo(String url) {
        driver.get(url);
        ExtentReportManager.getTest().info("Navigated to: " + url);
    }
}
