package com.example.travelbooking.data;

import com.example.travelbooking.model.City;
import com.example.travelbooking.model.Hostel;
import com.example.travelbooking.model.Route;

import java.util.*;
import java.util.stream.Collectors;

public class CityRepository {
    private static final Map<String, City> cities = new LinkedHashMap<>();
    private static final List<Route> routes = new ArrayList<>();
    private static final Random rnd = new Random();

    static {
        seed();
    }

    private static void seed() {
        // curated cities with resources-based images (src/main/resources/cities/<City>/<files>)
        addCity("Delhi", "Capital city with Red Fort, India Gate.",
                "cities/Delhi/delhi1.jpg",
                List.of("Gurugram", "Jaipur", "Agra"),
                List.of(new Hostel("Old Delhi Hostel", 1200, 10), new Hostel("Connaught Place Inn", 2200, 6)));
        addCity("Gurugram", "Millennium city, corporate hub.",
                "cities/Gurugram/gurugram1.jpg",
                List.of("Delhi", "Jaipur"),
                List.of(new Hostel("Cyber City Hostel", 1500, 8)));
        addCity("Jaipur", "Pink City, Hawa Mahal, Amber Fort.",
                "cities/Jaipur/jaipur1.jpg",
                List.of("Delhi", "Udaipur", "Agra"),
                List.of(new Hostel("Pink City Hostel", 1000, 12), new Hostel("Amber Stay", 1800, 5)));
        addCity("Udaipur", "City of Lakes, City Palace.",
                "cities/Udaipur/udaipur1.jpg",
                List.of("Jaipur", "Ahmedabad"),
                List.of(new Hostel("Lake View Hostel", 1300, 7)));
        addCity("Agra", "Taj Mahal, Agra Fort.",
                "cities/Agra/agra1.jpg",
                List.of("Delhi", "Jaipur"),
                List.of(new Hostel("Taj Hostel", 900, 15)));
        addCity("Mumbai", "Financial capital, Marine Drive.",
                "cities/Mumbai/mumbai1.jpg",
                List.of("Pune", "Surat", "Ahmedabad"),
                List.of(new Hostel("Marine Drive Hostel", 2000, 9)));
        addCity("Pune", "Educational hub.",
                "cities/Pune/pune1.jpg",
                List.of("Mumbai", "Nashik"),
                List.of(new Hostel("Deccan Hostel", 1100, 11)));
        addCity("Ahmedabad", "Sabarmati Ashram.",
                "cities/Ahmedabad/ahmedabad1.jpg",
                List.of("Udaipur", "Surat", "Mumbai"),
                List.of(new Hostel("Sabarmati Stay", 1000, 10)));

        // Generate additional placeholder cities to get 120+
        String placeholderImg = "https://via.placeholder.com/300x200.png?text=%s";
        String[] baseNames = {"Kolkata","Chennai","Bengaluru","Hyderabad","Kochi","Varanasi","Lucknow","Bhopal","Indore","Nagpur","Surat","Vadodara","Rajkot","Jodhpur","Jaisalmer","Amritsar","Chandigarh","Shimla","Manali","Rishikesh","Haridwar","Dehradun","Nainital","Leh","Srinagar","Gwalior","Patna","Ranchi","Guwahati","Shillong","Mysuru","Madurai","Coimbatore","Visakhapatnam","Vijayawada","Trivandrum","Goa","Noida","Ghaziabad","Kanpur","Prayagraj","Meerut","Aligarh","Ajmer","Alwar","Bikaner","Kota","Ujjain","Aurangabad","Nashik","Thane","Kalyan","Vasai","Faridabad","Rohtak","Sonipat","Panipat","Gurugram-2","Howrah","Durgapur","Asansol","Jabalpur","Bilaspur","Raipur","Dhamtari","Cuttack","Bhubaneswar","Puri","Rourkela","Jamshedpur","Dhanbad","Hazaribagh","Bareilly","Moradabad","Saharanpur","Firozabad","Agartala","Imphal","Aizawl","Kohima","Itanagar","Port Blair","Puducherry","Kavaratti","Diu","Daman","Silvassa","Gandhinagar","Dwarka","Somnath","Junagadh","Porbandar","Kutch","Kullu","Kasol","Spiti","Kargil","Pahalgam","Gulmarg","Munnar","Ooty","Kodaikanal","Alleppey","Hampi","Badami","Pattadakal","Halebidu","Belur"};
        Set<String> seen = new HashSet<>(cities.keySet());
        for (String nm : baseNames) {
            if (!seen.add(nm)) continue;
            addCity(nm,
                    nm + " is a beautiful place with popular attractions.",
                    String.format(placeholderImg, nm.replace(' ', '+')),
                    List.of(),
                    List.of(new Hostel(nm + " Backpackers", 800 + rnd.nextInt(1500), 5 + rnd.nextInt(15))));
        }
        // simple random connections
        List<String> names = new ArrayList<>(cities.keySet());
        for (City c : cities.values()) {
            if (c.getConnectedCities().isEmpty()) {
                int links = 1 + rnd.nextInt(3);
                List<String> picks = new ArrayList<>();
                for (int i = 0; i < links; i++) picks.add(names.get(rnd.nextInt(names.size())));
                c.setConnectedCities(picks);
            }
        }
        // sample routes + more bus routes
        routes.add(buildRoute(List.of("Delhi","Gurugram","Jaipur"), 280, 5));
        routes.add(buildRoute(List.of("Delhi","Agra","Jaipur","Udaipur"), 720, 12));
        routes.add(buildRoute(List.of("Mumbai","Pune"), 150, 3));
        routes.add(buildRoute(List.of("Jaipur","Udaipur","Ahmedabad"), 660, 11));
        routes.add(buildRoute(List.of("Delhi","Jaipur"), 280, 5));
        // Additional bus-like routes
        routes.add(buildRoute(List.of("Chennai","Bengaluru"), 350, 6.0));
        routes.add(buildRoute(List.of("Kolkata","Patna"), 560, 9.0));
        routes.add(buildRoute(List.of("Hyderabad","Visakhapatnam"), 620, 8.5));
        routes.add(buildRoute(List.of("Ahmedabad","Surat","Mumbai"), 530, 8.0));
        routes.add(buildRoute(List.of("Delhi","Haridwar","Rishikesh"), 250, 6.0));
        routes.add(buildRoute(List.of("Bengaluru","Mysuru"), 150, 3.5));
        routes.add(buildRoute(List.of("Jaipur","Jodhpur"), 340, 6.0));
        routes.add(buildRoute(List.of("Jaipur","Ajmer","Pushkar"), 150, 4.0));
        routes.add(buildRoute(List.of("Pune","Nashik"), 210, 5.0));
        routes.add(buildRoute(List.of("Kochi","Munnar"), 130, 4.5));
        routes.add(buildRoute(List.of("Goa","Belagavi","Hubballi"), 180, 5.0));
    }

