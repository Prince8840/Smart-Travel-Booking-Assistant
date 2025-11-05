# Smart Travel Booking Assistant

A Java Swing desktop app for searching routes/hotels, viewing a city image gallery, and booking/cancelling buses and hotels. Uses an embedded H2 database and resources-based city images.

## Requirements
- JDK 17+
- Maven 3.8+

## Build and Run
### Windows (PowerShell)
```
mvn -q clean package -DskipTests
java -jar target\travel-booking-app-1.0.0.jar
```

### macOS/Linux (bash/zsh)
```
mvn -q clean package -DskipTests
java -jar target/travel-booking-app-1.0.0.jar
```

### Run from an IDE
- Mark `src/main/java` as Sources and `src/main/resources` as Resources.
- Run main class: `com.example.travelbooking.Main`.

## Project Structure
```
TravelBookingApp/
├─ pom.xml
├─ README.md
├─ src/
│  ├─ main/
│  │  ├─ java/
│  │  │  └─ com/example/travelbooking/
│  │  │     ├─ Main.java                     # App entry
│  │  │     ├─ db/Database.java              # H2 schema + seeding
│  │  │     ├─ dao/                          # Bus/Hotel/Booking DAOs
│  │  │     ├─ service/                      # Booking/Search services
│  │  │     ├─ model/                        # POJOs (Bus, Hotel, Booking…)
│  │  │     ├─ data/CityRepository.java      # Cities, routes, hostels
│  │  │     └─ ui/                           # Swing UI panels & Theme
│  │  └─ resources/
│  │     └─ cities/
│  │        └─ <City>/                       # Drop city images here
│  │           └─ *.jpg|*.jpeg|*.png|*.gif
└─ target/
   └─ travel-booking-app-1.0.0.jar           # Built runnable JAR
```

## Adding/Updating City Images
- Add images to `src/main/resources/cities/<City>/` (e.g., `cities/Delhi/delhi1.jpeg`).
- Home and Gallery tabs load the first image per city and show galleries on click.
- Development (IDE/`mvn exec`): restart the app to pick up new files.
- Packaged JAR: rebuild the JAR after adding images.

## Database
- Embedded H2 file DB created in the project directory: `travel_booking_db.*`.
- On first run, schema is created and data is imported from `CityRepository` (cities, hotels, routes, and generated buses).
- Reset DB (start fresh): close the app and delete `travel_booking_db.*`, then run again.

## Using the App
- Home tab: search routes/hotels, double‑click a route or hotel to open the booking dialog.
- Gallery tab: browse cities; click a city to view all images.
- Bookings tab: view all bookings; use “Cancel by ID” to cancel and restore availability.

## Colors
The UI uses a fixed modern multi‑color scheme:
- Buttons (primary): purple/blue/pink/green
- Tables: routes (blue), hotels (pink)
- Lists: recommended (green)

## Troubleshooting
- If buttons are not visible, ensure JDK 17+ and run the latest build.
- If images don’t show, confirm the path is exactly `src/main/resources/cities/<City>/` and file extensions are one of: jpg, jpeg, png, gif, bmp.
