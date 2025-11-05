package com.example.travelbooking.ui;

import com.example.travelbooking.data.CityRepository;
import com.example.travelbooking.model.City;
import com.example.travelbooking.model.Hostel;
import com.example.travelbooking.model.Route;
import com.example.travelbooking.util.ImageUtil;
import com.example.travelbooking.dao.BusDao;
import com.example.travelbooking.dao.HotelDao;
import com.example.travelbooking.service.BookingService;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.*;

public class HomePanel extends JPanel {
    private final JTextField searchField = new JTextField(24);
    private final JButton showAllBtn = new JButton("Show All");
    private final JTable routesTable = new JTable();
    private final JTable hostelsTable = new JTable();
    private final JPanel routeView = new JPanel();
    private final JPanel galleryGrid = new JPanel(new GridLayout(0, 3, 8, 8));
    private final JList<String> recommendedList = new JList<>();

    private final BusDao busDao = new BusDao();
    private final HotelDao hotelDao = new HotelDao();
    private final BookingService bookingService = new BookingService();

    private final DefaultTableModel routesModel = new DefaultTableModel(new Object[][]{}, new String[]{"Route", "Distance (km)", "Time (hrs)"}){
        public boolean isCellEditable(int r,int c){return false;}
    };
    private final DefaultTableModel hostelsModel = new DefaultTableModel(new Object[][]{}, new String[]{"City","Hostel","Price/Night","Rooms"}){
        public boolean isCellEditable(int r,int c){return false;}
    };

