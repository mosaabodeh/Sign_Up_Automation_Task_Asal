package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;
import utils.ToastOcrHandler;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class SignUpPage extends BasePage {

    public SignUpPage(AppiumDriver driver) {
        super(driver);
    }

    public void enterEmail(String email) {
        By emailField = ElementRegistry.get( ElementKey.EMAIL_FIELD);
        type(emailField, email);
    }

    public void clickContinue() {
        By continueBtn = ElementRegistry.get( ElementKey.CONTINUE_BUTTON);
        click(continueBtn);
    }


    public void submitEmailStage(String email) {
        By signUpButton = ElementRegistry.get( ElementKey.SIGNUP_FIELD);
        click(signUpButton) ;
        By emailField = ElementRegistry.get( ElementKey.EMAIL_FIELD);
        By cancelBtn = ElementRegistry.get( ElementKey.CANCEL_BUTTON_CREATION);

        click(emailField);
        try {
            click(cancelBtn);
        } catch (Exception ignored) {}

        enterEmail(email);
        hideKeyboardIfShown();
        clickContinue();
    }

    public void enterPassword(String password) {
        By passwordField = ElementRegistry.get( ElementKey.PASSWORD_FIELD);
        type(passwordField, password);
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
    public void submitPasswordStage(String password, String code) {
        By cancelBtn = ElementRegistry.get( ElementKey.CANCEL_BUTTON_CREATION);
        By codeField = ElementRegistry.get( ElementKey.VERIFICATION_FIELD);
        try {
            click(cancelBtn);
        } catch (Exception ignored) {}

        type(codeField, code);
        enterPassword(password);
        clickTermsButton();
        clickContinue();
    }
    public void fillPersonalInfo(String firstName, String lastName, String targetCountry) {
        By firstNameField = ElementRegistry.get( ElementKey.FIRST_NAME_FIELD);
        By lastNameField = ElementRegistry.get( ElementKey.LAST_NAME_FIELD);
        By countryDropdownField = ElementRegistry.get( ElementKey.COUNTRY_DROPDOWN_FIELD);
        By finishButton = ElementRegistry.get( ElementKey.FINISH_BUTTON);

        waitClickable(firstNameField).sendKeys(firstName);
        waitClickable(lastNameField).sendKeys(lastName);

        hideKeyboardIfShown();

        // 1. Open the spinner dropdown list overlay
        waitClickable(countryDropdownField).click();

        // 2. Perform the dynamic scrolling search
        scrollToElementAndClick(targetCountry);

        // 3. Confirm selection
        waitClickable(finishButton).click();
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
    public void IsAllowButtonExisit() {
        By allowButton = ElementRegistry.get( ElementKey.ALLOW_CONTACT_BUTTON);
        click(allowButton);
        click(allowButton);
    }


    public boolean CancelUploadAvatarProcess() {
        By changeAvatar = ElementRegistry.get( ElementKey.CHANGE_AVATAR);
        By uploadPhoto = ElementRegistry.get( ElementKey.UPLOAD_PHOTO);

        By selectPhoto = ElementRegistry.get( ElementKey.SELECT_USER_PHONE_PHOTO);

        By JUST_ONE_SELECT = ElementRegistry.get( ElementKey.JUST_ONE_SELECT);
        By CANCEL_BUTTON = ElementRegistry.get( ElementKey.CANCEL_BUTTON);
        By NAVIGATE_BACK = ElementRegistry.get( ElementKey.NAVIGATE_BACK);


        click(changeAvatar);
        click(uploadPhoto);
        click(selectPhoto);
        click(JUST_ONE_SELECT);
        click(CANCEL_BUTTON);
        click(NAVIGATE_BACK);
        click(ElementRegistry.get( ElementKey.USER_MENU));
        click(ElementRegistry.get( ElementKey.USER_MENU));
        click(ElementRegistry.get( ElementKey.LOGOUT_BUTTON));
        click(ElementRegistry.get( ElementKey.LOGOUT_CONFIRM));

        return isDisplayed(ElementRegistry.get( ElementKey.EMAIL_FIELD));

    }

    public boolean verifyErrorMessageViaOcr(String expectedMessage) {
        String normalizedExpected = expectedMessage.toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        System.out.println("==================================================");
        System.out.println("🎯 TARGET NORMALIZED EXPECTED: [" + normalizedExpected + "]");
        System.out.println("==================================================");

        org.openqa.selenium.support.ui.WebDriverWait customWait =
                new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5));

        boolean isMatchFound = false;

        try {
            isMatchFound = customWait.until(d -> {
                String rawOcrText = ToastOcrHandler.captureAndReadToast(driver);

                if (rawOcrText == null || rawOcrText.isEmpty()) {
                    return false;
                }

                String normalizedOcr = rawOcrText.toLowerCase().replaceAll("[^a-z0-9]", "");
                System.out.println("📸 Polling Screen via OCR for compressed layout matches...");

                return normalizedOcr.contains(normalizedExpected);
            });
        } catch (org.openqa.selenium.TimeoutException e) {
            System.out.println("❌ Timeout: The expected error text was not found via OCR within 5 seconds.");
        }

        try {
            By cancelBtn = ElementRegistry.get( ElementKey.CANCEL_BUTTON_CREATION);
            waitClickable(cancelBtn).click();
            System.out.println("✅ Cancel creation button found and clicked.");
        } catch (Exception e) {
            System.out.println("ℹ️ Cancel creation button was not visible on screen. Continuing test execution workflow...");
        }

        return isMatchFound;
    }
    public void clickOkButton() {
        By okButton = ElementRegistry.get( ElementKey.OK_BUTTON);
        click(ElementRegistry.get( ElementKey.OK_BUTTON));
        click(ElementRegistry.get( ElementKey.SIGN_IN_BUTTON));
        click(okButton);
    }

    public void clickSubmitWithoutInputs() {
        clickContinue();

    }


    public boolean IsVerificationFieldExisit() {
        By codeField = ElementRegistry.get( ElementKey.VERIFICATION_FIELD);
        return isDisplayed(codeField);
    }


}