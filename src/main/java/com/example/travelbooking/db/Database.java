package com.example.travelbooking.db;

import com.example.travelbooking.data.CityRepository;
import com.example.travelbooking.model.City;
import com.example.travelbooking.model.Hostel;
import com.example.travelbooking.model.Route;

import java.sql.*;
import javax.swing.JOptionPane;

public class Database {
    private static final String URL = "jdbc:h2:./travel_booking_db"; // file-based DB in project folder
    private static final String USER = "sa";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void init() {
        try (Connection conn = getConnection()) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS CITY (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(100) UNIQUE NOT NULL" +
                        ")");
                st.execute("CREATE TABLE IF NOT EXISTS BUS (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(120) NOT NULL, " +
                        "source_city_id INT NOT NULL, " +
                        "dest_city_id INT NOT NULL, " +
                        "fare DECIMAL(10,2) NOT NULL, " +
                        "departure_time VARCHAR(20), " +
                        "seats_total INT NOT NULL, " +
                        "seats_available INT NOT NULL, " +
                        "FOREIGN KEY (source_city_id) REFERENCES CITY(id), " +
                        "FOREIGN KEY (dest_city_id) REFERENCES CITY(id)" +
                        ")");
                st.execute("CREATE TABLE IF NOT EXISTS HOTEL (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(120) NOT NULL, " +
                        "city_id INT NOT NULL, " +
                        "rating DECIMAL(3,1) NOT NULL, " +
                        "rooms_total INT NOT NULL, " +
                        "rooms_available INT NOT NULL, " +
                        "price_per_night DECIMAL(10,2) NOT NULL, " +
                        "FOREIGN KEY (city_id) REFERENCES CITY(id)" +
                        ")");
                st.execute("CREATE TABLE IF NOT EXISTS BOOKING (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "ref_code VARCHAR(20) UNIQUE, " +
                        "type VARCHAR(10) NOT NULL, /* BUS or HOTEL */ " +
                        "item_id INT NOT NULL, " +
                        "customer_name VARCHAR(120) NOT NULL, " +
                        "persons INT NOT NULL, " +
                        "date DATE NOT NULL, " +
                        "total_amount DECIMAL(12,2) NOT NULL, " +
                        "status VARCHAR(15) NOT NULL /* ACTIVE|CANCELLED */ " +
                        ")");
            }
            seed(conn);
            importFromRepository(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to initialize database: " + e.getMessage());
        }
    }

    private static void seed(Connection conn) throws SQLException {
        // Seed a few base cities if DB empty
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM CITY")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO CITY(name) VALUES (?), (?), (?)")) {
                    ps.setString(1, "Delhi");
                    ps.setString(2, "Jaipur");
                    ps.setString(3, "Mumbai");
                    ps.executeUpdate();
                }
            }
        }
        // Seed a few buses if empty
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM BUS")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                int delhi = ensureCity(conn, "Delhi");
                int jaipur = ensureCity(conn, "Jaipur");
                int mumbai = ensureCity(conn, "Mumbai");
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO BUS(name, source_city_id, dest_city_id, fare, departure_time, seats_total, seats_available) " +
                                "VALUES (?,?,?,?,?,?,?)")) {
                    // Delhi -> Jaipur
                    ps.setString(1, "Raj Express"); ps.setInt(2, delhi); ps.setInt(3, jaipur);
                    ps.setBigDecimal(4, new java.math.BigDecimal("500.00")); ps.setString(5, "08:00");
                    ps.setInt(6, 40); ps.setInt(7, 40); ps.addBatch();

                    // Jaipur -> Delhi
                    ps.setString(1, "Pink City Travels"); ps.setInt(2, jaipur); ps.setInt(3, delhi);
                    ps.setBigDecimal(4, new java.math.BigDecimal("520.00")); ps.setString(5, "17:30");
                    ps.setInt(6, 40); ps.setInt(7, 40); ps.addBatch();

                    // Mumbai -> Jaipur
                    ps.setString(1, "Western Line"); ps.setInt(2, mumbai); ps.setInt(3, jaipur);
                    ps.setBigDecimal(4, new java.math.BigDecimal("1200.00")); ps.setString(5, "22:15");
                    ps.setInt(6, 50); ps.setInt(7, 50); ps.addBatch();

                    ps.executeBatch();
                }
            }
        }
        // Seed a few hotels if empty
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM HOTEL")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                int delhi = ensureCity(conn, "Delhi");
                int jaipur = ensureCity(conn, "Jaipur");
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO HOTEL(name, city_id, rating, rooms_total, rooms_available, price_per_night) " +
                                "VALUES (?,?,?,?,?,?)")) {
                    ps.setString(1, "Hotel Delhi Heights"); ps.setInt(2, delhi);
                    ps.setBigDecimal(3, new java.math.BigDecimal("4.2")); ps.setInt(4, 60); ps.setInt(5, 60);
                    ps.setBigDecimal(6, new java.math.BigDecimal("1800.00")); ps.addBatch();

                    ps.setString(1, "Jaipur Palace Inn"); ps.setInt(2, jaipur);
                    ps.setBigDecimal(3, new java.math.BigDecimal("4.5")); ps.setInt(4, 40); ps.setInt(5, 40);
                    ps.setBigDecimal(6, new java.math.BigDecimal("2500.00")); ps.addBatch();

                    ps.setString(1, "Hawa Mahal Suites"); ps.setInt(2, jaipur);
                    ps.setBigDecimal(3, new java.math.BigDecimal("4.0")); ps.setInt(4, 30); ps.setInt(5, 30);
                    ps.setBigDecimal(6, new java.math.BigDecimal("2200.00")); ps.addBatch();

                    ps.executeBatch();
                }
            }
        }
    }

    private static void importFromRepository(Connection conn) throws SQLException {
        // Import all cities
        for (City c : CityRepository.getAllCities()) {
            int cityId = ensureCity(conn, c.getName());
            // Import hostels as hotels if not present
            for (Hostel h : c.getHostels()) {
                if (!hotelExists(conn, h.getName())) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO HOTEL(name, city_id, rating, rooms_total, rooms_available, price_per_night) VALUES (?,?,?,?,?,?)")) {
                        ps.setString(1, h.getName());
                        ps.setInt(2, cityId);
                        ps.setBigDecimal(3, new java.math.BigDecimal("4.0")); // default rating
                        ps.setInt(4, 40);
                        ps.setInt(5, Math.max(10, h.getAvailableRooms()));
                        ps.setBigDecimal(6, new java.math.BigDecimal(h.getPricePerNight()));
                        ps.executeUpdate();
                    }
                }
            }
        }
        // Import simple buses between connected cities if not present
        for (City c : CityRepository.getAllCities()) {
            int srcId = ensureCity(conn, c.getName());
            for (String dest : c.getConnectedCities()) {
                int dstId = ensureCity(conn, dest);
                if (!busExistsByPair(conn, srcId, dstId)) {
                    String name = c.getName() + " Express to " + dest;
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO BUS(name, source_city_id, dest_city_id, fare, departure_time, seats_total, seats_available) VALUES (?,?,?,?,?,?,?)")) {
                        double fare = 300 + (Math.abs(c.getName().hashCode() + dest.hashCode()) % 1200);
                        ps.setString(1, name);
                        ps.setInt(2, srcId);
                        ps.setInt(3, dstId);
                        ps.setBigDecimal(4, new java.math.BigDecimal(String.format(java.util.Locale.US, "%.2f", fare)));
                        ps.setString(5, String.format("%02d:%02d", (srcId + dstId) % 24, (srcId * 7 + dstId) % 60));
                        ps.setInt(6, 40);
                        ps.setInt(7, 40);
                        ps.executeUpdate();
                    }
                }
            }
        }
        // Ensure buses for each recommended route (start -> end)
        for (Route r : CityRepository.getAllRoutes()) {
            java.util.List<City> cs = r.getCitiesOnRoute();
            if (cs == null || cs.size() < 2) continue;
            String srcName = cs.get(0).getName();
            String dstName = cs.get(cs.size() - 1).getName();
            int srcId = ensureCity(conn, srcName);
            int dstId = ensureCity(conn, dstName);
            if (!busExistsByPair(conn, srcId, dstId)) {
                String name = "Route Express: " + srcName + " → " + dstName;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO BUS(name, source_city_id, dest_city_id, fare, departure_time, seats_total, seats_available) VALUES (?,?,?,?,?,?,?)")) {
                    double fare = Math.max(200.0, r.getTotalDistance() * 1.8);
                    ps.setString(1, name);
                    ps.setInt(2, srcId);
                    ps.setInt(3, dstId);
                    ps.setBigDecimal(4, new java.math.BigDecimal(String.format(java.util.Locale.US, "%.2f", fare)));
                    ps.setString(5, "08:00");
                    ps.setInt(6, 40);
                    ps.setInt(7, 40);
                    ps.executeUpdate();
                }
            }
        }
    }

    private static boolean hotelExists(Connection conn, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM HOTEL WHERE LOWER(name)=LOWER(?)")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    private static boolean busExists(Connection conn, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM BUS WHERE LOWER(name)=LOWER(?)")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
    private static boolean busExistsByPair(Connection conn, int srcId, int dstId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM BUS WHERE source_city_id=? AND dest_city_id=?")) {
            ps.setInt(1, srcId);
            ps.setInt(2, dstId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    private static int ensureCity(Connection conn, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM CITY WHERE LOWER(name)=LOWER(?)")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        try (PreparedStatement ins = conn.prepareStatement("INSERT INTO CITY(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ins.setString(1, name);
            ins.executeUpdate();
            try (ResultSet keys = ins.getGeneratedKeys()) { if (keys.next()) return keys.getInt(1); }
        }
        return getCityId(conn, name);
    }

    private static int getCityId(Connection conn, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM CITY WHERE LOWER(name)=LOWER(?)")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("City not found: " + name);
    }
}
