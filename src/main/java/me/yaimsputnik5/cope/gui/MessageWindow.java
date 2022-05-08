package me.yaimsputnik5.cope.gui;

import javax.swing.*;

public class MessageWindow {
    public static void showMessage(String title,String body){
        JOptionPane.showMessageDialog(new JFrame(), body, title,
                JOptionPane.ERROR_MESSAGE);
    }
}
