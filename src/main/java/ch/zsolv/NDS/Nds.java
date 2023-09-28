package ch.zsolv.NDS;


import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import ch.zsolv.Athlete;
import ch.zsolv.Config;
import ch.zsolv.Tpak;

/**
 * Selenium based interface for NDS
 */
public class Nds {
        
    // Selenium Driver
    private WebDriver driver;

    // Singleton instance
    private static Nds instance = null;
    private Nds(){
        // init chrom driver
        driver = new ChromeDriver();
        // go to nds
        driver.get("https://nds.baspo.admin.ch");
    }
    public static Nds getInstance(){
        if(instance == null){
            instance = new Nds();
            return null;
        }
        return instance;
    }
    public WebDriver getDriver(){
        return driver;
    }

    /**
     * To close chrome on destroy
     */
    public static void destroy(){
        if (instance == null){
            return;
        }
        try{
            instance.driver.quit();
        }catch(NullPointerException e){}
    }

    /**
     * Checks if user has done login
     * @return
     */
    public boolean isPastLogin(){
        return driver.getCurrentUrl().contains("www.nds.baspo.admin.ch");
    }

    /**
     * Try to get current date
     * @return
     * @throws Exception
     */
    public Date getCurrentDate() throws Exception{
        WebElement day = SeleniumUtils.waitAndGetElement(driver, By.cssSelector("li.nds-ui-prev-next-nav-current"));
        String currentDateValue = day.getText().split("\n")[1].split(" ")[1];  
        return new SimpleDateFormat("d.M.yyyy").parse(currentDateValue);
    }

    /**
     * Try to go to the next date
     * @throws Exception
     */
    public void goToNext() throws Exception {

        if (SeleniumUtils.checkIfExists(driver, By.cssSelector("#mat-dialog-0"))){
            System.out.println("Dialog opened, try clicking away");
            SeleniumUtils.elementScrollAndClick(driver, SeleniumUtils.waitAndGetElement(driver, By.cssSelector("#mat-dialog-0 > nds-ui-dialog-confirm > div.mat-dialog-actions.ng-star-inserted > button.mat-focus-indicator.mat-button.mat-raised-button.mat-button-base.mat-primary")));
            System.out.println("Clicked Dialog away");
        }      
        Date currentDate = getCurrentDate();

        WebElement next = SeleniumUtils.waitAndGetElement(driver, By.cssSelector("li.nds-ui-prev-next-nav-next.ng-star-inserted > a"));
        SeleniumUtils.elementScrollAndClick(driver, next);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) { }

