package com.example.travelbooking;

import com.example.travelbooking.db.Database;
import com.example.travelbooking.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            com.example.travelbooking.ui.Theme.applyGlobalTheme();
            Database.init();
            new MainFrame().setVisible(true);
        });
    }
}
