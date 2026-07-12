package tests;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.ProfilePage;
import pages.SignUpPage;
import utils.EmailUtils;
import utils.JsonReader;

public class SignUpTest extends BaseTest {

    private static final String SIGNUP_DATA_FILE = "signUpData.json";

    private SignUpPage signUpPage;
    private ProfilePage profilePage;

    @BeforeClass
    public void initializePages() {
        this.signUpPage = new SignUpPage(getDriver());
        this.profilePage = new ProfilePage(getDriver());
    }

    @Override
    protected void waitForScreenReady() {
        signUpPage.ensureSignUpScreenReady();
    }

    @Test(priority = 1, groups = { "android" },
            description = "Complete the dynamic sign up registration funnel")
    public void testSignUpFlow() {

        String emailBase = JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "emailBase");
        String emailDomain = JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "emailDomain");
        String appPasswordForGetCodeFromEmail = JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "emailImapPassword");
        String passwordForSignUp = JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "password");
        String firstName = JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "firstName");
        String lastName = JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "lastName");
        String country = JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "country");

        String targetEmail = emailBase + System.currentTimeMillis() + emailDomain;
        signUpPage.submitEmailStage(targetEmail);

        System.out.println("📬 Fetching verification code sent to: " + targetEmail);
        String registrationCode = EmailUtils.getVerificationCode(targetEmail, appPasswordForGetCodeFromEmail);
        System.out.println("✅ Found Code: " + registrationCode);

        signUpPage.submitPasswordStage(passwordForSignUp, registrationCode);
        signUpPage.fillPersonalInfo(firstName, lastName, country);

        System.out.println(" The User With Data: " + firstName + "\t" + lastName + "\t" + "From : " + country + " Is Sign Up Successfully");
        signUpPage.clickTheTwoAllowButtons();

        Assert.assertTrue(profilePage.cancelUploadAvatarAndLogOutProcess(), "Failsafe: The Sign up process Flow Not executed Successfully We Cant Interact With Continue Button.");
    }

    @Test(priority = 2, groups = { "android" },
            description = "Complete the dynamic sign up registration ensure bad scenario")
    public void invalidSignUp() {
        String emailBase = JsonReader.getTestData(SIGNUP_DATA_FILE, "validSignUp", "emailBase");
        String emailDomain = JsonReader.getTestData(SIGNUP_DATA_FILE, "invalidEmailSignUp", "email");
        String targetEmail = emailBase + System.currentTimeMillis() + emailDomain;

        signUpPage.submitEmailStage(targetEmail);

        System.out.println("The Current Email is: " + targetEmail);
        String expectedErrorMessage = JsonReader.getTestData(SIGNUP_DATA_FILE, "invalidEmailSignUp", "expectedErrorMessage");
        boolean isVisible = signUpPage.verifyErrorMessageViaOcr(expectedErrorMessage);
        Assert.assertTrue(isVisible, "The validation error message '" + expectedErrorMessage + "' was not detected via OCR.");
    }

    @Test(priority = 4, groups = { "android" },
            description = "Verify that entering a weak password displays the appropriate complexity validation error message via OCR")
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
        signUpPage.clickOkButton();
        Assert.assertTrue(isErrorDisplayed, "Failsafe: The expected validation error message '" + expectedErrorMessage + "' was not found by OCR scanning.");
        profilePage.dismissOkAndSignInPrompt();
    }

    @Test(priority = 3, groups = { "android" },
            description = "Verify that attempting to submit with blank mandatory fields throws structural validation inline errors")
    public void testBlankMandatoryFieldsValidation() {

        String expectedErrorMessage = JsonReader.getTestData(SIGNUP_DATA_FILE, "EmptyFieldsSignUp", "expectedErrorMessage");
        signUpPage.clickSubmitWithoutInputs();

        boolean isErrorDisplayed = signUpPage.verifyErrorMessageViaOcr(expectedErrorMessage);
        Assert.assertTrue(isErrorDisplayed, "Failsafe: Missing field mandatory warning labels were not detected by OCR scanning.");
    }

    @Test(priority = 5, groups = { "android" },
            description = "Verify that signing up with an already registered email throws a duplicate registration toast exception")
    public void testDuplicateEmailRegistrationValidation() {
        String duplicateEmail = JsonReader.getTestData(SIGNUP_DATA_FILE, "duplicateEmailSignUp", "email");
        String emailImapPassword = JsonReader.getTestData(SIGNUP_DATA_FILE, "duplicateEmailSignUp", "emailImapPassword");

        signUpPage.clickNoButton();
        signUpPage.submitEmailStage(duplicateEmail);

        boolean existingAccountNotified = EmailUtils.isExistingAccountEmailReceived(duplicateEmail, emailImapPassword);

        Assert.assertTrue(existingAccountNotified,
                "Failsafe: Expected 'existing account' notification email was not received for: " + duplicateEmail);
    }
}