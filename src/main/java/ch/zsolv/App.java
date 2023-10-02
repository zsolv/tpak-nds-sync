package ch.zsolv;

import java.io.PrintStream;

import ch.zsolv.GUI.GUI;
import ch.zsolv.GUI.screens.NDSControls;
import ch.zsolv.GUI.screens.TpakCredentials;
import ch.zsolv.NDS.Nds;

/**
 * Main Class, started
 */
public class App {

    public static void main( String[] args ) {

        // For debugging print java version
        System.out.println("Java: "+System.getProperty("java.version"));

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

        // Load GUI
        Nds.getInstance();

    }

}
