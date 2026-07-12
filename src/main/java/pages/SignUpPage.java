package pages;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;

public class SignUpPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(SignUpPage.class);

    public SignUpPage(AppiumDriver driver) {
        super(driver);
    }

    public void enterEmail(String email) {
        By emailField = ElementRegistry.get(ElementKey.EMAIL_FIELD);
        type(emailField, email);
    }

    public void submitEmailStage(String email) {
        click( ElementRegistry.get(ElementKey.SIGNUP_FIELD));

        try {
            click(ElementRegistry.get(ElementKey.CANCEL_BUTTON_CREATION));
        } catch (Exception e) {
            log.debug("Cancel button not present during email stage, continuing.", e);
        }
        enterEmail(email);
        hideKeyboardIfShown();
        clickContinue();
    }

    public void enterPassword(String password) {
        type(ElementRegistry.get(ElementKey.PASSWORD_FIELD), password);
    }

    public void submitPasswordStage(String password, String code) {

        try {
            click(ElementRegistry.get(ElementKey.CANCEL_BUTTON_CREATION));
        } catch (Exception e) {
            log.debug("Cancel button not present during password stage, continuing.", e);
        }

        type(ElementRegistry.get(ElementKey.VERIFICATION_FIELD), code);
        enterPassword(password);
        clickTermsButton();
        clickContinue();
    }
    public void clickNoButton() {
        click(ElementRegistry.get(ElementKey.NO_BUTTON));
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
    public void clickTheTwoAllowButtons() {
        By allowButton = ElementRegistry.get(ElementKey.ALLOW_CONTACT_BUTTON);
        click(allowButton);
        click(allowButton);
    }

    public void fillPersonalInfo(String firstName, String lastName, String targetCountry) {
        SpinnerScrollHelper spinnerScrollHelper=new SpinnerScrollHelper(this.driver);
        waitClickable(ElementRegistry.get(ElementKey.FIRST_NAME_FIELD)).sendKeys(firstName);
        waitClickable(ElementRegistry.get(ElementKey.LAST_NAME_FIELD)).sendKeys(lastName);
        hideKeyboardIfShown();


        spinnerScrollHelper.waitClickable(ElementRegistry.get(ElementKey.COUNTRY_DROPDOWN_FIELD)).click();
        spinnerScrollHelper.scrollToElementAndClick(targetCountry);
        spinnerScrollHelper.waitClickable(ElementRegistry.get(ElementKey.FINISH_BUTTON)).click();
    }

    public void clickSubmitWithoutInputs() {
        hideKeyboardIfShown();
        click(ElementRegistry.get(ElementKey.SIGN_UP_BUTTON));
        clickContinue();
    }


    public void ensureSignUpScreenReady() {
        waitVisible(ElementRegistry.get(ElementKey.SIGNUP_FIELD));
        log.info("Sign-up screen confirmed ready.");
    }
}