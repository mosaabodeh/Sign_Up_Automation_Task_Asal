package pages;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

public class ElementsPage {

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

    public static By getScrollToCountry(String countryName) {
        return AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(\"" + countryName + "\"))"
        );
    }
    public static final By ToastMessage = By.xpath("//android.widget.Toast[contains(@text,'valid e-mail')]");

public static final By FINISH_BUTTON=AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(10)");
    public static final By EMAIL_FIELD = AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.EditText\").instance(0)");


    static final By VERIFICATION_FIELD = AppiumBy.xpath(
            "//android.widget.EditText[.//android.widget.TextView[@text='Verification code']]");
    public static By password() {
        return AppiumBy.xpath("//android.widget.EditText[.//android.widget.TextView[@text='Enter a password']]");
    }

}
