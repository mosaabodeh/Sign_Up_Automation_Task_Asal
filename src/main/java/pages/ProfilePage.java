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

        boolean signUpSucceeded = isDisplayed(ElementRegistry.get(ElementKey.USER_MENU));
        if (signUpSucceeded) {
            click(ElementRegistry.get(ElementKey.USER_MENU));
        }
        return signUpSucceeded;
    }

    public void logOutIfLoggedIn() {
        if (isUserLoggedIn()) {
            System.out.println("👤 >>> Existing logged-in session detected. Logging out...");
            try {
                driver.findElement(ElementRegistry.get(ElementKey.USER_MENU)).click();
                driver.findElement(ElementRegistry.get(ElementKey.LOGOUT_BUTTON)).click();
                driver.findElement(ElementRegistry.get(ElementKey.LOGOUT_CONFIRM)).click();
                System.out.println("✅ >>> Logout completed successfully.");
            } catch (Exception e) {
                System.out.println("⚠️ Logout attempt failed: " + e.getMessage());
            }
        }
    }

    private boolean isUserLoggedIn() {
        try {
            By userMenuLocator = ElementRegistry.get(ElementKey.USER_MENU);
            return driver.findElement(userMenuLocator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void dismissOkAndSignInPrompt() {
        hideKeyboardIfShown();
        clickOkButton();
    }


}
