package ch.zsolv.NDS;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class Scroller {
    
    private final JavascriptExecutor js;

    public Scroller(WebDriver js){
        this.js = (JavascriptExecutor) js;
    }
    public void top(){
        scrollTo(0);
    }
    public void next(){
        double v = getScrollPosition();
        scrollTo(v+250);
        if(getScrollPosition() - v < 50){
            scrollTo(0);
        }
    }
    private void scrollTo(double value){
        js.executeScript("window.scroll(0,"+value+")");
    }
    private double getScrollPosition(){
        Object o = js.executeScript("return window.pageYOffset;");
        if (o instanceof Double){
            return (double) o;
        }
        return Double.parseDouble(o.toString());
    }


}
