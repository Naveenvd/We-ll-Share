package com.wellshare.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Takes and saves screenshots on test failure
 */
public class ScreenshotUtil {

    private static final String SCREENSHOTS_DIR = ConfigReader.get("screenshots.path");

    /**
     * Takes a screenshot and saves it to the screenshots folder
     * @return absolute path to the saved screenshot (used in ExtentReports)
     */
    public static String capture(WebDriver driver, String testName) {
        try {
            // Create directory if it doesn't exist
            Path dir = Paths.get(SCREENSHOTS_DIR);
            if (!Files.exists(dir)) Files.createDirectories(dir);

            // File name: TestName_2026-06-07_14-30-45.png
            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String fileName = testName + "_" + timestamp + ".png";
            String filePath = SCREENSHOTS_DIR + fileName;

            // Take and save screenshot
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), Paths.get(filePath));

            System.out.println("📸 Screenshot saved: " + filePath);
            return new File(filePath).getAbsolutePath();

        } catch (IOException e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
            return "";
        }
    }
}
