package utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        ExtentManager.getInstance();
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = ExtentManager.getInstance().createTest(result.getMethod().getMethodName());
        ExtentManager.setTest(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentManager.getTest().log(Status.PASS, "Test Passed Successfully");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentManager.getTest().log(Status.FAIL, "Test Failed: " + result.getThrowable());

        // Dynamic addition: Link your existing screenshots automatically!
        String screenshotPath = System.getProperty("user.dir") + "/screenshots/" + result.getName() + ".png";
        if (new java.io.File(screenshotPath).exists()) {
            ExtentManager.getTest().addScreenCaptureFromPath(screenshotPath, "Failure Screenshot");
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentManager.getTest().log(Status.SKIP, "Test Skipped");
    }

    @Override
    public void onFinish(ITestContext context) {
        if (ExtentManager.getInstance() != null) {
            ExtentManager.getInstance().flush();
        }
    }
}