        while (currentDate.equals(getCurrentDate())){
            System.out.println("Failed to change Date, stuck with "+currentDate.toString()+"!");
            SeleniumUtils.elementScrollAndClick(driver, SeleniumUtils.waitAndGetElement(driver, By.cssSelector("li.nds-ui-prev-next-nav-next.ng-star-inserted > a")));
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) { }
        }
    }

    /**
     * Update one day into the NDS
     * 
     * By design not adding some wait time, to be more human and avoid errors with browser interactions when data not loaded fast enough
     * @param date
     * @param tpak
     * @throws Exception
     */
    public void UpdateEntriesOfTheDay(Date date, Tpak tpak) throws Exception{

        // Log that we will update this date
        System.out.println("Day: "+new SimpleDateFormat("d.M.yyyy").format(date)+" started");

        // Wait a second to give time for everything to be loaded
        Thread.sleep(1000);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                                                                                    //
        // Coaches Part                                                                                       //
        //                                                                                                    //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////

        try{
            // If coaches should be touched at all
            if (Config.getTouchCoaches()){

                // Get list of coaches
                List<WebElement> coachesEl = SeleniumUtils.waitAndGetElements(driver, By.cssSelector("#content > nds-normale-ui-attendance-detail > div > nds-normale-ui-activity-attendance-check > div > div:nth-child(2) > nds-ui-table > div > div.nds-ui-table > table > tbody > tr"));

                boolean coachesHave = coachesEl.stream().map(coach -> 
                Arrays.asList(coach.findElement(
                    By.className("mat-checkbox")).getAttribute("class").split(" ")
                ).contains("mat-checkbox-checked")).anyMatch(x -> Boolean.TRUE.equals(x));           

                // Only if no coaches has a checked checkbox, check all of them
                if (! coachesHave
                    ){
                        WebElement allCoaches = SeleniumUtils.waitAndGetElement(driver, By.cssSelector("#content > nds-normale-ui-attendance-detail > div > nds-normale-ui-activity-attendance-check > div > div:nth-child(2) > nds-ui-table > div > div.nds-ui-table > table > thead > tr"));
                        SeleniumUtils.elementScrollAndClick(driver, allCoaches.findElement(By.className("mat-checkbox")));
                    } else {
                        // Else log that we do nothing
                        System.out.println("Day: "+new SimpleDateFormat("d.M.yyyy").format(date)+" already coach entry, do nothing!");
                    }
            }
        }catch(Exception e){
            throw new Exception("Error while syncing coaches! --> "+e.getMessage()+"\n");
        }
        System.out.println("Day: "+new SimpleDateFormat("d.M.yyyy").format(date)+" coaches done");


        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                                                                                    //
        // Athletes Part                                                                                      //
        //                                                                                                    //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Get List of athlete entries
        List<WebElement> athletesNDSList;
        try{
            athletesNDSList = SeleniumUtils.waitAndGetElements(driver, By.cssSelector("#content > nds-normale-ui-attendance-detail > div > nds-normale-ui-activity-attendance-check > div > div:nth-child(3) > nds-ui-table > div > div.nds-ui-table > table > tbody > tr"));
        }catch(Exception e){
            throw new Exception("Error while getting athlete list! --> "+e.getMessage()+"\n");
        }
        System.out.println("Day: "+new SimpleDateFormat("d.M.yyyy").format(date)+" Athlete reading done");

        // Iterate over athlete elements
        for (WebElement listEntry : athletesNDSList) {

            // Get Data of athlete
            WebElement checkBox;
            boolean checked;
            String lastname = "", firstname = "";
            try{
                lastname = listEntry.findElement(By.className("mat-column-person.lastName")).getText();
                firstname = listEntry.findElement(By.className("mat-column-person-firstName")).getText();
                checkBox = listEntry.findElement(By.className("mat-checkbox"));
                checked = Arrays.asList(checkBox.getAttribute("class").split(" ")).contains("mat-checkbox-checked");
            }catch(Exception e){
                throw new Exception("Error while reading athlete data ("+firstname+" "+lastname+")! --> "+e.getMessage()+"\n");
            }

            try{
                List<Athlete> athletes = Tpak.getInstance().getAllAthletes();
                // Match athletes and bring to sync if athlete match
                for(int j = 0; j<athletes.size(); ++j){
                    if (athletes.get(j).firstname.equals(firstname) && athletes.get(j).lastname.equals(lastname)){

                        // Get if athlete has trained at this day
                        boolean trained = tpak.athleteHasRegioTrainingAtDate(athletes.get(j), date);

                        if (trained) {
                            System.out.println(athletes.get(j).firstname+" "+athletes.get(j).lastname +" has trained on "+new SimpleDateFormat("d.M.yyyy").format(date));
                        }
                        if (trained != checked && (trained || Config.getTouchEnteredAthletes())){
                            SeleniumUtils.elementScrollAndClick(driver, checkBox);
                        }
                    }
                }
            }catch(Exception e){
                throw new Exception("Error while writing athlete data ("+firstname+" "+lastname+")! --> "+e.getMessage()+"\n");
            }
        }
        System.out.println("Day: "+new SimpleDateFormat("d.M.yyyy").format(date)+" Athlete writing done");


        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                                                                                    //
        // Save the changes                                                                                   //
        //                                                                                                    //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        Thread.sleep(1000);
        try{
            WebElement save = SeleniumUtils.waitAndGetElement(driver, By.cssSelector("#content > nds-normale-ui-attendance-detail > nds-ui-footer > div > div > div > div:nth-child(2) > nds-ui-footer-actions > div > button"));
            SeleniumUtils.elementScrollAndClick(driver, save);
        }catch(Exception e){
            throw new Exception("Error while saving this day! --> "+e.getMessage()+"\n");
        }
        Thread.sleep(2000);
        System.out.println("Day: "+new SimpleDateFormat("d.M.yyyy").format(date)+" done");
        
    }
    
}
