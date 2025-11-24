package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class TrackTraceForManufacturersPage {

    private WebDriver driver;
    private WebDriverWait wait;

    private By hierarchyBrowser = By.id("x");

    public TrackTraceForManufacturersPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void clickOnHierarchyBrowser(){
        wait.until(ExpectedConditions.elementToBeClickable(hierarchyBrowser)).click();
    }
}
