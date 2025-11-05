package com.example.travelbooking.model;

import java.util.List;

public class Route {
    private List<City> citiesOnRoute;
    private double totalDistance; // km
    private double estimatedTime; // hours

    public Route(List<City> citiesOnRoute, double totalDistance, double estimatedTime) {
        this.citiesOnRoute = citiesOnRoute;
        this.totalDistance = totalDistance;
        this.estimatedTime = estimatedTime;
    }

    public List<City> getCitiesOnRoute() { return citiesOnRoute; }
    public double getTotalDistance() { return totalDistance; }
    public double getEstimatedTime() { return estimatedTime; }

    public void setCitiesOnRoute(List<City> citiesOnRoute) { this.citiesOnRoute = citiesOnRoute; }
    public void setTotalDistance(double totalDistance) { this.totalDistance = totalDistance; }
    public void setEstimatedTime(double estimatedTime) { this.estimatedTime = estimatedTime; }
}
