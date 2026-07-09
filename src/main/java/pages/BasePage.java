package pages;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
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
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class BasePage {



    protected AppiumDriver driver;
    protected WebDriverWait wait;

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
        By termsCheckbox = ElementRegistry.get( ElementKey.TERMS_CHECKBOX);
        org.openqa.selenium.WebElement element = waitClickable(termsCheckbox);
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
            System.out.println("⚠️ Driver hiccup caught during swipe action. Recovering link...");
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
                    System.out.println("Scrolling list item wrapper... Step " + (scrollCount + 1));
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
        System.out.println("✅ Target matched and clicked: " + targetText);
        return true;
    }

    private void clickTargetOrParent(WebElement targetItem) {
        try {
            targetItem.findElement(By.xpath("./..")).click();
        } catch (Exception e) {
            targetItem.click();
        }
    }


    protected void hideKeyboardIfShown() {
        try {
            if (driver instanceof HasOnScreenKeyboard && driver instanceof HidesKeyboard) {
                boolean shown = ((HasOnScreenKeyboard) driver).isKeyboardShown();
                System.out.println("⌨️ Keyboard shown status: " + shown);

                if (shown) {
                    ((HidesKeyboard) driver).hideKeyboard();
                    System.out.println("⌨️ hideKeyboard() called.");
                }
            } else {
                System.out.println("⚠️ Driver does not support keyboard visibility APIs.");
            }
        } catch (Exception e) {
            System.out.println("⚠️ hideKeyboardIfShown() failed: " + e.getMessage());
        }
    }
}