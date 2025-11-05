package com.example.travelbooking.service;

import com.example.travelbooking.dao.BookingDao;
import com.example.travelbooking.dao.BusDao;
import com.example.travelbooking.dao.HotelDao;
import com.example.travelbooking.model.Booking;
import com.example.travelbooking.model.BookingType;

import java.sql.SQLException;
import java.time.LocalDate;

public class BookingService {
    private final BookingDao bookingDao = new BookingDao();
    private final BusDao busDao = new BusDao();
    private final HotelDao hotelDao = new HotelDao();

    public Booking bookBus(int busId, String customerName, int persons, LocalDate date, double farePerPerson) throws SQLException {
        if (!busDao.reduceSeats(busId, persons)) throw new SQLException("Not enough seats available");
        String busName = busDao.getNameById(busId);
        double total = farePerPerson * persons;
        return bookingDao.createBooking(BookingType.BUS, busId, busName, customerName, persons, date, total);
    }

    public Booking bookHotel(int hotelId, String customerName, int rooms, LocalDate date, double pricePerNight) throws SQLException {
        if (!hotelDao.reduceRooms(hotelId, rooms)) throw new SQLException("Not enough rooms available");
        String hotelName = hotelDao.getNameById(hotelId);
        double total = pricePerNight * rooms; // 1 night for simplicity
        return bookingDao.createBooking(BookingType.HOTEL, hotelId, hotelName, customerName, rooms, date, total);
    }

    public boolean cancelBooking(int bookingId) throws SQLException {
        var b = bookingDao.findById(bookingId);
        if (b == null || !"ACTIVE".equalsIgnoreCase(b.status)) return false;
        if (!bookingDao.cancel(bookingId)) return false;
        // restore availability
        switch (b.type) {
            case BUS -> busDao.restoreSeats(b.itemId, b.persons);
            case HOTEL -> hotelDao.restoreRooms(b.itemId, b.persons);
        }
        return true;
    }

    public java.util.List<com.example.travelbooking.model.Booking> listBookings() {
        return bookingDao.listAll();
    }
}
