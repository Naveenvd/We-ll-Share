package com.wellshare.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

/**
 * Manages the ExtentReports HTML report
 * Creates a single report file for the entire test run
 */
public class ExtentReportManager {

    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    public static ExtentReports getInstance() {
        if (extent == null) {
            String reportPath = ConfigReader.get("reports.path") + "WellShare_TestReport.html";

            ExtentSparkReporter reporter = new ExtentSparkReporter(reportPath);
            reporter.config().setDocumentTitle("We'll Share — Automation Report");
            reporter.config().setReportName("Selenium Test Results");
            reporter.config().setTheme(Theme.DARK);
            reporter.config().setTimeStampFormat("dd-MM-yyyy HH:mm:ss");

            extent = new ExtentReports();
            extent.attachReporter(reporter);

            // System info shown in report header
            extent.setSystemInfo("Application", "We'll Share");
            extent.setSystemInfo("Environment", "Local — http://localhost:4200");
            extent.setSystemInfo("Browser", ConfigReader.get("browser"));
            extent.setSystemInfo("Tester", "SDET Automation");
        }
        return extent;
    }

    public static void setTest(ExtentTest extentTest) {
        test.set(extentTest);
    }

    public static ExtentTest getTest() {
        return test.get();
    }

    public static void flush() {
        if (extent != null) extent.flush();
    }
}
