package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class HomePage {

    private WebDriver driver;
    private WebDriverWait wait;

    // Locators
    private By selectCategoryMenu = By.id("gwt-debug-NavigationMenu");
    private By trackTraceForManufacturers = By.id("gwt-debug-mmTrackAndTraceForManufacturers");

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void navigateToTrackTraceForManufacturers() {
        wait.until(ExpectedConditions.elementToBeClickable(selectCategoryMenu)).click();
        wait.until(ExpectedConditions.elementToBeClickable(trackTraceForManufacturers)).click();
    }
}
