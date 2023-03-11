package ch.zsolv.NDS;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SeleniumUtils {

    /**
     * Try to get a webelement when it is loaded by a css selector
     * @param selector
     * @return
     * @throws Exception
     */
    public static WebElement waitAndGetElement(WebDriver driver, By selector) throws Exception{
        Wait<WebDriver> fWait = new FluentWait<>(driver)
        .withTimeout(Duration.ofSeconds(10))
        .pollingEvery(Duration.ofMillis(500))
        .ignoring(NoSuchElementException.class);
        return fWait.until(new Function<WebDriver,WebElement>() {
            public WebElement apply(WebDriver driver){
                return driver.findElement(selector);
            }
        });
    }
    
    /**
     * Try to get a list of webelements when they are loaded by a css selector
     * @param selector
     * @return
     * @throws Exception
     */
    public static List<WebElement> waitAndGetElements(WebDriver driver, By selector) throws Exception{
        Wait<WebDriver> fWait = new FluentWait<>(driver)
        .withTimeout(Duration.ofSeconds(10))
        .pollingEvery(Duration.ofMillis(500))
        .ignoring(NoSuchElementException.class);
        fWait.until(new Function<WebDriver,WebElement>() {
            public WebElement apply(WebDriver driver){
                return driver.findElement(selector);
            }
        });
        return driver.findElements(selector);
    }

    /**
     * Scrolls to an element before clicking it
     */
    public static void elementScrollAndClick(WebDriver driver, WebElement element) throws Exception{
        Scroller scroll = new Scroller(driver);
        boolean clicked = false;
        while(!clicked){
            try{
                element.click();
                clicked = true;
            }catch (ElementClickInterceptedException e) {
                scroll.next();
                new WebDriverWait(driver, Duration.ofMillis(100));
            }
        }
    }

    public static boolean checkIfExists(WebDriver driver, By selector) throws Exception{
        try{
            driver.findElement(selector);
        }catch(NoSuchElementException e){
            return false;
        }
        return true;
    }
}
