package com.example.travelbooking.service;

import com.example.travelbooking.dao.BusDao;
import com.example.travelbooking.dao.HotelDao;
import com.example.travelbooking.model.Bus;
import com.example.travelbooking.model.Hotel;

import java.util.List;

public class SearchService {
    private final BusDao busDao = new BusDao();
    private final HotelDao hotelDao = new HotelDao();

    public List<Bus> searchBusesByDestination(String city) {
        return busDao.findByDestination(city);
    }

    public List<Hotel> searchHotelsByCity(String city) {
        return hotelDao.findByCity(city);
    }
}
