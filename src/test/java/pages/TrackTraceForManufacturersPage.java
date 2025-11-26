package pages;

import config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class TrackTraceForManufacturersPage {

    private WebDriver driver;
    private WebDriverWait wait;

    private By enterTheCodeInput = By.xpath("//input[@placeholder='Enter the code...']");
    private By searchButton = By.xpath("//button[@data-testid='ws-search-button']");
    private By compliantStatus = By.xpath("//*[text()='Compliance Details']/following-sibling::div");
    private By compilanceDetailsLoading = By.xpath("//*[text()='Compliance Details']/following-sibling::div//*[text()='Loading...']");


    public TrackTraceForManufacturersPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(60));
    }

    public void clickOnHierarchyBrowser(){
        driver.get(ConfigReader.getProperty("base.url") +"/ui/tnt-manufacturer-frontend/hierarchies/general");
    }

    public void searchCode(String code){
        wait.until(ExpectedConditions.visibilityOfElementLocated(enterTheCodeInput)).sendKeys(code);
        wait.until(ExpectedConditions.elementToBeClickable(searchButton)).click();
    }

    public boolean isCompliant(){
        wait.until(ExpectedConditions.invisibilityOfElementLocated(compilanceDetailsLoading));
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(compliantStatus)).getText().contains("Compliance");
            //OR CHECK GREEN ICON
            //The expected result is not fully legible in the screenshot provided in the task.
        }catch (Exception e){
            return false;
        }

    }
}
