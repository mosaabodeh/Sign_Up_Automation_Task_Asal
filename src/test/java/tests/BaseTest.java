package tests;

import com.google.common.collect.ImmutableMap;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;
import org.testng.annotations.*;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;
import utils.ConfigReader;

import java.io.File;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseTest {

    private static final ThreadLocal<AndroidDriver> driverThreadLocal = new ThreadLocal<>();
    private static AppiumDriverLocalService server;

    public AndroidDriver getDriver() {
        return driverThreadLocal.get();
    }

    @BeforeSuite
    public void startAppiumServer() {
        AppiumServiceBuilder builder = new AppiumServiceBuilder()
                .withIPAddress("127.0.0.1")
                .usingPort(4723)
                .withArgument(() -> "--allow-cors");

        server = AppiumDriverLocalService.buildService(builder);
        server.start();
        System.out.println(">>> Appium Server started automatically <<<");
    }

    @BeforeClass
    @Parameters({"environment", "systemPort"})
    public void setUp(@Optional("realdevice") String targetEnv, @Optional("8201") String systemPort)  {
        ConfigReader.loadConfig(targetEnv + ".properties");

        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName(ConfigReader.getProperty("platform.name"))
                .setAutomationName(ConfigReader.getProperty("automation.name"))
                .setDeviceName(ConfigReader.getProperty("device.name"))
                .setAppPackage(ConfigReader.getProperty("app.package"))
                .setAppActivity(ConfigReader.getProperty("app.activity"))
                .setNoReset(!isSignUpTestClass())
                .setFullReset(false);

        options.setSystemPort(Integer.parseInt(systemPort));
        options.setCapability("appium:resetKeyboard", true);
        options.setCapability("appium:ensureWebviewsHavePages", true);
        options.setCapability("appium:settings[allowWindowOcclusion]", true);
        options.setCapability("appium:includeWindows", false);
        String udid = ConfigReader.getProperty("device.udid");
        if (udid != null && !udid.isEmpty()) {
            options.setUdid(udid);
            System.out.println("🎯 >>> Target device UDID set to: " + udid);
        }

        AndroidDriver currentDriver = new AndroidDriver(server.getUrl(), options);
        driverThreadLocal.set(currentDriver);
        System.out.println("📱 >>> Driver initialized successfully for environment: " + targetEnv);
    }

    @BeforeMethod
    public void launchAppCleanly() {
        if (getDriver() != null) {
            String appPackage = ConfigReader.getProperty("app.package");

            try {
                System.out.println("🔄 >>> Cycling application state...");
                getDriver().terminateApp(appPackage);
                getDriver().activateApp(appPackage);

                logOutIfLoggedIn();

                if (isSignUpTestClass()) {
                    System.out.println("🔗 >>> Aligning target view via Sign-Up Deep Link routing...");
                    triggerDeepLink("rainbow://signup", appPackage);
                } else {
                    System.out.println("🔗 >>> Aligning target view via Home Deep Link routing...");
                    triggerDeepLink("rainbow://openrainbow.net", appPackage);
                }

                System.out.println("✅ >>> App is active and ready for test execution.");

                waitForScreenReady();
            } catch (Exception e) {
                System.out.println("⚠️ Fallback: Forcing basic app activation due to: " + e.getMessage());
                getDriver().activateApp(appPackage);
            }
        }
    }

    private void logOutIfLoggedIn() {
        if (isUserLoggedIn()) {
            System.out.println("👤 >>> Existing logged-in session detected. Logging out...");
            try {
                getDriver().findElement(ElementRegistry.get(ElementKey.USER_MENU)).click();
                getDriver().findElement(ElementRegistry.get(ElementKey.LOGOUT_BUTTON)).click();
                getDriver().findElement(ElementRegistry.get(ElementKey.LOGOUT_CONFIRM)).click();
                System.out.println("✅ >>> Logout completed successfully.");
            } catch (Exception e) {
                System.out.println("⚠️ Logout attempt failed: " + e.getMessage());
            }
        }
    }

    private boolean isUserLoggedIn() {
        try {
            By userMenuLocator = ElementRegistry.get(ElementKey.USER_MENU);
            return getDriver().findElement(userMenuLocator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected void waitForScreenReady() {
        // Intentionally empty — subclasses override if they need a screen-ready check.
    }

    private boolean isSignUpTestClass() {
        return this.getClass().getName().contains("SignUp");
    }

    @AfterMethod
    public void tearDownAfterMethod(ITestResult result) {
        if (ITestResult.FAILURE == result.getStatus()) {
            takeScreenshot(result.getName());
        }
        System.out.println("🔄 >>> Test Method Finished.");
    }

    private void triggerDeepLink(String url, String appPackage) {
        try {
            getDriver().executeScript("mobile: deepLink", ImmutableMap.of(
                    "url", url,
                    "package", appPackage
            ));
        } catch (Exception e) {
            System.out.println("⚠️ Standard Deep Link failed, trying alternative ADB intent injection...");
            getDriver().executeScript("mobile: shell", ImmutableMap.of(
                    "command", "am start",
                    "args", "-a android.intent.action.VIEW -d " + url + " " + appPackage
            ));
        }
    }

    @AfterClass
    public void tearDown() {
        if (getDriver() != null) {
            try {
                getDriver().quit();
                System.out.println(">>> Driver session quit successfully. <<<");
            } catch (Exception e) {
                System.out.println("⚠️ Driver session was already closed: " + e.getMessage());
            }
        }
        driverThreadLocal.remove();
        System.out.println(">>> Test Class Finished execution. Thread resources cleared. <<<");
    }

    @AfterSuite
    public void stopAppiumServer() {
        if (server != null && server.isRunning()) {
            server.stop();
            System.out.println(">>> Appium Server stopped automatically <<<");
        }
    }

    public void takeScreenshot(String testName) {
        if (getDriver() == null) return;

        try {
            File srcFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String folderPath = System.getProperty("user.dir") + "/screenshots/";
            String filePath = folderPath + testName + "_" + timestamp + ".png";

            File screenshotFolder = new File(folderPath);
            if (!screenshotFolder.exists()) {
                screenshotFolder.mkdirs();
            }
            FileUtils.copyFile(srcFile, new File(filePath));
            System.out.println("❌ Test Failed! Screenshot captured at: " + filePath);
        } catch (Exception e) {
            System.out.println("⚠️ Exception occurred while capturing screenshot.");
        }
    }
}