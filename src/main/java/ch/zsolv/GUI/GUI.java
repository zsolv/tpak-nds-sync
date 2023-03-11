package ch.zsolv.GUI;

import javax.imageio.ImageIO;
import javax.swing.*;

import ch.zsolv.NDS.Nds;

import java.awt.*;
import java.io.IOException;
import java.util.Stack;



/**
 * GUI object doing everything related to it
 */
public class GUI extends JFrame{

    private final String TITLE = "Tpak to NDS Sync";

    private static GUI instance;

    //Active GUI Panel
    private GUIPanel activePanel;
    private final Stack<GUIPanel> overlays = new Stack<>();
    

    private GUI(){

        // Set basic window settings such as title, decoration, size, close operation
        setTitle(TITLE);
        setUndecorated(false);
        setSize(480,640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //set window to middle of screen, no layout, not resizable
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);

        // Set background color and visibility
        setBackground(new Color(255, 255, 255));
        setVisible(true);

        // Add close event listener, to shutdown Selenium instance appropriately
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                try{
                    Nds.destroy();
                }catch(NullPointerException e1){}
                System.exit(0);
            }
        });


        // Try to define an icon for the window
        try {
            Image icon = ImageIO.read(this.getClass().getClassLoader().getResource("icon.png"));
            setIconImage(icon);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 
     * Getter for Singelton instance
     */
    public static GUI getInstance(){
        if (GUI.instance == null){
            GUI.instance = new GUI();
        }
        return instance;
    }

    /**
     * Method to change to a new panel
     * @param panel
     */
    public void changeTo(GUIPanel panel){
        if(panel == null){
            return;
        }
        if(panel.isOverlay())
            overlays.push(activePanel);

        swapToPanel(panel);
        panel.run();
    }
    /**
     * Method to close an overlay
     */
    public void closeOverlay(){
        if(! overlays.empty()){
            swapToPanel(overlays.pop());
            activePanel.run();
        }
    }

    /**
     * Returns the active panel
     * @return
     */
    public GUIPanel getActivePanel(){
        return activePanel;
    }

    /**
     * Change to a new GUI Panel
     * @param newPanel
     */
    private void swapToPanel(GUIPanel newPanel) {
        if (newPanel == null){
            return;
        }
        newPanel.reDraw();
        try {
            remove(activePanel);
        }catch (NullPointerException ignore){}
        add(newPanel);
        activePanel = newPanel;
        validate();
        repaint();
        setVisible(true);
    }

}
