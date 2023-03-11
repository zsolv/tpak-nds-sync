package ch.zsolv;

import java.io.PrintStream;

import ch.zsolv.GUI.GUI;
import ch.zsolv.GUI.screens.NDSControls;
import ch.zsolv.GUI.screens.TpakCredentials;

/**
 * Main Class, started
 */
public class App {

    public static void main( String[] args ) {

        PrintStream interceptor = new LogIntercepter(System.out);
        System.setOut(interceptor);

        // Ensure latest version is running
        UpdateCheck.ensureLatestVersion();

        // Start Gui App
        GUI gui = GUI.getInstance();

        // Start NDS Controls
        gui.changeTo(new NDSControls());

        // Ask for tpack credentials
        gui.changeTo(new TpakCredentials());

    }

}