    private static void addCity(String name, String desc, String img, List<String> connected, List<Hostel> hostels) {
        cities.put(name, new City(name, desc, img, new ArrayList<>(connected), new ArrayList<>(hostels)));
    }

    private static Route buildRoute(List<String> cityNames, double km, double hrs) {
        List<City> list = cityNames.stream().map(cities::get).filter(Objects::nonNull).collect(Collectors.toList());
        return new Route(list, km, hrs);
    }

    public static Collection<City> getAllCities() { return cities.values(); }
    public static List<Route> getAllRoutes() { return routes; }

    public static List<Route> searchRoutes(String query) {
        String q = query.toLowerCase();
        return routes.stream().filter(r -> r.getCitiesOnRoute().stream().map(City::getName).anyMatch(n -> n.toLowerCase().contains(q)))
                .collect(Collectors.toList());
    }

    public static List<City> searchCities(String query) {
        String q = query.toLowerCase();
        return cities.values().stream().filter(c -> c.getName().toLowerCase().contains(q) || c.getDescription().toLowerCase().contains(q)).collect(Collectors.toList());
    }

    public static List<Route> recommendedTrips(int count) {
        List<Route> copy = new ArrayList<>(routes);
        Collections.shuffle(copy, rnd);
        return copy.subList(0, Math.min(count, copy.size()));
    }

    public static List<City> popularCities(int count) {
        return new ArrayList<>(cities.values()).subList(0, Math.min(count, cities.size()));
    }
}
