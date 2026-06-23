package tests;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.SignUpPage;
import utils.EmailUtils;
import utils.JsonReader;

public class SignUpTest extends BaseTest {
    private static final String SIGNUP_DATA_FILE = "signUpData.json";
    private SignUpPage signUpPage;

    @BeforeClass
    public void initializePages() {
        this.signUpPage = new SignUpPage(getDriver());
    }

    @Test(priority = 1, description = "Complete the dynamic sign up registration funnel")

    public void testSignUpFlow()  {
        String emailBase = JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "emailBase");
        String emailDomain = JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "emailDomain");
        String appPasswordForGetCodeFromEmail = JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "emailImapPassword");
        String passwordForSignUp= JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "password");
        String firstName= JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "firstName");
        String lastName= JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "lastName");
        String country = JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "country");
        String targetEmail = emailBase + System.currentTimeMillis() + emailDomain;

        signUpPage.submitEmailStage(targetEmail);

        System.out.println("📬 Fetching verification code sent to: " + targetEmail);
        String registrationCode = EmailUtils.getVerificationCode(targetEmail, appPasswordForGetCodeFromEmail);
        System.out.println("✅ Found Code: " + registrationCode);
        signUpPage.submitPasswordStage(passwordForSignUp,registrationCode);
        signUpPage.fillPersonalInfo(firstName,lastName,country);
        System.out.println(" The User With Data: " + firstName +"\t"+lastName+"\t"+"From : "+country+" Is Sign Up Successfully");

    }
    @Test(priority = 2, description = "Complete the dynamic sign up registration ensure bad scenario")
    public void invalidSignUp(){
        String emailBase = JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "emailBase");
        String emailDomain = JsonReader.getTestData(SIGNUP_DATA_FILE, "invalidEmailSignUp", "email");
        String targetEmail = emailBase + System.currentTimeMillis() + emailDomain;
        signUpPage.submitEmailStage(targetEmail);
        System.out.println("The Current Email is : "+targetEmail);
        String errorMessage = JsonReader.getTestData(SIGNUP_DATA_FILE, "invalidEmailSignUp", "expectedErrorMessage");

        signUpPage.toastMessage();
        boolean isVisible = signUpPage.verifyErrorMessageViaOcr(errorMessage);

        Assert.assertTrue(isVisible, "The validation error message was not displayed on the screen.");
       // Assert.assertTrue(signUpPage.isEmailFieldVisible());

    }
    @Test(priority = 3, description = "Verify that entering a weak password displays the appropriate complexity validation error message via OCR")
    public void testWeakPasswordSignUpValidation() {
        String emailBase = JsonReader.getTestData(SIGNUP_DATA_FILE, "weakPasswordSignUp", "emailBase");
        String emailDomain = JsonReader.getTestData(SIGNUP_DATA_FILE, "weakPasswordSignUp", "emailDomain");
        String passwordForSignUp = JsonReader.getTestData(SIGNUP_DATA_FILE, "weakPasswordSignUp", "password");
        String expectedErrorMessage = JsonReader.getTestData(SIGNUP_DATA_FILE, "weakPasswordSignUp", "expectedErrorMessage");

        String targetEmail = emailBase + System.currentTimeMillis() + emailDomain;

        signUpPage.submitEmailStage(targetEmail);

        System.out.println("📬 Fetching verification code sent to: " + targetEmail);
        String appPasswordForGetCodeFromEmail = JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "emailImapPassword");
        String registrationCode = EmailUtils.getVerificationCode(targetEmail, appPasswordForGetCodeFromEmail);
        System.out.println("✅ Found Code: " + registrationCode);

        signUpPage.submitPasswordStage(passwordForSignUp, registrationCode);

        System.out.println("🔍 Scanning screen via OCR for error: " + expectedErrorMessage);
        boolean isErrorDisplayed = signUpPage.verifyErrorMessageViaOcr(expectedErrorMessage);

        Assert.assertTrue(isErrorDisplayed,
                "Failsafe: The expected validation error message '" + expectedErrorMessage + "' was not found by OCR scanning.");
    }




}