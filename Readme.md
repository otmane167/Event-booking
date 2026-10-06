# Evently - Event Booking Platform

Evently is a full-stack event booking application for discovering events, creating organizer-managed listings, managing ticket inventory, and reserving tickets. The project includes a Spring Boot REST API, PostgreSQL persistence, JWT authentication, and a React + Vite frontend.

## Features

- User registration and login with JWT-based authentication
- Attendee and organizer account roles
- Event discovery with search and status filters
- Event creation, update, and deletion for organizers
- Ticket type management with price and quantity tracking
- Booking flow with ticket quantity selection
- Personal booking history for signed-in users
- Backend stock checks, validation, and centralized exception handling

## Tech Stack

**Frontend**

- React 19
- TypeScript
- Vite
- Oxlint

**Backend**

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT with JJWT
- Maven

## Project Structure

```text
Event-Booking/
|-- Backend/
|   |-- pom.xml
|   `-- src/main/java/com/event/booking/
|       |-- config/
|       |-- controller/
|       |-- exception/
|       |-- model/
|       |-- repository/
|       `-- service/
|-- Frontend/
|   |-- package.json
|   `-- src/
|-- docker-compose.yml
`-- Readme.md
```

## Prerequisites

- Java 21
- Node.js and npm
- PostgreSQL 16 or a compatible PostgreSQL instance
- Maven is optional because the backend includes Maven wrapper scripts

## Environment Setup

Create a PostgreSQL database that matches the backend configuration:

```sql
CREATE DATABASE booking_db;
CREATE USER booking_user WITH PASSWORD 'booking_pass';
GRANT ALL PRIVILEGES ON DATABASE booking_db TO booking_user;
```

The backend currently reads these defaults from `Backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/booking_db
spring.datasource.username=booking_user
spring.datasource.password=booking_pass
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## Run Locally

### 1. Start the backend

From the backend directory:

```bash
cd Backend
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
cd Backend
.\mvnw.cmd spring-boot:run
```

The API runs on `http://localhost:8080`.

### 2. Start the frontend

In a second terminal:

```bash
cd Frontend
npm install
npm run dev
```

The Vite app usually runs on `http://localhost:5173`.

If the API is hosted somewhere other than the same origin, create `Frontend/.env` and set:

```env
VITE_API_URL=http://localhost:8080
```

## Available Scripts

Frontend:

```bash
npm run dev
npm run build
npm run lint
npm run preview
```

Backend:

```bash
./mvnw test
./mvnw spring-boot:run
./mvnw clean package
```

## API Overview

Authentication:

- `POST /api/auth/login` - log in and receive a JWT

Users:

- `POST /api/users` - create an account
- `GET /api/users` - list users
- `GET /api/users/{id}` - get one user

Events:

- `GET /api/events` - list events
- `GET /api/events/{id}` - get one event
- `POST /api/events?organizerId={id}` - create an event
- `PUT /api/events/{id}?organizerId={id}` - update an event
- `DELETE /api/events/{id}?organizerId={id}` - delete an event

Ticket types:

- `GET /api/ticket-types?eventId={id}` - list tickets for an event
- `POST /api/ticket-types?eventId={id}&organizerId={id}` - create a ticket type
- `PUT /api/ticket-types/{id}?organizerId={id}` - update a ticket type
- `DELETE /api/ticket-types/{id}?organizerId={id}` - delete a ticket type

Bookings:

- `POST /api/bookings?userId={id}&ticketTypeId={id}&quantity={number}` - create a booking
- `GET /api/bookings?userId={id}` - list bookings for a user

Most endpoints require an `Authorization: Bearer <token>` header. Registration and login are public.

## Testing

Run backend tests:

```bash
cd Backend
./mvnw test
```

Run frontend validation:

```bash
cd Frontend
npm run build
npm run lint
```

## Docker Notes

The repository includes a `docker-compose.yml` with PostgreSQL, backend, and frontend services. The compose file expects Dockerfiles in the backend and frontend folders. Add those Dockerfiles before using:

```bash
docker compose up --build
```

## License

This project is available for learning and portfolio use. Add a license file before publishing it for broader reuse.
