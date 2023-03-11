package ch.zsolv;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.awt.Desktop;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import org.json.JSONObject;


/**
 * Ensure, a client always runs the newest available version!
 * 
 * The version is just the timestamp of compilation in hex format, so local compile and run is always newest
 */
public class UpdateCheck {

    // What URL contains info on the latest version
    private static final String LATEST_INFO_URL = "https://zsolv.ch/tpak-nds-sync/version.json";

    // Logger
    private static Logger logger = Logger.getLogger(UpdateCheck.class.getName());


    /**
     * Extracts the version from the jar
     * 
     * If version file can not be read: current timestamp is returned
     * 
     * @return a long with the timestamp from version file
     */
    private static long getMyVersion(){


        // Try reading input the version file
        try (InputStream in = new UpdateCheck().getClass().getResourceAsStream("/version.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            // Parsing daate time from file content
            LocalDateTime dateTime = LocalDateTime.parse(reader.readLine(), DateTimeFormatter.ISO_DATE_TIME);

            // Log version
            logger.info("Version: "+Long.toHexString(dateTime.toEpochSecond(ZoneOffset.UTC)));

            // return timestamp
            return dateTime.toEpochSecond(ZoneOffset.UTC);

        } catch (IOException e) {

            // Log that we could not read from version file from jar
            logger.warning("Could not read version from jar");
        }

        // Return current timestamp if we reach here
        return LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    }

    /**
     * Returns the latest version timestamp according to predefined URL
     * @return a long with the timestamp from the defined URL
     * 
     * If something failes, returns 0
     */
    private static long getLatestVersion(){
        try{
            return Long.parseLong(getLatestInfo().getString("version"),16);       
        }catch(IOException e){}
        return 0;
    }

    /**
     * Returns the link to download the latest URL
     * @return a string with the URL
     * 
     * If something fails, return an empty string ""
     */
    private static String getLatestDownloadLink(){
        try{
            return getLatestInfo().getString("link");
        }catch(IOException e){}
        return "";
    }

    /**
     * Downloads the information from the latest URL and parse it into a JSONObject
     * @return a JSONObject
     * @throws IOException if anything gos wrong
     */
    private static JSONObject getLatestInfo() throws IOException{
        URL url = new URL(LATEST_INFO_URL);
        try (InputStream input = url.openStream()) {
            InputStreamReader isr = new InputStreamReader(input, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder json = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                json.append((char) c);
            }

            return new JSONObject(json.toString());
        }catch(Exception e){
            logger.severe("Failed to download information on the latest version");
            return new JSONObject();
        }
    }

    /**
     * Check if this version is the latest, else it asks the user download it and stops the execution
     */
    static void ensureLatestVersion(){

        // Get version timestamps
        long version = getMyVersion();
        long latest = getLatestVersion();

        // If newer version is available
        if (latest > version) {

            // Get Download Link for latest version
            String dl = getLatestDownloadLink();
            try{
                new URI(dl);
            }catch(Exception e){
                dl = "";
            }

            // Prepare text area with information
            JTextArea ta = new JTextArea();
            ta.setText("There is a newer version available!"+(dl.equals("")?"":"\nDownload here: "+dl));
            ta.setCaretPosition(0);
            ta.setEditable(false);
            ta.setBackground(null);
            ta.setBorder(null);


            // Create Ok button that exits this program
            JButton okay = new JButton("Ok");
            okay.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    System.exit(0);
                    
                }
                
            });

            // Create Download button
            JButton download = new JButton("Download");
            download.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent arg0) {

                        // If supported by the system, open the browser to download the latest version
                        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                            try {
                                desktop.browse(new URI(getLatestDownloadLink()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    
                    
                }
                
            });

            // Create Dialog
            JOptionPane.showOptionDialog(
                null, 
                ta, 
                "Update available", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE, 
                null, 
                dl.equals("")?new Object[]{okay}:new Object[]{okay, download}, 
                okay);

            // If the Dialog ends, stop execution
            System.exit(0);
        }
    }
}
