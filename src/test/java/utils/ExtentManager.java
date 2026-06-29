package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import java.io.File;

public class ExtentManager {
    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testThreadLocal = new ThreadLocal<>();

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            File rootProjectDir = new File(".");
            String absoluteRootPath = rootProjectDir.getAbsolutePath();

            if (absoluteRootPath.endsWith(".")) {
                absoluteRootPath = absoluteRootPath.substring(0, absoluteRootPath.length() - 1);
            }

            String reportFolderPath = absoluteRootPath + "reports";
            String reportFilePath = reportFolderPath + File.separator + "ExtentReport.html";

            System.out.println("🤖 Hard Diagnosis - Creating directory at: " + reportFolderPath);
            File reportDir = new File(reportFolderPath);
            if (!reportDir.exists()) {
                boolean folderCreated = reportDir.mkdirs();
                System.out.println("🤖 Hard Diagnosis - Was folder created successfully? " + folderCreated);
            }

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportFilePath);
            sparkReporter.config().setTheme(Theme.DARK);
            sparkReporter.config().setDocumentTitle("Appium Automation Report");
            sparkReporter.config().setReportName("Mobile Test Execution Results");

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            extent.setSystemInfo("OS", "Cross-Platform");
            extent.setSystemInfo("Framework", "Page Object Model");
        }
        return extent;
    }

    public static ExtentTest getTest() {
        return testThreadLocal.get();
    }

    public static void setTest(ExtentTest test) {
        testThreadLocal.set(test);
    }

    public static void clearTest() {
        testThreadLocal.remove();
    }
}