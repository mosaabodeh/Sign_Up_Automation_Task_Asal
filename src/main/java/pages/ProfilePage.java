package pages;

import io.appium.java_client.AppiumDriver;

import org.openqa.selenium.By;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;

public class ProfilePage extends BasePage{

    public ProfilePage(AppiumDriver driver) {
        super(driver);
    }



    public boolean cancelUploadAvatarAndLogOutProcess() {
        click(ElementRegistry.get(ElementKey.CHANGE_AVATAR));
        click(ElementRegistry.get(ElementKey.UPLOAD_PHOTO));
        click(ElementRegistry.get(ElementKey.SELECT_USER_PHONE_PHOTO));
        click(ElementRegistry.get(ElementKey.JUST_ONE_SELECT));
        click(ElementRegistry.get(ElementKey.CANCEL_BUTTON));
        click(ElementRegistry.get(ElementKey.NAVIGATE_BACK));
        click(ElementRegistry.get(ElementKey.USER_MENU));

        return true;
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
