package ch.zsolv;

import java.io.OutputStream;
import java.io.PrintStream;

import ch.zsolv.GUI.GUI;
import ch.zsolv.GUI.GUIPanel;
import ch.zsolv.GUI.screens.NDSControls;

public class LogIntercepter extends PrintStream {
    public LogIntercepter(OutputStream out) {
        super(out, true);
    }
    @Override
    public void print(String s){
        super.print(s);
        GUIPanel ap = GUI.getInstance().getActivePanel();
        if(ap instanceof NDSControls){
            ((NDSControls) ap).log(s);
        }
    }
}