    public HomePanel() {
        setLayout(new BorderLayout(8,8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel searchLbl = new JLabel("Search City or Route:");
        top.add(searchLbl);
        top.add(searchField);
        Theme.styleButton(showAllBtn, Theme.ACCENT_PURPLE);
        top.add(showAllBtn);
        add(top, BorderLayout.NORTH);

        routesTable.setModel(routesModel);
        hostelsTable.setModel(hostelsModel);
        Theme.styleTable(routesTable, Theme.ACCENT_BLUE);
        Theme.styleTable(hostelsTable, Theme.ACCENT_PINK);

        JPanel left = new JPanel(new BorderLayout(6,6));
        JSplitPane vSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(routesTable), new JScrollPane(hostelsTable));
        vSplit.setResizeWeight(0.5);
        left.add(vSplit, BorderLayout.CENTER);
        left.add(new JLabel("Recommended Trips"), BorderLayout.NORTH);
        Theme.styleList(recommendedList, Theme.ACCENT_GREEN);
        left.add(new JScrollPane(recommendedList), BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout(6,6));
        routeView.setLayout(new BoxLayout(routeView, BoxLayout.Y_AXIS));
        right.add(new JScrollPane(routeView), BorderLayout.NORTH);
        right.add(new JScrollPane(galleryGrid), BorderLayout.CENTER);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        mainSplit.setResizeWeight(0.45);
        add(mainSplit, BorderLayout.CENTER);

        routesTable.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseClicked(java.awt.event.MouseEvent e){
                if (e.getClickCount()==2) openBusBookingForSelectedRoute();
            }
        });
        hostelsTable.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseClicked(java.awt.event.MouseEvent e){
                if (e.getClickCount()==2) openHotelBookingForSelectedCity();
            }
        });

        showAllBtn.addActionListener(e -> { searchField.setText(""); loadAll(); });
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){ filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ filter(); }
        });

        loadAll();
    }

    private void loadAll() {
        loadRoutesFromDb(null);
        loadHostelsFromDbAll();
        loadGallery(CityRepository.popularCities(30));
        loadRecommended(CityRepository.recommendedTrips(5));
        routeView.removeAll(); routeView.revalidate(); routeView.repaint();
    }

    private void filter() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) { loadAll(); return; }
        loadRoutesFromDb(q);
        loadHostelsFromDbByCity(q);
        java.util.List<City> cities = CityRepository.searchCities(q);
        loadGallery(cities.isEmpty() ? CityRepository.popularCities(30) : cities);
    }

    private void loadRoutesFromDb(String query) {
        routesModel.setRowCount(0);
        java.util.List<String[]> rows = busDao.listDistinctRoutes(query);
        for (String[] r : rows) {
            String route = r[0] + " → " + r[1];
            double minFare = 0.0; try { minFare = Double.parseDouble(r[2]); } catch (Exception ignored) {}
            double km = minFare > 0 ? (minFare / 1.8) : 0; // rough estimate
            double hrs = km > 0 ? (km / 50.0) : 0;
            routesModel.addRow(new Object[]{route, Math.round(km), String.format(java.util.Locale.US, "%.1f", hrs)});
        }
        if (routesModel.getRowCount() > 0) routesTable.setRowSelectionInterval(0,0);
    }

    private void loadHostelsFromDbAll() {
        hostelsModel.setRowCount(0);
        try {
            for (var h : hotelDao.listAll()) {
                hostelsModel.addRow(new Object[]{h.city, h.name, h.pricePerNight, h.roomsAvailable});
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadHostelsFromDbByCity(String cityLike) {
        hostelsModel.setRowCount(0);
        try {
            for (var h : hotelDao.findByCity(cityLike)) {
                hostelsModel.addRow(new Object[]{h.city, h.name, h.pricePerNight, h.roomsAvailable});
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadGallery(Collection<City> cities) {
        galleryGrid.removeAll();
        for (City c : cities) {
            JPanel card = new JPanel(new BorderLayout());
            JLabel img = new JLabel("Loading...", SwingConstants.CENTER);
            img.setPreferredSize(new Dimension(220, 140));
            card.add(img, BorderLayout.CENTER);
            card.add(new JLabel(c.getName(), SwingConstants.CENTER), BorderLayout.SOUTH);
            galleryGrid.add(card);
            SwingUtilities.invokeLater(() -> {
                ImageIcon ic = ImageUtil.loadFirstFromCityFolder(c.getName(), 220, 140);
                if (ic != null) { img.setText(null); img.setIcon(ic); }
                else setImage(img, c.getImagePath(), 220, 140);
            });
        }
        galleryGrid.revalidate(); galleryGrid.repaint();
    }

    private void loadRecommended(List<Route> rts) {
        DefaultListModel<String> m = new DefaultListModel<>();
        for (Route r : rts) {
            m.addElement(r.getCitiesOnRoute().stream().map(City::getName).reduce((a,b)->a+" → "+b).orElse(""));
        }
        recommendedList.setModel(m);
    }

    private void openBusBookingForSelectedRoute() {
        int row = routesTable.getSelectedRow();
        if (row < 0) return;
        String routeStr = routesModel.getValueAt(row, 0).toString();
        String[] parts = routeStr.split(" → ");
        if (parts.length < 2) return;
        String src = parts[0];
        String dst = parts[parts.length-1];
        openBusDialog(src, dst);
    }

    private void renderRoute(Route r) {
        routeView.removeAll();
        routeView.add(new JLabel("Cities on route:"));
        for (City c : r.getCitiesOnRoute()) {
            JPanel row = new JPanel(new BorderLayout(6,6));
            JLabel img = new JLabel("Loading...", SwingConstants.CENTER);
            img.setPreferredSize(new Dimension(280, 160));
            row.add(img, BorderLayout.WEST);
            JTextArea ta = new JTextArea(c.getName()+"\n"+c.getDescription());
            ta.setLineWrap(true); ta.setWrapStyleWord(true); ta.setEditable(false);
            row.add(new JScrollPane(ta), BorderLayout.CENTER);
            routeView.add(row);
            ImageIcon ic = ImageUtil.loadFirstFromCityFolder(c.getName(), 280, 160);
            if (ic != null) { img.setText(null); img.setIcon(ic); }
            else setImage(img, c.getImagePath(), 280, 160);
        }
        routeView.add(new JLabel(String.format("Total: %.0f km, ~%.1f hrs", r.getTotalDistance(), r.getEstimatedTime())));
        routeView.revalidate(); routeView.repaint();
    }

    private void setImage(JLabel label, String pathOrUrl, int w, int h) {
        try {
            BufferedImage img = null;
            if (pathOrUrl != null && pathOrUrl.startsWith("http")) {
                img = ImageIO.read(new URL(pathOrUrl));
            } else if (pathOrUrl != null) {
                java.net.URL res = HomePanel.class.getClassLoader().getResource(pathOrUrl);
                if (res != null) img = ImageIO.read(res);
                else img = ImageIO.read(new File(pathOrUrl));
            }
            if (img != null) {
                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                label.setText(null);
                label.setIcon(new ImageIcon(scaled));
            } else {
                label.setText("[no image]");
            }
        } catch (IOException e) { label.setText("[image error]"); }
    }

    private void openHotelBookingForSelectedCity() {
        int row = hostelsTable.getSelectedRow();
        if (row < 0) return;
        String city = hostelsModel.getValueAt(row, 0).toString();
        openHotelDialog(city);
    }

    private void appendRecent(String q) {
        try {
            Path p = Path.of("recent_searches.txt");
            Files.writeString(p, q+System.lineSeparator(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }

    private void openBusDialog(String src, String dst) {
        java.util.List<com.example.travelbooking.model.Bus> buses = busDao.findByRoute(src, dst);
        if (buses.isEmpty()) { JOptionPane.showMessageDialog(this, "No buses for "+src+" → "+dst); return; }
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Book Bus: "+src+" → "+dst, true);
        JPanel p = new JPanel(new GridBagLayout()); GridBagConstraints gc = new GridBagConstraints(); gc.insets = new Insets(4,4,4,4); gc.anchor = GridBagConstraints.WEST;
        gc.gridx=0; gc.gridy=0; p.add(new JLabel("Bus:"), gc);
        JComboBox<String> itemCb = new JComboBox<>();
        for (var b : buses) itemCb.addItem("#"+b.id+" - "+b.name+" ("+b.departureTime+") Fare: "+b.fare);
        gc.gridx=1; p.add(itemCb, gc);
        gc.gridx=0; gc.gridy++; p.add(new JLabel("Passengers:"), gc);
        JSpinner countSp = new JSpinner(new SpinnerNumberModel(1,1,99,1));
        gc.gridx=1; p.add(countSp, gc);
        gc.gridx=0; gc.gridy++; p.add(new JLabel("Date:"), gc);
        JSpinner dateSp = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH)); dateSp.setEditor(new JSpinner.DateEditor(dateSp, "yyyy-MM-dd"));
        gc.gridx=1; p.add(dateSp, gc);
        gc.gridx=0; gc.gridy++; p.add(new JLabel("Unit Fare:"), gc);
        JLabel unitLbl = new JLabel("0.00"); gc.gridx=1; p.add(unitLbl, gc);
        gc.gridx=0; gc.gridy++; p.add(new JLabel("Total:"), gc);
        JLabel totalLbl = new JLabel("0.00"); gc.gridx=1; p.add(totalLbl, gc);
        gc.gridx=0; gc.gridy++; p.add(new JLabel("Customer:"), gc);
        JTextField nameTf = new JTextField(18); gc.gridx=1; p.add(nameTf, gc);
        JButton createBtn = new JButton("Book"); JButton closeBtn = new JButton("Close");
        Theme.styleButton(createBtn, Theme.ACCENT_GREEN);
        Theme.styleButton(closeBtn, Theme.ACCENT_BLUE);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btns.add(createBtn); btns.add(closeBtn);
        JPanel wrap = new JPanel(new BorderLayout()); wrap.add(p, BorderLayout.CENTER); wrap.add(btns, BorderLayout.SOUTH); dlg.setContentPane(wrap);
        Runnable update = () -> {
            String sel = (String) itemCb.getSelectedItem(); if (sel==null){unitLbl.setText("0.00"); totalLbl.setText("0.00"); return;}
            int id = Integer.parseInt(sel.substring(1, sel.indexOf(' ')));
            try { double unit = busDao.getFareById(id); unitLbl.setText(String.format("%.2f", unit)); totalLbl.setText(String.format("%.2f", unit * (Integer)countSp.getValue())); } catch (Exception ignored) {}
        };
        itemCb.addActionListener(e -> update.run());
        countSp.addChangeListener(e -> update.run());
        update.run();
        createBtn.addActionListener(e -> {
            try {
                String sel = (String) itemCb.getSelectedItem(); if (sel==null) return; int id = Integer.parseInt(sel.substring(1, sel.indexOf(' ')));
                int persons = (Integer) countSp.getValue(); LocalDate date = ((Date)dateSp.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); String cust=nameTf.getText().trim(); if(cust.isEmpty()){JOptionPane.showMessageDialog(dlg,"Enter customer name");return;}
                double unit = Double.parseDouble(unitLbl.getText()); var b = bookingService.bookBus(id, cust, persons, date, unit); JOptionPane.showMessageDialog(dlg, "Booked Ref: "+b.refCode);
                dlg.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Failed: "+ex.getMessage()); }
        });
        closeBtn.addActionListener(e -> dlg.dispose()); dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }

    private void openHotelDialog(String city) {
        java.util.List<com.example.travelbooking.model.Hotel> hotels = new java.util.ArrayList<>();
        try { hotels = hotelDao.findByCity(city); } catch (Exception ignored) {}
        if (hotels.isEmpty()) { JOptionPane.showMessageDialog(this, "No hotels in "+city); return; }
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Book Hotel: "+city, true);
        JPanel p = new JPanel(new GridBagLayout()); GridBagConstraints gc = new GridBagConstraints(); gc.insets = new Insets(4,4,4,4); gc.anchor = GridBagConstraints.WEST;
        gc.gridx=0; gc.gridy=0; p.add(new JLabel("Hotel:"), gc);
        JComboBox<String> itemCb = new JComboBox<>();
        for (var h : hotels) itemCb.addItem("#"+h.id+" - "+h.name+" Price: "+h.pricePerNight);
        gc.gridx=1; p.add(itemCb, gc);
        gc.gridx=0; gc.gridy++; p.add(new JLabel("Rooms:"), gc);
        JSpinner countSp = new JSpinner(new SpinnerNumberModel(1,1,99,1)); gc.gridx=1; p.add(countSp, gc);
        gc.gridx=0; gc.gridy++; p.add(new JLabel("Date:"), gc);
        JSpinner dateSp = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH)); dateSp.setEditor(new JSpinner.DateEditor(dateSp, "yyyy-MM-dd")); gc.gridx=1; p.add(dateSp, gc);
        gc.gridx=0; gc.gridy++; p.add(new JLabel("Price/Night:"), gc); JLabel unitLbl = new JLabel("0.00"); gc.gridx=1; p.add(unitLbl, gc);
        gc.gridx=0; gc.gridy++; p.add(new JLabel("Total:"), gc); JLabel totalLbl = new JLabel("0.00"); gc.gridx=1; p.add(totalLbl, gc);
        gc.gridx=0; gc.gridy++; p.add(new JLabel("Customer:"), gc); JTextField nameTf = new JTextField(18); gc.gridx=1; p.add(nameTf, gc);
        JButton createBtn = new JButton("Book"); JButton closeBtn = new JButton("Close");
        Theme.styleButton(createBtn, Theme.ACCENT_PINK);
        Theme.styleButton(closeBtn, Theme.ACCENT_BLUE);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btns.add(createBtn); btns.add(closeBtn);
        JPanel wrap = new JPanel(new BorderLayout()); wrap.add(p, BorderLayout.CENTER); wrap.add(btns, BorderLayout.SOUTH); dlg.setContentPane(wrap);
        Runnable update = () -> { String sel=(String)itemCb.getSelectedItem(); if(sel==null){unitLbl.setText("0.00"); totalLbl.setText("0.00"); return;} int id=Integer.parseInt(sel.substring(1, sel.indexOf(' '))); try{ double unit=hotelDao.getPriceById(id); unitLbl.setText(String.format("%.2f", unit)); totalLbl.setText(String.format("%.2f", unit * (Integer)countSp.getValue())); }catch(Exception ignored){} };
        itemCb.addActionListener(e -> update.run()); countSp.addChangeListener(e -> update.run()); update.run();
        createBtn.addActionListener(e -> { try{ String sel=(String)itemCb.getSelectedItem(); if(sel==null)return; int id=Integer.parseInt(sel.substring(1, sel.indexOf(' '))); int rooms=(Integer)countSp.getValue(); LocalDate date=((Date)dateSp.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); String cust=nameTf.getText().trim(); if(cust.isEmpty()){JOptionPane.showMessageDialog(dlg,"Enter customer name");return;} double unit=Double.parseDouble(unitLbl.getText()); var b=bookingService.bookHotel(id, cust, rooms, date, unit); JOptionPane.showMessageDialog(dlg, "Booked Ref: "+b.refCode); dlg.dispose(); } catch(Exception ex){ JOptionPane.showMessageDialog(dlg, "Failed: "+ex.getMessage()); }});
        closeBtn.addActionListener(e -> dlg.dispose()); dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }
}
