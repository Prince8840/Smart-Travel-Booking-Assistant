package com.example.travelbooking.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class Theme {
    // Fixed multi-color scheme (no switching)
    public static final Color ACCENT_PURPLE = new Color(0x6750A4);
    public static final Color ACCENT_BLUE   = new Color(0x2962FF);
    public static final Color ACCENT_GREEN  = new Color(0x2E7D32);
    public static final Color ACCENT_PINK   = new Color(0xD81B60);
    public static final Color BG = new Color(0xF6F6FA);

    public static void applyGlobalTheme() {
        UIManager.put("control", BG);
        UIManager.put("Panel.background", BG);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0,0,0,0));
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.foreground", new Color(0x222222));
        UIManager.put("Table.gridColor", new Color(0xE9E9EF));
        UIManager.put("Table.selectionForeground", Color.WHITE);
        // Keep default button colors globally; we'll style important buttons individually
        UIManager.put("List.selectionForeground", Color.WHITE);
    }

    public static void styleTabbedPane(JTabbedPane tabs){
        tabs.setBackground(BG);
        tabs.setBorder(new EmptyBorder(4,4,0,4));
    }

    public static void styleTable(JTable table){ styleTable(table, ACCENT_PURPLE); }
    public static void styleTable(JTable table, Color accent) {
        table.setRowHeight(24);
        table.setGridColor(new Color(0xE9E9EF));
        table.setSelectionBackground(accent);
        table.setSelectionForeground(Color.WHITE);
        JTableHeader h = table.getTableHeader();
        h.setBackground(new Color(0xEFEFFF));
        h.setForeground(new Color(0x333366));
        h.setFont(h.getFont().deriveFont(Font.BOLD));
        DefaultTableCellRenderer zebra = new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col){
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFAFAFF));
                return c;
            }
        };
        for (int i=0;i<table.getColumnCount();i++) table.getColumnModel().getColumn(i).setCellRenderer(zebra);
    }

    public static void styleButton(AbstractButton b){ styleButton(b, ACCENT_PURPLE); }
    public static void styleButton(AbstractButton b, Color accent){
        b.setBackground(accent);
        b.setForeground(Color.WHITE);
        b.setFocusable(false);
        if (b instanceof JButton jb){
            jb.setBorderPainted(false);
            jb.setOpaque(true);
        }
    }

    public static void styleList(JList<?> list){ styleList(list, ACCENT_PURPLE); }
    public static void styleList(JList<?> list, Color accent){
        list.setSelectionBackground(accent);
        list.setSelectionForeground(Color.WHITE);
        list.setBackground(Color.WHITE);
    }
}
