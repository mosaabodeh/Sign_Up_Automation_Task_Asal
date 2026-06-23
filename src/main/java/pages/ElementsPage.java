package pages;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ElementsPage extends BasePage{
    public static final By ALLow_CONTACT_BUTTON=By.id("com.android.permissioncontroller:id/permission_allow_button");
    public static final By CHANGE_AVATAR=AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(22)");
    public static final By UPLOAD_PHOTO=AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(5)");
    public static final By SELECT_USER_PHONE_PHOTO=AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(5)");
    public static final By JUST_ONE_SELECT=AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"android:id/button_once\")");
    public static final By DONE_BUTTON=By.id("com.sec.android.gallery3d:id/menu_edit_app_bar_done");
    public static final By SAVE_BUTTON=AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(12)");


    public static final By CONTINUE_BUTTON = AppiumBy.androidUIAutomator(
            "new UiSelector().text(\"Continue\")");
    public static final By SignUpField = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.Button\").instance(1)");

    public static final By TERMS_CHECKBOX = AppiumBy.androidUIAutomator(
            "new UiSelector()"
                    + ".className(\"android.widget.CheckBox\")"
                    + ".packageName(\"com.ale.rainbow\")"
                    + ".checkable(true)");

    public static final By FIRST_NAME_FIELD = AppiumBy.xpath(
            "//android.widget.EditText[./android.widget.TextView[contains(@text, 'First name')]]");

    public static final By LAST_NAME_FIELD = AppiumBy.xpath(
            "//android.widget.EditText[./android.widget.TextView[contains(@text, 'Last name')]]");

    public static final By COUNTRY_DROPDOWN_FIELD = AppiumBy.androidUIAutomator("new UiSelector().text(\"France\")");

    public ElementsPage(AndroidDriver driver) {
        super(driver);
    }

    public static WebElement scrollToAndGetCountry() {
        return waitForVisibility(AppiumBy.className("android.widget.ScrollView"));
    }

public static final By FINISH_BUTTON=AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(10)");
    public static final By EMAIL_FIELD = AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.EditText\").instance(0)");


    static final By VERIFICATION_FIELD = AppiumBy.xpath(
            "//android.widget.EditText[.//android.widget.TextView[@text='Verification code']]");
    public static final By PASSWORD_FIELD =
         AppiumBy.xpath("//android.widget.EditText[.//android.widget.TextView[@text='Enter a password']]");


}
