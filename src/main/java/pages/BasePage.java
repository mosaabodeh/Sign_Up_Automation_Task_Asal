package pages;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;
import utils.ConfigReader;
import io.appium.java_client.HasOnScreenKeyboard;
import io.appium.java_client.HidesKeyboard;
import utils.ToastOcrHandler;

import java.time.Duration;

public class BasePage {

    protected AppiumDriver driver;
    protected WebDriverWait wait;
    protected static final Logger log = LoggerFactory.getLogger(BasePage.class);

    public BasePage(AppiumDriver driver) {
        this.driver = driver;

        long timeout = Long.parseLong(
                ConfigReader.getProperty("timeout.explicit", "10")
        );

        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
    }

    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        waitClickable(locator).click();
    }

    protected void type(By locator, String text) {
        WebElement el = waitVisible(locator);
        el.clear();
        el.sendKeys(text);
    }



    public boolean verifyErrorMessageViaOcr(String expectedMessage) {
        String normalizedExpected = expectedMessage.toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        log.info("Target normalized expected: [{}]", normalizedExpected);

        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        boolean isMatchFound = false;

        try {
            isMatchFound = customWait.until(d -> {
                String rawOcrText = ToastOcrHandler.captureAndReadToast(driver);

                if (rawOcrText == null || rawOcrText.isEmpty()) {
                    return false;
                }

                String normalizedOcr = rawOcrText.toLowerCase().replaceAll("[^a-z0-9]", "");
                log.debug("Polling screen via OCR for compressed layout matches...");
                System.out.println(("OCR result: [{}]"+ normalizedOcr));
                return normalizedOcr.contains(normalizedExpected);
            });
        } catch (TimeoutException e) {
            log.warn("Timeout: expected error text not found via OCR within 5 seconds.");
        }

        try {
            By cancelBtn = ElementRegistry.get(ElementKey.CANCEL_BUTTON_CREATION);
            waitClickable(cancelBtn).click();
            log.info("Cancel creation button found and clicked.");
        } catch (Exception e) {
            log.info("Cancel creation button was not visible on screen. Continuing test execution workflow.");
        }

        return isMatchFound;
    }


    public void clickOkButton() {
        click(ElementRegistry.get(ElementKey.OK_BUTTON));
    }

    protected void hideKeyboardIfShown() {
        try {
            if (driver instanceof HasOnScreenKeyboard && driver instanceof HidesKeyboard) {
                boolean shown = ((HasOnScreenKeyboard) driver).isKeyboardShown();
                log.info("Keyboard shown status: {}", shown);

                if (shown) {
                    ((HidesKeyboard) driver).hideKeyboard();
                    log.info("hideKeyboard() called.");
                }
            } else {
                log.warn("Driver does not support keyboard visibility APIs.");
            }
        } catch (Exception e) {
            log.warn("hideKeyboardIfShown() failed: {}", e.getMessage());
        }
    }
}