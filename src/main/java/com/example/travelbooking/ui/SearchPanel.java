package com.example.travelbooking.ui;

import com.example.travelbooking.model.Booking;
import com.example.travelbooking.model.Bus;
import com.example.travelbooking.model.Hotel;
import com.example.travelbooking.service.BookingService;
import com.example.travelbooking.service.SearchService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class SearchPanel extends JPanel {
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Bus", "Hotel"});
    private final JTextField cityField = new JTextField(18);
    private final JButton searchBtn = new JButton("Search Now");
    private final JTable table = new JTable();
    private final JButton bookBtn = new JButton("Book Selected");

    // New: photo/drive UI
    private final JComboBox<File> driveCombo = new JComboBox<>();
    private final JButton browseImageBtn = new JButton("Browse Image...");
    private final JLabel imageLabel = new JLabel("No image selected", SwingConstants.CENTER);
    private BufferedImage currentImage;

    private final SearchService searchService = new SearchService();
    private final BookingService bookingService = new BookingService();

    // caches for current table rows
    private java.util.List<Bus> lastBusResults;
    private java.util.List<Hotel> lastHotelResults;

    public SearchPanel() {
        setLayout(new BorderLayout(8,8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Type:")); top.add(typeCombo);
        top.add(new JLabel("City / Destination:")); top.add(cityField);
        top.add(searchBtn);
        // Windows roots (drives)
        top.add(new JLabel("Drive:"));
        top.add(driveCombo);
        add(top, BorderLayout.NORTH);

        // Left: results table
        JScrollPane tableScroll = new JScrollPane(table);

        // Right: image viewer and actions
        JPanel right = new JPanel(new BorderLayout(6,6));
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rightTop.add(browseImageBtn);
        right.add(rightTop, BorderLayout.NORTH);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(Color.WHITE);
        right.add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, right);
        split.setResizeWeight(0.65);
        add(split, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(bookBtn);
        add(bottom, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> doSearch());
        bookBtn.addActionListener(e -> doBook());
        typeCombo.addActionListener(e -> refreshColumns());
        browseImageBtn.addActionListener(e -> browseImage());
        imageLabel.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { redrawImage(); }
        });

        loadWindowsRoots();
        refreshColumns();
    }

    private void loadWindowsRoots() {
        driveCombo.removeAllItems();
        File[] roots = File.listRoots();
        if (roots != null) {
            for (File r : roots) driveCombo.addItem(r);
        }
        if (driveCombo.getItemCount() > 0) driveCombo.setSelectedIndex(0);
    }

    private void browseImage() {
        File initial = (File) driveCombo.getSelectedItem();
        JFileChooser fc = new JFileChooser(initial != null ? initial : new File("."));
        fc.setDialogTitle("Select an image");
        fc.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "gif", "bmp"));
        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                currentImage = ImageIO.read(f);
                imageLabel.setText(null);
                redrawImage();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load image: " + ex.getMessage());
            }
        }
    }

    private void redrawImage() {
        if (currentImage == null) return;
        int w = imageLabel.getWidth();
        int h = imageLabel.getHeight();
        if (w <= 0 || h <= 0) return;
        Image scaled = currentImage.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaled));
    }

    private void refreshColumns() {
        if (typeCombo.getSelectedItem().toString().equals("Bus")) {
            table.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"ID","Name","Route","Fare","Time","Seats Left"}){
                public boolean isCellEditable(int r, int c){return false;}
            });
        } else {
            table.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"ID","Name","City","Rating","Rooms Left","Price/Night"}){
                public boolean isCellEditable(int r, int c){return false;}
            });
        }
    }

    private void doSearch() {
        String city = cityField.getText().trim();
        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a city (e.g., Jaipur)");
            return;
        }
        if (typeCombo.getSelectedItem().toString().equals("Bus")) {
            List<Bus> buses = searchService.searchBusesByDestination(city);
            lastBusResults = buses;
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            m.setRowCount(0);
            for (Bus b : buses) {
                m.addRow(new Object[]{b.id, b.name, b.sourceCity + " → " + b.destCity, b.fare, b.departureTime, b.seatsAvailable});
            }
        } else {
            List<Hotel> hotels = searchService.searchHotelsByCity(city);
            lastHotelResults = hotels;
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            m.setRowCount(0);
            for (Hotel h : hotels) {
                m.addRow(new Object[]{h.id, h.name, h.city, h.rating, h.roomsAvailable, h.pricePerNight});
            }
        }
    }

    private void doBook() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a row to book");
            return;
        }
        String type = typeCombo.getSelectedItem().toString();
        try {
            String name = JOptionPane.showInputDialog(this, "Customer Name:");
            if (name == null || name.isBlank()) return;
            String personsStr = JOptionPane.showInputDialog(this, type.equals("Bus") ? "Number of seats:" : "Number of rooms:");
            if (personsStr == null) return;
            int persons = Integer.parseInt(personsStr);
            LocalDate date = LocalDate.now();

            Booking booking;
            if (type.equals("Bus")) {
                Bus b = lastBusResults.get(table.convertRowIndexToModel(row));
                booking = bookingService.bookBus(b.id, name, persons, date, b.fare);
            } else {
                Hotel h = lastHotelResults.get(table.convertRowIndexToModel(row));
                booking = bookingService.bookHotel(h.id, name, persons, date, h.pricePerNight);
            }
            JOptionPane.showMessageDialog(this, "Booked successfully! Ref: " + booking.refCode + " Total: " + booking.totalAmount);
            doSearch();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Booking failed: " + ex.getMessage());
        }
    }
}
