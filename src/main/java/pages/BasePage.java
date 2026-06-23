package pages;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;
import utils.ToastOcrHandler;
import java.time.Duration;

public class BasePage {
    protected AndroidDriver driver;
    protected static WebDriverWait wait;

    public BasePage(AndroidDriver driver) {
        this.driver = driver;
        String timeoutProp = ConfigReader.getProperty("timeout.explicit");
        long defaultTimeout = (timeoutProp != null) ? Long.parseLong(timeoutProp) : 10;
        wait = new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout));
    }

    protected static WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickability(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        waitForClickability(locator).click();
    }



    protected void sendKeys(By locator, String text) {
        WebElement element = waitForVisibility(locator);
        element.click();
        element.clear();
        element.sendKeys(text);

    }
    public void toastMessage() {

        String parsedText = ToastOcrHandler.captureAndReadToast(driver);

        parsedText = parsedText.replaceAll("\\s+", " ");

        String expectedErrorMessage = "Please enter a valid e-mail address!";

        if (parsedText.contains(expectedErrorMessage)) {
            System.out.println("✅ Test Passed! Validation message detected via OCR.");
        } else {
            System.out.println("❌ Test Failed. Expected: '" + expectedErrorMessage + "' but OCR found: " + parsedText);
        }
    }

    protected void implicitWait(long duration){
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(duration));
    }

}