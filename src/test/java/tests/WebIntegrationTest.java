package tests;

import config.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.HomePage;
import pages.TrackTraceForManufacturersPage;
import utils.DriverManager;
import utils.TestContext;

public class WebIntegrationTest {

    private WebDriver driver;

    @BeforeClass
    public void setup() {
        driver = DriverManager.getDriver();
    }

    @Test
    public void testWebValidationFlow() {
        // --- STEP 2: Data Transfer from API test ---
        String ssccToTest = TestContext.SHARED_SSCC;
        System.out.println("Will be searching for SSCC: " + ssccToTest);


        // --- STEP 1: Authentication ---
        driver.get(ConfigReader.getProperty("base.url") +"/Portal/");

        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(
                ConfigReader.getProperty("app.email"),
                ConfigReader.getProperty("app.password")
        );

        // --- STEP 3: Data Transfer from API test ---
        HomePage homePage = new HomePage(driver);
        homePage.navigateToTrackTraceForManufacturers();

        TrackTraceForManufacturersPage trackTraceForManufacturersPage = new TrackTraceForManufacturersPage(driver);
        trackTraceForManufacturersPage.clickOnHierarchyBrowser();
        trackTraceForManufacturersPage.searchCode(ssccToTest);

        // --- STEP 4: Validation ---
        Assert.assertTrue(trackTraceForManufacturersPage.isCompliant(), "Pallet status is NOT Compliant!");
    }

    @AfterClass
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
