# Xparience Backend

Java 21 + Spring Boot 3.4.1 REST API for the Xparience dating app.

## Requirements
- Java 21
- PostgreSQL 15+
- Maven 3.9+

## Setup

### 1. Create the database
```bash
createdb xparience_db
```

### 2. Set environment variables
Copy `.env.example` to `.env` and fill in values, then export:
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=your-256-bit-base64-secret
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
export CLOUDINARY_CLOUD_NAME=your-cloud-name
export CLOUDINARY_API_KEY=your-api-key
export CLOUDINARY_API_SECRET=your-api-secret
```

### 3. Run schema
```bash
psql -U postgres -d xparience_db -f src/main/resources/schema.sql
```

### 4. Run the app
```bash
mvn spring-boot:run
```

### 4b. Run with local profile (Windows PowerShell)
```powershell
./run-local.ps1
```

Or directly:
```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 5. View API docs
```
http://localhost:8080/swagger-ui.html
```

## API Overview

| Module        | Base Path               |
|---------------|-------------------------|
| Auth          | /api/v1/auth            |
| Profile       | /api/v1/profile         |
| Verification  | /api/v1/verification    |
| Matching      | /api/v1/matches         |
| Chat          | /api/v1/chat            |
| Dates         | /api/v1/dates           |
| AI            | /api/v1/ai              |
| Settings      | /api/v1/settings        |
| Subscription  | /api/v1/subscription    |
| User Info     | /api/v1/user            |