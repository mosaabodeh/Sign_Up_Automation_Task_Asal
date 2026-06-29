package utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import java.io.File;

public class TestListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        System.out.println("🚀 [SUITE START] Context initialized for: " + context.getName());
        try {
            ExtentManager.getInstance();
        } catch (Exception e) {
            System.err.println("❌ Error initializing ExtentManager inside onStart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testDescription = result.getMethod().getDescription();
        String testName = (testDescription != null && !testDescription.isEmpty())
                ? testDescription
                : result.getMethod().getMethodName();

        System.out.println("⏳ [TEST START] Launching test execution log for: " + testName);

        try {
            ExtentTest test = ExtentManager.getInstance().createTest(testName);
            ExtentManager.setTest(test);
        } catch (Exception e) {
            System.err.println("❌ Error creating test node branch layer: " + e.getMessage());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("✅ [TEST PASSED] Log recorded for: " + result.getName());
        if (ExtentManager.getTest() != null) {
            ExtentManager.getTest().log(Status.PASS, "Test Passed Successfully");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("❌ [TEST FAILED] Collecting logs and attachments for: " + result.getName());
        if (ExtentManager.getTest() != null) {
            ExtentManager.getTest().log(Status.FAIL, "Test Failed: " + result.getThrowable());
        }

        try {
            File directory = new File(System.getProperty("user.dir") + File.separator + "screenshots");
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles((dir, name) -> name.startsWith(result.getName()) && name.endsWith(".png"));

                if (files != null && files.length > 0) {
                    java.util.Arrays.sort(files, org.apache.commons.io.comparator.LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
                    File latestScreenshot = files[files.length - 1];

                    if (ExtentManager.getTest() != null) {
                        ExtentManager.getTest().addScreenCaptureFromPath(latestScreenshot.getAbsolutePath(), "Failure Screenshot");
                        System.out.println("🔗 Attached latest screenshot file matching: " + latestScreenshot.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Failed to find or bind screenshot file to layout. " + e.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (ExtentManager.getTest() != null) {
            ExtentManager.getTest().log(Status.SKIP, "Test Skipped");
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("💾 [SUITE FINISH] Test processing completed for context: " + context.getName());

        // 🚀 THE CRITICAL SAFETY GUARD
        try {
            if (ExtentManager.getInstance() != null) {
                System.out.println("📝 Attempting to write Extent Report down to disk drive memory...");
                ExtentManager.getInstance().flush();
                System.out.println("📊 Extent Report flushed and updated successfully for: " + context.getName());
            } else {
                System.out.println("⚠️ Warning: Extent instance was unexpectedly null inside onFinish processing!");
            }
        } catch (Exception e) {
            System.err.println("❌ CRITICAL: Failed to flush report down onto storage filesystem! Error:");
            e.printStackTrace();
        }
    }
}