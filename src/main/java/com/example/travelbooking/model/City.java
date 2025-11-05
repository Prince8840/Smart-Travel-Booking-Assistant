package com.example.travelbooking.model;

import java.util.List;

public class City {
    private String name;
    private String description;
    private String imagePath; // can be file path or URL
    private List<String> connectedCities; // names
    private List<Hostel> hostels;

    public City(String name, String description, String imagePath, List<String> connectedCities, List<Hostel> hostels) {
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.connectedCities = connectedCities;
        this.hostels = hostels;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
    public List<String> getConnectedCities() { return connectedCities; }
    public List<Hostel> getHostels() { return hostels; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setConnectedCities(List<String> connectedCities) { this.connectedCities = connectedCities; }
    public void setHostels(List<Hostel> hostels) { this.hostels = hostels; }
}
