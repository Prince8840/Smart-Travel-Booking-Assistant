package com.example.travelbooking.dao;

import com.example.travelbooking.db.Database;
import com.example.travelbooking.model.Bus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BusDao {
    public List<Bus> findByDestination(String cityLike) {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT b.id,b.name,sc.name AS src, dc.name AS dst,b.fare,b.departure_time,b.seats_total,b.seats_available " +
                "FROM BUS b JOIN CITY sc ON b.source_city_id=sc.id JOIN CITY dc ON b.dest_city_id=dc.id " +
                "WHERE LOWER(dc.name) LIKE LOWER(?) ORDER BY b.fare ASC";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + cityLike + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Bus b = new Bus();
                    b.id = rs.getInt("id");
                    b.name = rs.getString("name");
                    b.sourceCity = rs.getString("src");
                    b.destCity = rs.getString("dst");
                    b.fare = rs.getDouble("fare");
                    b.departureTime = rs.getString("departure_time");
                    b.seatsTotal = rs.getInt("seats_total");
                    b.seatsAvailable = rs.getInt("seats_available");
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Bus> listAll() {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT b.id,b.name,sc.name AS src, dc.name AS dst,b.fare,b.departure_time,b.seats_total,b.seats_available " +
                "FROM BUS b JOIN CITY sc ON b.source_city_id=sc.id JOIN CITY dc ON b.dest_city_id=dc.id ORDER BY b.id ASC";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Bus b = new Bus();
                b.id = rs.getInt("id");
                b.name = rs.getString("name");
                b.sourceCity = rs.getString("src");
                b.destCity = rs.getString("dst");
                b.fare = rs.getDouble("fare");
                b.departureTime = rs.getString("departure_time");
                b.seatsTotal = rs.getInt("seats_total");
                b.seatsAvailable = rs.getInt("seats_available");
                list.add(b);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Double getFareById(int id) throws SQLException {
        String sql = "SELECT fare FROM BUS WHERE id=?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return null;
    }

    public boolean reduceSeats(int busId, int count) throws SQLException {
        String sql = "UPDATE BUS SET seats_available = seats_available - ? WHERE id=? AND seats_available>=?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, count);
            ps.setInt(2, busId);
            ps.setInt(3, count);
            return ps.executeUpdate() > 0;
        }
    }

    public void restoreSeats(int busId, int count) throws SQLException {
        String sql = "UPDATE BUS SET seats_available = seats_available + ? WHERE id=?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, count);
            ps.setInt(2, busId);
            ps.executeUpdate();
        }
    }

    public String getNameById(int id) throws SQLException {
        String sql = "SELECT name FROM BUS WHERE id=?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return "Bus " + id;
    }

    public List<Bus> findByRoute(String sourceCity, String destCity) {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT b.id,b.name,sc.name AS src, dc.name AS dst,b.fare,b.departure_time,b.seats_total,b.seats_available " +
                "FROM BUS b JOIN CITY sc ON b.source_city_id=sc.id JOIN CITY dc ON b.dest_city_id=dc.id " +
                "WHERE LOWER(sc.name)=LOWER(?) AND LOWER(dc.name)=LOWER(?) ORDER BY b.fare ASC";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sourceCity);
            ps.setString(2, destCity);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Bus b = new Bus();
                    b.id = rs.getInt("id");
                    b.name = rs.getString("name");
                    b.sourceCity = rs.getString("src");
                    b.destCity = rs.getString("dst");
                    b.fare = rs.getDouble("fare");
                    b.departureTime = rs.getString("departure_time");
                    b.seatsTotal = rs.getInt("seats_total");
                    b.seatsAvailable = rs.getInt("seats_available");
                    list.add(b);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Returns distinct routes [src, dst, minFare]
    public List<String[]> listDistinctRoutes(String query) {
        List<String[]> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT sc.name AS src, dc.name AS dst, MIN(b.fare) AS min_fare ")
          .append("FROM BUS b JOIN CITY sc ON b.source_city_id=sc.id JOIN CITY dc ON b.dest_city_id=dc.id ");
        boolean filtered = query != null && !query.trim().isEmpty();
        if (filtered) sb.append("WHERE LOWER(sc.name) LIKE ? OR LOWER(dc.name) LIKE ? ");
        sb.append("GROUP BY sc.name, dc.name ORDER BY sc.name, dc.name");
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            if (filtered) {
                String q = "%" + query.toLowerCase() + "%";
                ps.setString(1, q);
                ps.setString(2, q);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{rs.getString("src"), rs.getString("dst"), String.valueOf(rs.getDouble("min_fare"))});
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
