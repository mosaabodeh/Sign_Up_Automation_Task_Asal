package utils;

import io.appium.java_client.AppiumDriver;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;

public class ToastOcrHandler {

    public static String captureAndReadToast(AppiumDriver driver) {
        Tesseract tesseract = new Tesseract();

        // 1. Get the base project directory
        String projectRoot = System.getProperty("user.dir");

        // 2. Explicitly build a clean Windows path using File.separator
        String tessdataPath = projectRoot + File.separator + "src"
                + File.separator + "test"
                + File.separator + "resources"
                + File.separator + "tessdata";

        // 3. Set the clean folder path
        tesseract.setDatapath(tessdataPath);
        tesseract.setLanguage("eng");

        // Set up clean paths for saving the target screenshot
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File destinationFile = new File(projectRoot + File.separator + "target" + File.separator + "toast_screenshot.png");

        try {
            FileUtils.copyFile(screenshot, destinationFile);
            return tesseract.doOCR(destinationFile);
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            return "OCR Error: " + e.getMessage();
        }
    }
}