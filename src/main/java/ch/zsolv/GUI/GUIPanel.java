package ch.zsolv.GUI;

import javax.swing.*;
import java.awt.*;

public abstract class GUIPanel extends JPanel{

    protected GUIPanel me;

    public GUIPanel(){
        me = this;
        initPanel();
        draw();
    }

    protected abstract void draw();

    protected void reDraw(){}
    String description(){
        return this.getClass().getName();
    }

    private void initPanel(){

        setLayout(null);
        setBounds(0,0, 480,640);
        setBackground(new Color(255, 255, 255));


    }
    protected void run(){}

    protected boolean isOverlay() {
        return false;
    }
}