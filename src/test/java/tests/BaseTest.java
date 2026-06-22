package tests;

import com.google.common.collect.ImmutableMap;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;
import org.testng.annotations.*;
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
    public void setUp(@Optional("realdevice") String targetEnv, @Optional("8201") String systemPort) throws MalformedURLException {
        ConfigReader.loadConfig(targetEnv + ".properties");

        boolean isSignUpTestClass = this.getClass().getName().contains("SignUp");

        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName(ConfigReader.getProperty("platform.name"))
                .setAutomationName(ConfigReader.getProperty("automation.name"))
                .setDeviceName(ConfigReader.getProperty("device.name"))
                .setAppPackage(ConfigReader.getProperty("app.package"))
                .setAppActivity(ConfigReader.getProperty("app.activity"))
                .setNoReset(!isSignUpTestClass)
                .setFullReset(false);

        options.setSystemPort(Integer.parseInt(systemPort));
        options.setCapability("appium:resetKeyboard", true);
        options.setCapability("appium:ensureWebviewsHavePages", true);
        options.setCapability("appium:includeWindows", true);
        options.setCapability("appium:settings[allowWindowOcclusion]", true);
        // Remove this or set it to false in your driver setup options:
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
    public void launchAppCleanly(java.lang.reflect.Method method) {
        if (getDriver() != null) {
            String appPackage = ConfigReader.getProperty("app.package");
            boolean isSignUpTestMethod = method.getName().toLowerCase().contains("signup");

            try {
                System.out.println("🔄 >>> Cycling application state...");
                getDriver().terminateApp(appPackage);
                getDriver().activateApp(appPackage);

                if (isSignUpTestMethod) {
                    System.out.println("🔗 >>> Aligning target view via Sign-Up Deep Link routing...");
                    triggerDeepLink("rainbow://signup", appPackage);
                } else {
                    System.out.println("🔗 >>> Aligning target view via Home Deep Link routing...");
                    triggerDeepLink("rainbow://openrainbow.net", appPackage);
                }

                System.out.println("✅ >>> App is active and ready for test execution.");
            } catch (Exception e) {
                System.out.println("⚠️ Fallback: Forcing basic app activation due to: " + e.getMessage());
                getDriver().activateApp(appPackage);
            }
        }
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