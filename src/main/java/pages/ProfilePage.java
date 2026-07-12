package pages;

import io.appium.java_client.AppiumDriver;

import org.openqa.selenium.By;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;

public class ProfilePage extends BasePage{

    public ProfilePage(AppiumDriver driver) {
        super(driver);
    }

    void logOutProcess() {
        click(ElementRegistry.get(ElementKey.USER_MENU));
        click(ElementRegistry.get(ElementKey.LOGOUT_BUTTON));
        click(ElementRegistry.get(ElementKey.LOGOUT_CONFIRM));
    }

    public boolean cancelUploadAvatarAndLogOutProcess() {
        click(ElementRegistry.get(ElementKey.CHANGE_AVATAR));
        click(ElementRegistry.get(ElementKey.UPLOAD_PHOTO));
        click(ElementRegistry.get(ElementKey.SELECT_USER_PHONE_PHOTO));
        click(ElementRegistry.get(ElementKey.JUST_ONE_SELECT));
        click(ElementRegistry.get(ElementKey.CANCEL_BUTTON));
        click(ElementRegistry.get(ElementKey.NAVIGATE_BACK));
        click(ElementRegistry.get(ElementKey.USER_MENU));
        logOutProcess();
        return isDisplayed(ElementRegistry.get(ElementKey.EMAIL_FIELD));
    }
    protected boolean isDisplayed(By locator) {
        try {
            return waitVisible(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    public void dismissOkAndSignInPrompt() {
        hideKeyboardIfShown();
        clickOkButton();
    }

    public boolean isVerificationFieldExist() {
        By codeField = ElementRegistry.get(ElementKey.VERIFICATION_FIELD);
        return isDisplayed(codeField);
    }
}
