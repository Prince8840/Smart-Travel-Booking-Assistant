package com.example.travelbooking.ui;

import com.example.travelbooking.model.Booking;
import com.example.travelbooking.service.BookingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BookingsPanel extends JPanel {
    private final BookingService bookingService = new BookingService();
    private final JTable table = new JTable();
    private final JButton refreshBtn = new JButton("Refresh");
    private final JButton cancelBtn = new JButton("Cancel by ID");

    public BookingsPanel() {
        setLayout(new BorderLayout(8,8));
        table.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"ID","Ref","Type","Item ID","Customer","Persons","Date","Total","Status"}){
            public boolean isCellEditable(int r, int c){return false;}
        });
        Theme.styleTable(table, Theme.ACCENT_PURPLE);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        Theme.styleButton(refreshBtn, Theme.ACCENT_BLUE);
        Theme.styleButton(cancelBtn, Theme.ACCENT_PINK);
        bottom.add(refreshBtn);
        bottom.add(cancelBtn);
        add(bottom, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> load());
        cancelBtn.addActionListener(e -> cancel());

        load();
    }

    private void load() {
        List<Booking> list = bookingService.listBookings();
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        m.setRowCount(0);
        for (Booking b : list) {
            m.addRow(new Object[]{b.id, b.refCode, b.type, b.itemId, b.customerName, b.persons, b.date, b.totalAmount, b.status});
        }
    }

    private void cancel() {
        String idStr = JOptionPane.showInputDialog(this, "Enter Booking ID to cancel:");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            boolean ok = bookingService.cancelBooking(id);
            JOptionPane.showMessageDialog(this, ok ? "Cancelled" : "Unable to cancel (maybe already cancelled or not found)");
            load();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid ID");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
