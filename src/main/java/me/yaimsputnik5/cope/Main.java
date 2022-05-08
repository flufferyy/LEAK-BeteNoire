package me.yaimsputnik5.cope;
import com.formdev.flatlaf.FlatDarculaLaf;
import me.yaimsputnik5.cope.gui.MainWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        if(args.length == 0){
            FlatDarculaLaf.install();
            JFrame.setDefaultLookAndFeelDecorated(true);
            Info.gui = true;
            new MainWindow();
        }
        else {
            Info.gui = false;
            CLI.init(args);
        }
    }
}