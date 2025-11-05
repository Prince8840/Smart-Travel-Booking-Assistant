package com.example.travelbooking.model;

import java.time.LocalDate;

public class Booking {
    public int id;
    public String refCode;
    public BookingType type;
    public int itemId;
    public String itemName; // convenience for UI
    public String customerName;
    public int persons;
    public LocalDate date;
    public double totalAmount;
    public String status; // ACTIVE or CANCELLED
}
