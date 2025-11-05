package com.example.travelbooking.ui;

import com.example.travelbooking.data.CityRepository;
import com.example.travelbooking.model.City;
import com.example.travelbooking.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class GalleryPanel extends JPanel {
    private final JPanel grid = new JPanel(new GridLayout(0, 4, 8, 8));
    private final JComboBox<String> cityFilter = new JComboBox<>();

    public GalleryPanel() {
        setLayout(new BorderLayout(8,8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("City:"));
        top.add(cityFilter);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(grid), BorderLayout.CENTER);

        cityFilter.addItem("All");
        for (City c : CityRepository.getAllCities()) cityFilter.addItem(c.getName());
        cityFilter.addActionListener(e -> load());
        load();
    }

    private void load() {
        grid.removeAll();
        String selected = (String) cityFilter.getSelectedItem();
        Collection<City> cities = selected == null || selected.equals("All") ? CityRepository.getAllCities() : java.util.List.of(CityRepository.searchCities(selected).get(0));
        for (City c : cities) {
            final String cityName = c.getName();
            JPanel card = new JPanel(new BorderLayout());
            JLabel img = new JLabel("[no image]", SwingConstants.CENTER);
            img.setPreferredSize(new Dimension(180, 120));
            card.add(img, BorderLayout.CENTER);
            JLabel caption = new JLabel(cityName, SwingConstants.CENTER);
            card.add(caption, BorderLayout.SOUTH);
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new java.awt.event.MouseAdapter(){
                public void mouseClicked(java.awt.event.MouseEvent e){ showCityDialog(cityName); }
            });
            grid.add(card);
            ImageIcon icon = ImageUtil.loadFirstFromCityFolder(cityName, 180, 120);
            if (icon != null) { img.setText(null); img.setIcon(icon); }
        }
        grid.revalidate(); grid.repaint();
    }

    private void showCityDialog(String city) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), city + " Gallery", true);
        JPanel panel = new JPanel(new GridLayout(0, 3, 8, 8));
        java.util.List<ImageIcon> images = ImageUtil.loadAllFromCityFolder(city, 300, 180);
        if (images.isEmpty()) panel.add(new JLabel("No images in resources/cities/"+city));
        for (ImageIcon ic : images) panel.add(new JLabel(ic));
        dlg.getContentPane().add(new JScrollPane(panel));
        dlg.setSize(960, 600);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
}
