package pages;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;
import utils.ToastOcrHandler;

import java.time.Duration;

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
        } catch (Exception ignored) {
            System.out.println(ignored);
        }

        enterEmail(email);
        hideKeyboardIfShown();
        clickContinue();
    }

    public void enterPassword(String password) {
        By passwordField = ElementRegistry.get( ElementKey.PASSWORD_FIELD);
        type(passwordField, password);
    }



    public void submitPasswordStage(String password, String code) {
        By cancelBtn = ElementRegistry.get( ElementKey.CANCEL_BUTTON_CREATION);
        By codeField = ElementRegistry.get( ElementKey.VERIFICATION_FIELD);
        try {
            click(cancelBtn);
        } catch (Exception ignored) {
            System.out.println(ignored);
        }

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
        waitClickable(countryDropdownField).click();
        scrollToElementAndClick(targetCountry);
        waitClickable(finishButton).click();
    }

    public void IsAllowButtonExisit() {
        By allowButton = ElementRegistry.get( ElementKey.ALLOW_CONTACT_BUTTON);
        click(allowButton);
        click(allowButton);
    }


    public boolean CancelUploadAvatarProcess() {

        click(ElementRegistry.get( ElementKey.CHANGE_AVATAR));
        click(ElementRegistry.get( ElementKey.UPLOAD_PHOTO));
        click(ElementRegistry.get( ElementKey.SELECT_USER_PHONE_PHOTO));
        click(ElementRegistry.get( ElementKey.JUST_ONE_SELECT));
        click(ElementRegistry.get( ElementKey.CANCEL_BUTTON));
        click(ElementRegistry.get( ElementKey.NAVIGATE_BACK));
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
                System.out.println("The Real Ocr Found is : "+normalizedOcr);
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
        click(ElementRegistry.get(ElementKey.OK_BUTTON));

    }
    public void clickSignIn() {
        click(ElementRegistry.get(ElementKey.SIGN_IN_BUTTON));
    }
    public void restScenarioTest3(){
        hideKeyboardIfShown();
        clickOkButton();
        hideKeyboardIfShown();
        clickSignIn();
        clickOkButton();
    }

    public void clickSubmitWithoutInputs() {
        hideKeyboardIfShown();
        click(ElementRegistry.get( ElementKey.SIGN_UP_BUTTON));
        clickContinue();
    }

    public boolean IsVerificationFieldExist() {
        By codeField = ElementRegistry.get( ElementKey.VERIFICATION_FIELD);
        return isDisplayed(codeField);
    }

    public void clickNoButton() {
        click(ElementRegistry.get( ElementKey.NO_BUTTON));
    }
}