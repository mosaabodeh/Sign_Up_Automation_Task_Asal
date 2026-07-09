package pages.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.Map;

public class ElementRegistry {

    private static final Map<String, Map<ElementKey, By>> REGISTRY = new HashMap<>();
    static {
        Map<ElementKey, By> mobile = new HashMap<>();

        // Permissions
        mobile.put(ElementKey.ALLOW_CONTACT_BUTTON,
                By.id("com.android.permissioncontroller:id/permission_allow_button"));

        // Avatar
        mobile.put(ElementKey.CHANGE_AVATAR,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.view.View\").instance(18)"));

        mobile.put(ElementKey.UPLOAD_PHOTO,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.view.View\").instance(5)"));

        mobile.put(ElementKey.SELECT_USER_PHONE_PHOTO,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.view.View\").instance(5)"));

        mobile.put(ElementKey.DONE_BUTTON,
                By.id("com.sec.android.gallery3d:id/menu_edit_app_bar_done"));

        mobile.put(ElementKey.SAVE_BUTTON,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.view.View\").instance(12)"));

        mobile.put(ElementKey.CANCEL_BUTTON,
                AppiumBy.id("com.sec.android.gallery3d:id/menu_edit_app_bar_cancel"));

        // Navigation
        mobile.put(ElementKey.NAVIGATE_BACK,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.view.View\").instance(19)"));

        // Continue
        mobile.put(ElementKey.CONTINUE_BUTTON,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().text(\"Continue\")"));

        mobile.put(ElementKey.CONTINUE_BUTTON_ASSERTION,
                AppiumBy.id("com.ale.rainbow:id/continueButton"));

        // Gallery
        mobile.put(ElementKey.JUST_ONE_SELECT,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().resourceId(\"android:id/button_once\")"));

        // Signup
        mobile.put(ElementKey.SIGNUP_FIELD,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.widget.Button\").instance(1)"));

        mobile.put(ElementKey.TERMS_CHECKBOX,
                AppiumBy.androidUIAutomator(
                        "new UiSelector()"
                                + ".className(\"android.widget.CheckBox\")"
                                + ".packageName(\"com.ale.rainbow\")"
                                + ".checkable(true)"));


        mobile.put(ElementKey.FIRST_NAME_FIELD,
                By. xpath(
                        "//android.widget.EditText[./android.widget.TextView[contains(@text,'First name')]]"));

        mobile.put(ElementKey.LAST_NAME_FIELD,
                By.xpath(
                        "//android.widget.EditText[./android.widget.TextView[contains(@text,'Last name')]]"));

        mobile.put(ElementKey.COUNTRY_DROPDOWN_FIELD,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().text(\"France\")"));

        mobile.put(ElementKey.FINISH_BUTTON,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.view.View\").instance(10)"));

        mobile.put(ElementKey.CANCEL_BUTTON_CREATION,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.view.View\").instance(5)"));

        // Authentication
        mobile.put(ElementKey.EMAIL_FIELD,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.widget.EditText\").instance(0)"));
        mobile.put(ElementKey.OK_BUTTON,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.view.View\").instance(4)"));
        mobile.put(ElementKey.SIGN_IN_BUTTON,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.view.View\").instance(12)"));
        mobile.put(ElementKey.NO_BUTTON,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.widget.Button\").instance(1)"));
        mobile.put(ElementKey.SIGN_UP_BUTTON,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.view.View\").instance(10)"));

        mobile.put(ElementKey.PASSWORD_FIELD,
                By.xpath(
                        "//android.widget.EditText[.//android.widget.TextView[@text='Enter a password']]"));

        mobile.put(ElementKey.VERIFICATION_FIELD,
                By.xpath(
                        "//android.widget.EditText[.//android.widget.TextView[@text='Verification code']]"));
       mobile.put(
                ElementKey.USER_MENU,
                By.xpath("//androidx.compose.ui.platform.ComposeView[@resource-id='com.ale.rainbow:id/compose_view']/android.view.View/android.view.View[1]")
        );


        mobile.put(
                ElementKey.LOGOUT_BUTTON,
                AppiumBy.id("com.ale.rainbow:id/drawer_exit")
        );

        mobile.put(
                ElementKey.LOGOUT_CONFIRM,
                AppiumBy.id("android:id/button1")
        );
        REGISTRY.put("android", mobile);    }

    public static By get(ElementKey key) {
        Map<ElementKey, By> map = REGISTRY.get("android");

        if (map == null) {
            throw new IllegalArgumentException(
                    "No locator registry found for platform: android");
        }

        By locator = map.get(key);
        if (locator == null) {
            throw new IllegalArgumentException(
                    "Locator not found for key: " + key + " on platform: android");
        }

        return locator;
    }
}