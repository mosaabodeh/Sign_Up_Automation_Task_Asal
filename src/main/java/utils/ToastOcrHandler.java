package utils;

import io.appium.java_client.AppiumDriver;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.openqa.selenium.OutputType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ToastOcrHandler {

    private static final String PROJECT_ROOT = System.getProperty("user.dir");


    private static final ThreadLocal<Tesseract> TESSERACT_THREAD = ThreadLocal.withInitial(() -> {
        Tesseract instance = new Tesseract();
        String tessDataPath = PROJECT_ROOT + File.separator + "src"
                + File.separator + "test"
                + File.separator + "resources"
                + File.separator + "tessdata";
        instance.setDatapath(tessDataPath);
        instance.setLanguage("eng");
        return instance;
    });

    public static String captureAndReadToast(AppiumDriver driver) {
        File screenshot = driver.getScreenshotAs(OutputType.FILE);
        File tempFile = null;

        try {
            tempFile = Files.createTempFile("toast_", ".png").toFile();
            org.apache.commons.io.FileUtils.copyFile(screenshot, tempFile);
            return TESSERACT_THREAD.get().doOCR(tempFile);

        } catch (IOException | TesseractException e) {
            System.out.println("⚠️ OCR capture/read failed: " + e.getMessage());
            return "OCR_ERROR: " + e.getMessage();
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                tempFile.deleteOnExit();
            }
        }
    }
}