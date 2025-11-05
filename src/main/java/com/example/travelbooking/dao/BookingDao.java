package com.example.travelbooking.dao;

import com.example.travelbooking.db.Database;
import com.example.travelbooking.model.Booking;
import com.example.travelbooking.model.BookingType;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDao {
    public Booking createBooking(BookingType type, int itemId, String itemName, String customerName, int persons, LocalDate date, double total) throws SQLException {
        String sql = "INSERT INTO BOOKING(ref_code, type, item_id, customer_name, persons, date, total_amount, status) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String tempRef = type.name().charAt(0) + String.valueOf(System.currentTimeMillis()).substring(7);
            ps.setString(1, tempRef);
            ps.setString(2, type.name());
            ps.setInt(3, itemId);
            ps.setString(4, customerName);
            ps.setInt(5, persons);
            ps.setDate(6, Date.valueOf(date));
            ps.setBigDecimal(7, new java.math.BigDecimal(total));
            ps.setString(8, "ACTIVE");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    String finalRef = (type == BookingType.BUS ? "B" : "H") + id;
                    try (PreparedStatement upd = conn.prepareStatement("UPDATE BOOKING SET ref_code=? WHERE id=?")) {
                        upd.setString(1, finalRef);
                        upd.setInt(2, id);
                        upd.executeUpdate();
                    }
                    Booking b = new Booking();
                    b.id = id; b.refCode = finalRef; b.type = type; b.itemId = itemId; b.itemName = itemName;
                    b.customerName = customerName; b.persons = persons; b.date = date; b.totalAmount = total; b.status = "ACTIVE";
                    return b;
                }
            }
        }
        throw new SQLException("Failed to create booking");
    }

    public List<Booking> listAll() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT id, ref_code, type, item_id, customer_name, persons, date, total_amount, status FROM BOOKING ORDER BY id DESC";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Booking b = new Booking();
                b.id = rs.getInt("id");
                b.refCode = rs.getString("ref_code");
                b.type = BookingType.valueOf(rs.getString("type"));
                b.itemId = rs.getInt("item_id");
                b.customerName = rs.getString("customer_name");
                b.persons = rs.getInt("persons");
                b.date = rs.getDate("date").toLocalDate();
                b.totalAmount = rs.getBigDecimal("total_amount").doubleValue();
                b.status = rs.getString("status");
                list.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean cancel(int bookingId) throws SQLException {
        String sql = "UPDATE BOOKING SET status='CANCELLED' WHERE id=? AND status='ACTIVE'";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    public Booking findById(int bookingId) throws SQLException {
        String sql = "SELECT id, ref_code, type, item_id, customer_name, persons, date, total_amount, status FROM BOOKING WHERE id=?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Booking b = new Booking();
                    b.id = rs.getInt("id");
                    b.refCode = rs.getString("ref_code");
                    b.type = BookingType.valueOf(rs.getString("type"));
                    b.itemId = rs.getInt("item_id");
                    b.customerName = rs.getString("customer_name");
                    b.persons = rs.getInt("persons");
                    b.date = rs.getDate("date").toLocalDate();
                    b.totalAmount = rs.getBigDecimal("total_amount").doubleValue();
                    b.status = rs.getString("status");
                    return b;
                }
            }
        }
        return null;
    }
}
