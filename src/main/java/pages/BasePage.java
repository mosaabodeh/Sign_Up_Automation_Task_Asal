package pages;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;
import utils.ConfigReader;
import io.appium.java_client.HasOnScreenKeyboard;
import io.appium.java_client.HidesKeyboard;
import utils.ToastOcrHandler;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

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

    protected boolean isDisplayed(By locator) {
        try {
            return waitVisible(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected void clickTermsButton() {
        By termsCheckbox = ElementRegistry.get(ElementKey.TERMS_CHECKBOX);
        WebElement element = waitClickable(termsCheckbox);
        int xOffset = -(element.getSize().getWidth() / 2) + 35;
        new Actions(driver)
                .moveToElement(element, xOffset, 0)
                .click()
                .perform();
    }

    private void executePhysicalSwipe(int startX, int startY, int endY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipeSequence = new Sequence(finger, 1);

        swipeSequence.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipeSequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        // Changed duration from 350ms to 600ms to give a smooth, realistic inertia momentum across lists
        swipeSequence.addAction(finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), startX, endY));
        swipeSequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipeSequence));
    }

    private void performScrollStep(int centerX, int startY, int endY) {
        try {
            executePhysicalSwipe(centerX, startY, endY);
            // Wait for lists scrolling momentum deceleration animation to finish rendering frame steps
            Thread.sleep(250);
        } catch (WebDriverException e) {
            log.warn("Driver hiccup caught during swipe action. Recovering...", e);
            try {
                Thread.sleep(300);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void scrollToElementAndClick(String targetText) {
        String cleanKeyword = extractCleanKeyword(targetText);
        String lowerCaseXpath = buildLowerCaseXpath(cleanKeyword);

        WebElement activeScrollContainer = waitVisible(By.xpath(
                "//android.widget.ListView | //android.widget.ScrollView | //android.view.View[@scrollable='true']"
        ));

        org.openqa.selenium.Rectangle containerRect = activeScrollContainer.getRect();
        int centerX = containerRect.getX() + (containerRect.getWidth() / 2);
        int startY = containerRect.getY() + (int) (containerRect.getHeight() * 0.80);
        int endY = containerRect.getY() + (int) (containerRect.getHeight() * 0.20);

        boolean elementFound = false;
        int maxScrolls = 20;
        int scrollCount = 0;

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);

            while (!elementFound && scrollCount < maxScrolls) {
                elementFound = tryClickMatchingElement(activeScrollContainer, lowerCaseXpath, targetText);

                if (!elementFound) {
                    log.debug("Scrolling list item wrapper... Step {}", scrollCount + 1);
                    performScrollStep(centerX, startY, endY);
                    scrollCount++;
                }
            }
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        }

        if (!elementFound) {
            throw new NoSuchElementException("Failed to find and select country matching: " + targetText);
        }
    }

    private String extractCleanKeyword(String targetText) {
        String cleanKeyword = targetText.trim().toLowerCase();
        if (cleanKeyword.contains(",")) {
            cleanKeyword = cleanKeyword.split(",")[0].trim();
        }
        return cleanKeyword;
    }

    private String buildLowerCaseXpath(String cleanKeyword) {
        return ".//android.widget.TextView[contains(translate(@text, " +
                "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + cleanKeyword + "')]";
    }

    private boolean tryClickMatchingElement(WebElement container, String lowerCaseXpath, String targetText) {
        // Scan elements strictly relative inside the active overlay list block container
        List<WebElement> elements = container.findElements(By.xpath(lowerCaseXpath));

        if (elements.isEmpty()) {
            return false;
        }

        WebElement targetItem = elements.getFirst();
        if (!targetItem.isDisplayed()) {
            return false;
        }

        clickTargetOrParent(targetItem);
        log.info("Target matched and clicked: {}", targetText);
        return true;
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
                log.debug("OCR result: [{}]", normalizedOcr);
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

    private void clickTargetOrParent(WebElement targetItem) {
        try {
            targetItem.findElement(By.xpath("./..")).click();
        } catch (Exception e) {
            targetItem.click();
        }
    }
    public void clickContinue() {
        By continueBtn = ElementRegistry.get(ElementKey.CONTINUE_BUTTON);
        click(continueBtn);
    }
    public void clickNoButton() {
        click(ElementRegistry.get(ElementKey.NO_BUTTON));
    }
    public void clickTheTwoAllowButtons() {
        By allowButton = ElementRegistry.get(ElementKey.ALLOW_CONTACT_BUTTON);
        click(allowButton);
        click(allowButton);
    }
    public void clickOkButton() {
        click(ElementRegistry.get(ElementKey.OK_BUTTON));
    }

    public void clickSignIn() {
        click(ElementRegistry.get(ElementKey.SIGN_IN_BUTTON));
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