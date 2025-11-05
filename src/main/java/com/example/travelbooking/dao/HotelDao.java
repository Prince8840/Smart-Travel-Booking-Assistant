package com.example.travelbooking.dao;

import com.example.travelbooking.db.Database;
import com.example.travelbooking.model.Hotel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HotelDao {
    public List<Hotel> findByCity(String cityLike) {
        List<Hotel> list = new ArrayList<>();
        String sql = "SELECT h.id,h.name,c.name AS city,h.rating,h.rooms_total,h.rooms_available,h.price_per_night " +
                "FROM HOTEL h JOIN CITY c ON h.city_id=c.id " +
                "WHERE LOWER(c.name) LIKE LOWER(?) ORDER BY h.price_per_night ASC";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + cityLike + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Hotel h = new Hotel();
                    h.id = rs.getInt("id");
                    h.name = rs.getString("name");
                    h.city = rs.getString("city");
                    h.rating = rs.getDouble("rating");
                    h.roomsTotal = rs.getInt("rooms_total");
                    h.roomsAvailable = rs.getInt("rooms_available");
                    h.pricePerNight = rs.getDouble("price_per_night");
                    list.add(h);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Hotel> listAll() {
        List<Hotel> list = new ArrayList<>();
        String sql = "SELECT h.id,h.name,c.name AS city,h.rating,h.rooms_total,h.rooms_available,h.price_per_night " +
                "FROM HOTEL h JOIN CITY c ON h.city_id=c.id ORDER BY h.id ASC";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Hotel h = new Hotel();
                h.id = rs.getInt("id");
                h.name = rs.getString("name");
                h.city = rs.getString("city");
                h.rating = rs.getDouble("rating");
                h.roomsTotal = rs.getInt("rooms_total");
                h.roomsAvailable = rs.getInt("rooms_available");
                h.pricePerNight = rs.getDouble("price_per_night");
                list.add(h);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Double getPriceById(int id) throws SQLException {
        String sql = "SELECT price_per_night FROM HOTEL WHERE id=?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return null;
    }

    public boolean reduceRooms(int hotelId, int count) throws SQLException {
        String sql = "UPDATE HOTEL SET rooms_available = rooms_available - ? WHERE id=? AND rooms_available>=?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, count);
            ps.setInt(2, hotelId);
            ps.setInt(3, count);
            return ps.executeUpdate() > 0;
        }
    }

    public void restoreRooms(int hotelId, int count) throws SQLException {
        String sql = "UPDATE HOTEL SET rooms_available = rooms_available + ? WHERE id=?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, count);
            ps.setInt(2, hotelId);
            ps.executeUpdate();
        }
    }

    public String getNameById(int id) throws SQLException {
        String sql = "SELECT name FROM HOTEL WHERE id=?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return "Hotel " + id;
    }
}
