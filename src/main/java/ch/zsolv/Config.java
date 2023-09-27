package ch.zsolv;

/**
 * Provide an object for configs
 */
public class Config {

    // If coaches should be touched at all
    private static volatile boolean touchCoaches = true;
    

    public static void setTouchCoaches(boolean b){
        Config.touchCoaches = b;
    }

    public static boolean getTouchCoaches(){
        return Config.touchCoaches;
    }


    // If entered athletes should be touched at all
    private static volatile boolean touchEnteredAthletes = true;
    

    public static void setTouchEnteredAthletes(boolean b){
        Config.touchEnteredAthletes = b;
    }

    public static boolean getTouchEnteredAthletes(){
        return Config.touchEnteredAthletes;
    }
}
