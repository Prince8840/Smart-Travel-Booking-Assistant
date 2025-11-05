package com.example.travelbooking.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        super("Smart Travel Booking Assistant");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setLocationRelativeTo(null);

        // Apply a fixed multi-color UI (no theme menu)
        Theme.applyGlobalTheme();

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.addTab("Home", new HomePanel());
        tabs.addTab("Bookings", new BookingsPanel());
        tabs.addTab("Gallery", new GalleryPanel());
        tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        Theme.styleTabbedPane(tabs);
        setContentPane(tabs);
    }
}
