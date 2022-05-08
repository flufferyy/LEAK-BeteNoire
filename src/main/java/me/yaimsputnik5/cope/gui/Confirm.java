package me.yaimsputnik5.cope.gui;

import javax.swing.*;

public class Confirm {
    public static boolean requestConfirm(String text){
        return JOptionPane.showConfirmDialog(null, text) == 0;
    }
}
