package ch.zsolv.GUI.screens;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import ch.zsolv.Config;
import ch.zsolv.StateTransmitter;
import ch.zsolv.Tpak;
import ch.zsolv.GUI.GUIPanel;
import ch.zsolv.NDS.Nds;
import ch.zsolv.NDS.SeleniumUtils;

public class NDSControls extends GUIPanel {


    private volatile JTextArea logArea;

    @Override
    protected void draw() {

        // Set header
        JLabel header = new JLabel("T-pak - NDS - Sync");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 30));
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setVerticalAlignment(SwingConstants.CENTER);
        header.setBounds(25, 25, 430, 50);
        header.setVisible(true);
        this.add(header);

        // Set log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBounds(25 ,300,430,320);
        this.add(logArea);

        // Add bug
        JButton debugButton = new JButton();
        try {
            BufferedImage icon = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = icon.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);   
            g2.drawImage(ImageIO.read(this.getClass().getClassLoader().getResource("bug.png")), 0, 0, 20, 20, null);
            g2.dispose();
            debugButton.setOpaque(true);
            debugButton.setContentAreaFilled(false);
            debugButton.setBorderPainted(false);
            debugButton.setIcon(new ImageIcon(icon));
        } catch (IOException e) {
            debugButton.setText("");
        }
       
        debugButton.setBounds(450, 10, 20, 20);
        debugButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showSaveDialog(debugButton) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    SeleniumUtils.debug(Nds.getInstance().getDriver(), file);
                }
            }
        });
        this.add(debugButton);

        // Set coach controls
        ButtonGroup coachConfigGroup = new ButtonGroup();
        JRadioButton coachAllWhenEmpty = new JRadioButton();
        JRadioButton coachNothing = new JRadioButton();
        JLabel coachConfigLabel = new JLabel();
        coachConfigLabel.setText("What to do with coaches?");
        coachAllWhenEmpty.setText("Set all, when none set, else leave existing");
        coachNothing.setText("Do nothing");
        coachAllWhenEmpty.setSelected(true);
        coachConfigGroup.add(coachAllWhenEmpty);
        coachConfigGroup.add(coachNothing);
        coachConfigLabel.setBounds(25,100,400,20);
        coachAllWhenEmpty.setBounds(40,125,400,20);
        coachNothing.setBounds(40,150,400,20);

        coachAllWhenEmpty.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent arg0) {
               if (arg0.getStateChange() == 1){
                Config.setTouchCoaches(true);
               }
                
            }
            
        });
        coachNothing.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent arg0) {
               if (arg0.getStateChange() == 1){
                Config.setTouchCoaches(false);
               }
                
            }
            
        });

        this.add(coachConfigLabel);
        this.add(coachAllWhenEmpty);
        this.add(coachNothing);

        // Set touch athlete controls
        ButtonGroup athleteTouchConfigGroup = new ButtonGroup();
        JRadioButton athleteTouchYes = new JRadioButton();
        JRadioButton athleteTouchNo = new JRadioButton();
        JLabel athleteTouchConfigLabel = new JLabel();
        athleteTouchConfigLabel.setText("What to do with already set athletes?");
        athleteTouchYes.setText("Add & Remove all attendance according to tpak");
        athleteTouchNo.setText("Just add attendance according to tpak");
        athleteTouchYes.setSelected(true);
        athleteTouchConfigGroup.add(athleteTouchYes);
        athleteTouchConfigGroup.add(athleteTouchNo);
        athleteTouchConfigLabel.setBounds(25,175,400,20);
        athleteTouchYes.setBounds(40,200,400,20);
        athleteTouchNo.setBounds(40,225,400,20);

        athleteTouchYes.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent arg0) {
               if (arg0.getStateChange() == 1){
                Config.setTouchEnteredAthletes(true);
               }
                
            }
            
        });
        athleteTouchNo.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent arg0) {
               if (arg0.getStateChange() == 1){
                Config.setTouchEnteredAthletes(false);
               }
                
            }
            
        });

        this.add(athleteTouchConfigLabel);
        this.add(athleteTouchYes);
        this.add(athleteTouchNo);

        // Check access
        JButton checkAccess = new JButton();
        checkAccess.setText("Athletes not given you access");
        checkAccess.setBounds(25,250,430,25);
        checkAccess.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {          
                if (Tpak.getInstance() == null){
                    JOptionPane.showMessageDialog(
                        me, 
                        "Failed to access t-pak, no credentials available!",
                        "T-pak not yet logged in", 
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        me, 
                        Tpak.getInstance().athletesWithoutPermission().stream()
                            .map(x -> { return x.firstname+" "+x.lastname;})
                            .map(Object::toString).collect(Collectors.joining("\n")),
                        "Athletes not given you access", 
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }

        });
        this.add(checkAccess);

        // Add NDS control buttons
        JButton ndsStartControlButton = new JButton();
        JButton ndsStopControlButton = new JButton();
        StateTransmitter runSync = new StateTransmitter();
        runSync.b = false;
         
        ndsStartControlButton.setText("Start Sync");
        ndsStartControlButton.setBounds(25,275, 430, 25);
        ndsStopControlButton.setText("Stop Sync");
        ndsStopControlButton.setBounds(25,275, 430, 25);
        ndsStartControlButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                runSync.b = true;
                new Thread(){
                    public void run(){
                        try {
                            if(Nds.getInstance() == null){ 
                                JOptionPane.showMessageDialog(me, "Log into the NDS, navigate to the day you want to start the sync and press 'Start Sync' again!", "Login on NDS", JOptionPane.INFORMATION_MESSAGE);
                                return;
                            }
                            if(!Nds.getInstance().isPastLogin()){
                                JOptionPane.showMessageDialog(me, "Error: Not logged in on NDS yet!", "Invalid page", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            Date currentDateOnSite = Nds.getInstance().getCurrentDate();
                            if (currentDateOnSite == null ){
                                JOptionPane.showMessageDialog(me, "Error (null): Could not get the date to enter attendance!", "Invalid page", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            ndsStartControlButton.setVisible(false);
                            ndsStopControlButton.setVisible(true);
                            me.remove(ndsStartControlButton);
                            me.add(ndsStopControlButton);
                            me.repaint();
                            
                            while(runSync.b){
                                
                                if( new Date().compareTo(currentDateOnSite) <= 0){
                                    ndsStartControlButton.setVisible(true);
                                    ndsStopControlButton.setVisible(false);
                                    me.add(ndsStartControlButton);
                                    me.remove(ndsStopControlButton);
                                    me.repaint();
                                    break;
                                }
                                try{
                                    Nds.getInstance().UpdateEntriesOfTheDay(currentDateOnSite, Tpak.getInstance());
                                }catch (Exception e1){
                                    JOptionPane.showMessageDialog(me, "Error while syncing day: "+new SimpleDateFormat("d.M.yyyy").format(currentDateOnSite)+"\n"+e1.getMessage(), "Sync error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                try{
                                    Nds.getInstance().goToNext();
                                }catch (Exception e1){
                                    JOptionPane.showMessageDialog(me, "Error going to next day", "Sync error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                try{
                                    currentDateOnSite = Nds.getInstance().getCurrentDate();
                                    if (currentDateOnSite == null ){
                                        JOptionPane.showMessageDialog(me, "Error (null): Could not get the date to enter attendance!", "Invalid page", JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                }catch(Exception e){
                                    JOptionPane.showMessageDialog(me, "Error (exc): Could not get the date to enter attendance!", "Invalid page", JOptionPane.ERROR_MESSAGE);
                                }
        
                            }
                            JOptionPane.showMessageDialog(me, "Sync stopped!", "Sync stopped", JOptionPane.INFORMATION_MESSAGE);
        
                            
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(me, "Error (exc): Could not get the date to enter attendance!", "Invalid page", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                  }.start();
                
            }
            
        });
        ndsStopControlButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                runSync.b = false;
                ndsStartControlButton.setVisible(true);
                ndsStopControlButton.setVisible(false);
                me.remove(ndsStopControlButton);
                me.add(ndsStartControlButton);
                me.repaint();
                    
            }
            
        });
        this.add(ndsStartControlButton);
        
    }

    public void log(String text){
        if(logArea == null){
            return;
        }
        //Get Old Log
        StringBuilder s = new StringBuilder(logArea.getText());

        String[] spl = s.toString().split("\n");
        s = new StringBuilder();
        if(spl.length<20){
            s = new StringBuilder(spl[0] + "\n");
        }
        for(int i = 1;i<spl.length;++i){
            s.append(spl[i]).append("\n");
        }

        //Set new text with time in front of old Log
        logArea.setText(s+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())+" >>> "+text);
    }
    
}
