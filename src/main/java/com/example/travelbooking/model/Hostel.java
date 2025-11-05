package com.example.travelbooking.model;

public class Hostel {
    private String name;
    private double pricePerNight;
    private int availableRooms;

    public Hostel(String name, double pricePerNight, int availableRooms) {
        this.name = name;
        this.pricePerNight = pricePerNight;
        this.availableRooms = availableRooms;
    }

    public String getName() { return name; }
    public double getPricePerNight() { return pricePerNight; }
    public int getAvailableRooms() { return availableRooms; }

    public void setName(String name) { this.name = name; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }
}
