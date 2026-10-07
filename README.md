# Full Stack Hotel Booking Management System

Guests search for free rooms by date, book them, and manage their reservations. Admins manage rooms and bookings.

**Stack:** Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA / Hibernate, PostgreSQL, React 18 (Vite), Gradle.

## Architecture (layered MVC)

```
backend/src/main/java/dev/pradeep/hotelbooking
  controller/   REST endpoints (Auth, Room, Booking, Admin)
  service/      business rules (booking overlap check, pricing, cancellation)
  repository/   Spring Data JPA repositories and JPQL queries
  model/        JPA entities: User, Room, Booking (+ enums)
  dto/          request / response records
  security/     JWT service, auth filter, security config
  exception/    custom exceptions + global handler
  config/       DatabaseCreator, DataSeeder
frontend/src    React pages, components, auth context, API helper
```

## Prerequisites

- JDK 17 or newer
- Node.js 18 or newer
- PostgreSQL running on your machine (default port 5432)
- Gradle (or open `backend` in IntelliJ IDEA, which handles Gradle for you)

## 1. Connect your PostgreSQL database

Open `backend/src/main/resources/application.properties` and edit these three lines:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/hotel_booking_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_POSTGRES_PASSWORD
```

- Replace `YOUR_POSTGRES_PASSWORD` with your own PostgreSQL password.
- `hotel_booking_db` is the database name. If it does not exist, the app creates it on startup (see `config/DatabaseCreator.java`).
- Tables (`users`, `rooms`, `bookings`) are created automatically by Hibernate (`spring.jpa.hibernate.ddl-auto=update`).
- On first run a default admin and 8 sample rooms are added.

## 2. Run the backend

```bash
cd backend
gradle bootRun
```

If you prefer `./gradlew`, run `gradle wrapper` once inside `backend` first. The API starts on http://localhost:8080.

## 3. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. The Vite dev server forwards `/api` calls to the backend.

## Default admin login

| Email | Password |
|-------|----------|
| admin@hotel.com | Admin@123 |

Change these in `application.properties` (`app.admin.*`) before first run if you want different credentials. Also change `app.jwt.secret` to your own long random string.

## API summary

| Method | Path | Access |
|--------|------|--------|
| POST | /api/auth/register, /api/auth/login | public |
| GET | /api/rooms, /api/rooms/{id}, /api/rooms/available?checkIn=&checkOut=&guests= | public |
| POST | /api/bookings | signed in |
| GET | /api/bookings/my | signed in |
| PATCH | /api/bookings/{id}/cancel | owner |
| GET, POST, PUT, DELETE | /api/admin/rooms | admin |
| GET | /api/admin/bookings, /api/admin/stats | admin |
| PATCH | /api/admin/bookings/{id}/cancel | admin |

## How double booking is prevented

`BookingService.create` runs in a transaction and takes a pessimistic write lock on the room row, then counts confirmed bookings whose dates overlap (`checkIn < requestedCheckOut AND checkOut > requestedCheckIn`). If any exist, it rejects the request with HTTP 409. Two requests for the same room are therefore processed one after the other.
