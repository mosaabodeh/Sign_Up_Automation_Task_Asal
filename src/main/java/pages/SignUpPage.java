package pages;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import utils.ToastOcrHandler;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class SignUpPage extends BasePage {

    public SignUpPage(AndroidDriver driver) {
        super(driver);
    }

   /* public boolean isEmailFieldVisible() {
        return waitForVisibility(ElementsPage.SignUpField).isDisplayed();
    }*/

    protected void enterEmail(String email) {
        sendKeys(ElementsPage.EMAIL_FIELD, email);
    }

    protected void clickContinue() {
        click(ElementsPage.CONTINUE_BUTTON);
    }


    public void submitEmailStage(String email) {
        waitForClickability(ElementsPage.SignUpField).click();
        enterEmail(email);
        try {
            if (driver.isKeyboardShown()) {
                driver.hideKeyboard();
            }
        } catch (Exception e) {
            System.out.println("⚠️ Note: Keyboard dismiss bypassed before Continue click.");
        }
        clickContinue();
    }

    protected void enterVerificationCode(String registrationCode) {
        waitForVisibility(ElementsPage.VERIFICATION_FIELD).sendKeys(registrationCode);
    }
    public boolean IsVerificationFieldExisit(){
      return   waitForVisibility(ElementsPage.VERIFICATION_FIELD).isDisplayed();
    }
    public boolean IsAllowButtonExisit(){
        return   waitForVisibility(ElementsPage.ALLow_CONTACT_BUTTON).isDisplayed();
    }
    public boolean IsContinueButtonApear(){
        return   waitForVisibility(ElementsPage.Continue_BUTTON_ASSErtion).isDisplayed();
    }


    protected void enterPassword(String password) {
        waitForVisibility(ElementsPage.PASSWORD_FIELD).sendKeys(password);
    }

    protected void clickTermsButton() {
        WebElement element = waitForClickability(ElementsPage.TERMS_CHECKBOX);
        int xOffset = -(element.getSize().getWidth() / 2) + 35;

        new Actions(driver)
                .moveToElement(element, xOffset, 0)
                .click()
                .perform();
    }
    public void UploadPhotoAvatar(){
        waitForClickability(ElementsPage.ALLow_CONTACT_BUTTON).click();
        waitForClickability(ElementsPage.ALLow_CONTACT_BUTTON).click();
        waitForClickability(ElementsPage.CHANGE_AVATAR).click();
        waitForClickability(ElementsPage.UPLOAD_PHOTO).click();
        waitForClickability(ElementsPage.SELECT_USER_PHONE_PHOTO).click();
        waitForClickability(ElementsPage.JUST_ONE_SELECT).click();
        waitForClickability(ElementsPage.DONE_BUTTON).click();
        waitForClickability(ElementsPage.SAVE_BUTTON).click();

    }
    public void CancelUploadAvatarProcess(){
        waitForClickability(ElementsPage.ALLow_CONTACT_BUTTON).click();
        waitForClickability(ElementsPage.ALLow_CONTACT_BUTTON).click();
        waitForClickability(ElementsPage.CHANGE_AVATAR).click();
        waitForClickability(ElementsPage.UPLOAD_PHOTO).click();
        waitForClickability(ElementsPage.SELECT_USER_PHONE_PHOTO).click();
        waitForClickability(ElementsPage.JUST_ONE_SELECT).click();
        waitForClickability(ElementsPage.CANCEL_BUTTON).click();
        waitForClickability(ElementsPage.NAVIGATE_BACK).click();
    }

    public void submitPasswordStage(String password, String registrationCode) {
        enterVerificationCode(registrationCode);
        enterPassword(password);
        clickTermsButton();
        clickContinue();
    }

    public void fillPersonalInfo(String firstName, String lastName, String targetCountry)  {
        waitForClickability(ElementsPage.FIRST_NAME_FIELD).sendKeys(firstName);
        waitForClickability(ElementsPage.LAST_NAME_FIELD).sendKeys(lastName);

        if (driver.isKeyboardShown()) {
            driver.hideKeyboard();
        }

        waitForClickability(ElementsPage.COUNTRY_DROPDOWN_FIELD).click();

        scrollToElementAndClick(targetCountry);

        waitForClickability(ElementsPage.FINISH_BUTTON).click();
    }



    public boolean verifyErrorMessageViaOcr(String expectedErrorMessage) {
        String parsedText = ToastOcrHandler.captureAndReadToast(driver);

        parsedText = parsedText.replaceAll("\\s+", " ").trim();

        if (parsedText.contains(expectedErrorMessage)) {
            System.out.println("✅ Test Passed! Validation message [" + expectedErrorMessage + "] detected via OCR.");
            return true;
        } else {
            System.out.println("❌ Test Failed. Expected: [" + expectedErrorMessage + "] but OCR found: " + parsedText);
            return false;
        }
    }

    public void scrollToElementAndClick(String targetText) {
        boolean elementFound = false;
        int maxScrolls = 20;
        int scrollCount = 0;

        // 1. Sanitize the string case early (e.g., "palestine,state Of" -> "palestine")
        String cleanKeyword = targetText.trim().toLowerCase();
        if (cleanKeyword.contains(",")) {
            cleanKeyword = cleanKeyword.split(",")[0].trim();
        }

        WebElement scrollView = ElementsPage.scrollToAndGetCountry();

        // Get screen container bounds
        org.openqa.selenium.Rectangle containerRect = scrollView.getRect();
        int centerX = containerRect.getX() + (containerRect.getWidth() / 2);
        int startY = containerRect.getY() + (int)(containerRect.getHeight() * 0.75);
        int endY = containerRect.getY() + (int)(containerRect.getHeight() * 0.25);

        // Dynamic translation statement optimized for scannability
        String lowerCaseXpath = ".//android.widget.TextView[contains(translate(@text, " +
                "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + cleanKeyword + "')]";

        try {
            // Drop implicit wait down once before entering the fast iteration sequence
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);

            while (!elementFound && scrollCount < maxScrolls) {
                // 3. Scan ONLY inside the active ScrollView container node layout to save search time
                List<WebElement> elements = scrollView.findElements(AppiumBy.xpath(lowerCaseXpath));

                if (!elements.isEmpty()) {
                    WebElement targetItem = elements.getFirst();
                    if (targetItem.isDisplayed()) {
                        try {
                            // Click the parent wrapper container node
                            targetItem.findElement(AppiumBy.xpath("./..")).click();
                        } catch (Exception e) {
                            targetItem.click();
                        }
                        elementFound = true;
                        System.out.println("✅ Target matched and clicked: " + targetText);
                        break;
                    }
                }

                System.out.println("Scrolling list item wrapper... Step " + (scrollCount + 1));

                try {
                    executePhysicalSwipe(centerX, startY, endY);
                    // 150ms allows the Android render thread to finish animating lists safely without catching hiccups
                    Thread.sleep(150);
                } catch (WebDriverException e) {
                    System.out.println("⚠️ Driver hiccup caught during swipe action. Recovering link...");
                    try { Thread.sleep(300); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                scrollCount++;
            }
        } finally {
            // ALWAYS restore your global framework wait safety margin back to its expected default state
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        }

        if (!elementFound) {
            throw new NoSuchElementException("Failed to find and select country matching: " + targetText);
        }
    }

    private void executePhysicalSwipe(int startX, int startY, int endY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipeSequence = new Sequence(finger, 1);

        swipeSequence.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipeSequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipeSequence.addAction(finger.createPointerMove(Duration.ofMillis(350), PointerInput.Origin.viewport(), startX, endY));
        swipeSequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipeSequence));
    }

    public void clickSubmitWithoutInputs() {
        waitForClickability(ElementsPage.SignUpField).click();
        clickContinue();

    }
}