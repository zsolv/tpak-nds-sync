package ch.zsolv.GUI.screens;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import ch.zsolv.Tpak;
import ch.zsolv.GUI.GUI;
import ch.zsolv.GUI.GUIPanel;

/**
 * Overlay to ask for T-pak credentials
 */
public class TpakCredentials extends GUIPanel {

    @Override
    protected void draw() {      
        
        // Define content 
        JLabel header = new JLabel("T-pak Login");
        JLabel usernameLabel = new JLabel("");
        JLabel passwordLabel = new JLabel("");
        JTextField usernameField = new JTextField("");
        JPasswordField passwordField = new JPasswordField("");
        JButton okButton = new JButton("Login");
        okButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Check if credentials are valid
                if (Tpak.checkCredentials(usernameField.getText(), String.valueOf(passwordField.getPassword()))) {
                    usernameField.setEnabled(false);
                    usernameField.setEditable(false);
                    passwordField.setEnabled(false);
                    passwordField.setEditable(false);
                    if (! Tpak.login(usernameField.getText(), String.valueOf(passwordField.getPassword())) ){
                        JOptionPane.showMessageDialog(me, "T-pak login failed with given credentials!", "Error: Login Failed!", JOptionPane.ERROR_MESSAGE);
                    }else {
                        GUI.getInstance().closeOverlay();
                    }
                } else {
                    JOptionPane.showMessageDialog(me, "T-pak login failed with given credentials!", "Error: Login Failed!", JOptionPane.ERROR_MESSAGE);
                }
            }
            
        });

        // Format content objects
        header.setFont(header.getFont().deriveFont(Font.BOLD, 30));
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setVerticalAlignment(SwingConstants.CENTER);
        header.setBounds(25, 25, 430, 50);
        header.setVisible(true);
        usernameLabel.setText("Username:");
        usernameLabel.setBounds(25, 100, 100, 25);
        usernameLabel.setVisible(true);
        passwordLabel.setText("Password:");
        passwordLabel.setBounds(25, 150, 100, 25);
        passwordLabel.setVisible(true);
        usernameField.setBounds(150, 100, 280, 25);
        usernameField.setVisible(true);
        passwordField.setBounds(150, 150, 280, 25);
        passwordField.setVisible(true);
        okButton.setBounds(25, 250, 430, 50);

        // Add content objects
        this.add(header);
        this.add(usernameLabel);
        this.add(passwordLabel);
        this.add(usernameField);
        this.add(passwordField);
        this.add(okButton);

    }
    protected boolean isOverlay() {
        return true;
    }
}
