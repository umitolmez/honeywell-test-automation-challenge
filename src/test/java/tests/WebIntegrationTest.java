package tests;

import config.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
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
    public void testWebValidationFlow() throws InterruptedException {
        String ssccToTest = TestContext.SHARED_SSCC;

        System.out.println("Web Testing Starts. Searching for SSCC: " + ssccToTest);

        driver.get(ConfigReader.getProperty("base.url") +"/Portal/");

        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(
                ConfigReader.getProperty("app.email"),
                ConfigReader.getProperty("app.password")
        );

        HomePage homePage = new HomePage(driver);
        homePage.navigateToTrackTraceForManufacturers();

        TrackTraceForManufacturersPage trackTraceForManufacturersPage = new TrackTraceForManufacturersPage(driver);
        trackTraceForManufacturersPage.clickOnHierarchyBrowser();

//        boolean isCompliant = hierarchyPage.isCompliant();
//        Assert.assertTrue(isCompliant, "Pallet status is NOT Compliant!");
    }

    @AfterClass
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
