package pages;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
public class SpinnerScrollHelper extends BasePage{

    private static final int MAX_SCROLLS = 20;



    public SpinnerScrollHelper(AppiumDriver driver) {
        super(driver);
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


    private void waitForScrollToSettle(WebElement scrollContainer) {
        AtomicReference<org.openqa.selenium.Point> previous = new AtomicReference<>();

        FluentWait<AppiumDriver> settleWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(2))
                .pollingEvery(Duration.ofMillis(100))
                .ignoring(WebDriverException.class);

        try {
            settleWait.until(d -> {
                org.openqa.selenium.Point current = getFirstChildLocation(scrollContainer);
                boolean settled = current != null && current.equals(previous.get());
                previous.set(current);
                return settled;
            });
            log.debug("Scroll settled — first visible child position stable.");
        } catch (TimeoutException e) {
            log.debug("Scroll settle wait exceeded timeout, proceeding anyway.");
        }
    }

    private org.openqa.selenium.Point getFirstChildLocation(WebElement scrollContainer) {
        try {
            List<WebElement> children = scrollContainer.findElements(By.xpath("./*[1]"));
            return children.isEmpty() ? null : children.getFirst().getLocation();
        } catch (WebDriverException e) {
            log.debug("Could not read first child location mid-scroll, retrying...", e);
            return null;
        }
    }

    private void performScrollStep(int centerX, int startY, int endY, WebElement scrollContainer) {
        executePhysicalSwipe(centerX, startY, endY);
        waitForScrollToSettle(scrollContainer);
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
        int scrollCount = 0;

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);

            while (!elementFound && scrollCount < MAX_SCROLLS) {
                elementFound = tryClickMatchingElement(activeScrollContainer, lowerCaseXpath, targetText);

                if (!elementFound) {
                    log.debug("Scrolling list item wrapper... Step {}", scrollCount + 1);
                    performScrollStep(centerX, startY, endY, activeScrollContainer);
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
        //this Methode to take the Part Before The Comma
        String cleanKeyword = targetText.trim().toLowerCase();
        if (cleanKeyword.contains(",")) {
            cleanKeyword = cleanKeyword.split(",")[0].trim();
        }
        return cleanKeyword;
    }

    private String buildLowerCaseXpath(String cleanKeyword) {
        //element on screen whose text contains the given keyword
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
}
