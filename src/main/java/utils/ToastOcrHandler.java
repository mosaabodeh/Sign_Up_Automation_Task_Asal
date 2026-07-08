package utils;

import io.appium.java_client.AppiumDriver;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ToastOcrHandler {

    private static final Tesseract tesseract = new Tesseract();
    private static final String PROJECT_ROOT = System.getProperty("user.dir");

    static {
        String tessdataPath = PROJECT_ROOT + File.separator + "src"
                + File.separator + "test"
                + File.separator + "resources"
                + File.separator + "tessdata";

        tesseract.setDatapath(tessdataPath);
        tesseract.setLanguage("eng");
    }

    public static String captureAndReadToast(AppiumDriver driver) {
        File screenshot = ( driver).getScreenshotAs(OutputType.FILE);

        try {
            File tempFile = Files.createTempFile("toast_", ".png").toFile();
            org.apache.commons.io.FileUtils.copyFile(screenshot, tempFile);

            return tesseract.doOCR(tempFile);

        } catch (IOException | TesseractException e) {
            return "OCR_ERROR: " + e.getMessage();
        }
    }
}