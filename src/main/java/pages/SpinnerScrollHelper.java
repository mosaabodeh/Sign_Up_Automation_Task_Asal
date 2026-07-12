package pages;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class SpinnerScrollHelper {

    private static final Logger log = LoggerFactory.getLogger(SpinnerScrollHelper.class);

    private static final int MAX_SCROLLS = 20;

    private final AppiumDriver driver;
    private final WebDriverWait wait;

    public SpinnerScrollHelper(AppiumDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void scrollToElementAndClick(String targetText) {
        String cleanKeyword = extractCleanKeyword(targetText);
        String lowerCaseXpath = buildLowerCaseXpath(cleanKeyword);

        WebElement activeScrollContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(ElementRegistry.get(ElementKey.SCROLL_CONTAINER_LOCATOR)));

        Rectangle containerRect = activeScrollContainer.getRect();
        int centerX = containerRect.getX() + (containerRect.getWidth() / 2);
        int startY = containerRect.getY() + (int) (containerRect.getHeight() * 0.80);
        int endY = containerRect.getY() + (int) (containerRect.getHeight() * 0.20);

        boolean elementFound = false;
        int scrollCount = 0;

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);

            while (!elementFound && scrollCount < MAX_SCROLLS) {
                elementFound = tryClickMatchingElement(activeScrollContainer, lowerCaseXpath, targetText);

                if (!elementFound) {
                    log.debug("Target not visible yet. Scrolling... Step {}", scrollCount + 1);
                    executePhysicalSwipe(centerX, startY, endY);
                    waitForLastChildToRender(activeScrollContainer);
                    scrollCount++;
                }
            }
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        }

        if (!elementFound) {
            throw new NoSuchElementException("Failed to find and select item matching: " + targetText);
        }
    }

    private void waitForLastChildToRender(WebElement scrollContainer) {
        By lastChildLocator = By.xpath("./*[last()]");

        try {
            wait.until(d -> {
                List<WebElement> children = scrollContainer.findElements(lastChildLocator);
                if (children.isEmpty()) {
                    return false;
                }
                WebElement lastChild = children.getLast();
                return lastChild.isDisplayed();
            });
            log.debug("Last visible child rendered — scroll settled.");
        } catch (TimeoutException e) {
            log.debug("Timed out waiting for last child to render, proceeding anyway.");
        }
    }

    private String extractCleanKeyword(String targetText) {
        // Takes the part before the comma, if present, e.g. "France, Europe" -> "france"
        String cleanKeyword = targetText.trim().toLowerCase();
        if (cleanKeyword.contains(",")) {
            cleanKeyword = cleanKeyword.split(",")[0].trim();
        }
        return cleanKeyword;
    }

    private String buildLowerCaseXpath(String cleanKeyword) {
        // Finds a TextView whose text contains the given keyword, case-insensitively
        return ".//android.widget.TextView[contains(translate(@text, " +
                "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + cleanKeyword + "')]";
    }

    private boolean tryClickMatchingElement(WebElement container, String lowerCaseXpath, String targetText) {
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

    private void clickTargetOrParent(WebElement targetItem) {
        try {
            targetItem.findElement(By.xpath("./..")).click();
        } catch (Exception e) {
            targetItem.click();
        }
    }

    private void executePhysicalSwipe(int startX, int startY, int endY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipeSequence = new Sequence(finger, 1);

        swipeSequence.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipeSequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipeSequence.addAction(finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), startX, endY));
        swipeSequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipeSequence));
    }
}