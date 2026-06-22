package pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class BasePage {
    protected AndroidDriver driver;
    protected WebDriverWait wait;

    public BasePage(AndroidDriver driver) {
        this.driver = driver;
        String timeoutProp = ConfigReader.getProperty("timeout.explicit");
        long defaultTimeout = (timeoutProp != null) ? Long.parseLong(timeoutProp) : 10;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout));
    }

    protected WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickability(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        waitForClickability(locator).click();
    }

    protected void clear(By locator) {
        waitForVisibility(locator).clear();
    }

    protected void doubleClick(By locator) {
        WebElement element = waitForClickability(locator);
        int x = element.getRect().getX() + (element.getRect().getWidth() / 2);
        int y = element.getRect().getY() + (element.getRect().getHeight() / 2);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence doubleTap = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), x, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(100), PointerInput.Origin.viewport(), x, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(doubleTap));
    }

    protected void scrollUp() {
        if (driver == null) return;
        int width = driver.manage().window().getSize().getWidth();
        int height = driver.manage().window().getSize().getHeight();

        int startX = width / 2;
        int startY = (int) (height * 0.20);
        int endX = width / 2;  // ✅ FIXED: Added the missing endX declaration
        int endY = (int) (height * 0.80);

        performScroll(startX, startY, endX, endY);
    }
    private void performScroll(int startX, int startY, int endX, int endY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence scroll = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), startX, startY))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), endX, endY))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(scroll));
    }



    public void swipeUp() {

        org.openqa.selenium.Dimension size = driver.manage().window().getSize();

        int centerX = size.width / 2;

        int startY = (int) (size.height * 0.8);
        int endY = (int) (size.height * 0.2);

        PointerInput finger = new PointerInput(
                PointerInput.Kind.TOUCH,
                "finger"
        );

        Sequence swipe = new Sequence(finger, 1);

        swipe.addAction(
                finger.createPointerMove(
                        Duration.ZERO,
                        PointerInput.Origin.viewport(),
                        centerX,
                        startY
                )
        );

        swipe.addAction(
                finger.createPointerDown(
                        PointerInput.MouseButton.LEFT.asArg()
                )
        );

        swipe.addAction(
                finger.createPointerMove(
                        Duration.ofMillis(700),
                        PointerInput.Origin.viewport(),
                        centerX,
                        endY
                )
        );

        swipe.addAction(
                finger.createPointerUp(
                        PointerInput.MouseButton.LEFT.asArg()
                )
        );

        driver.perform(List.of(swipe));
    }


    protected void sendKeys(By locator, String text) {
        WebElement element = waitForVisibility(locator);
        element.click();
        element.clear();
        element.sendKeys(text);


    }

    protected String getText(By locator) {
        return waitForVisibility(locator).getText();
    }

    protected boolean isDisplayed(By locator) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
    protected void implicitWait(long duration){
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(duration));
    }
    protected void longClickElement(By locator) {
        WebElement element = waitForVisibility(locator);

        int x = element.getRect().getX() + (element.getRect().getWidth() / 2);
        int y = element.getRect().getY() + (element.getRect().getHeight() / 2);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence longClickSequence = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), x, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(2000), PointerInput.Origin.viewport(), x, y))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(longClickSequence));
    }
}