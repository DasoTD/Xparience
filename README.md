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
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/xparience_db
export JWT_SECRET=your-256-bit-base64-secret
export MAIL_USERNAME=timileyindaso@gmail.com
export MAIL_PASSWORD=
export CLOUDINARY_CLOUD_NAME=your-cloud-name
export CLOUDINARY_API_KEY=your-api-key
export CLOUDINARY_API_SECRET=your-api-secret
```

### Using AWS PostgreSQL (RDS) instead of local DB
Set these before starting the app:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://xpdb.cty404w2y67l.eu-west-2.rds.amazonaws.com:5432/xpdb?sslmode=require
export DB_USERNAME=postgres
export DB_PASSWORD=
```

PowerShell equivalent:
```powershell
$env:SPRING_DATASOURCE_URL='jdbc:postgresql://xpdb.cty404w2y67l.eu-west-2.rds.amazonaws.com:5432/xpdb?sslmode=require'
$env:DB_USERNAME='postgres'
$env:DB_PASSWORD=''
```

If this is a fresh/empty RDS database, add:
```powershell
$env:SPRING_SQL_INIT_MODE='never'
```
This repository's `schema.sql` contains migration-style `ALTER` statements and expects base tables to already exist.

### 3. Schema migration (automatic)
On startup, the app now runs `src/main/resources/schema.sql` automatically (`spring.sql.init.mode=always`) and applies idempotent fixes, including:
- `users.two_factor_enabled`
- `otp_tokens_type_check` including `LOGIN_2FA`

For fresh databases with no existing tables, set `SPRING_SQL_INIT_MODE=never` and use your baseline schema creation flow first.

You can still run the script manually if needed:
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

## Production Deployment (AWS Elastic Beanstalk)

Production deployment now uses Elastic Beanstalk via:
- `.github/workflows/cd-eb.yml`
- `Procfile`

### One-time Elastic Beanstalk setup
1. Create an Elastic Beanstalk application and environment (Java 21 / Corretto platform recommended).
2. In your EB environment, configure application environment variables (same values you currently use on EC2):
   - `SPRING_DATASOURCE_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
   - `JWT_SECRET`
   - `MAIL_USERNAME`, `MAIL_PASSWORD`
   - `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`
3. Ensure the EB instance profile and network can reach your RDS endpoint.

### GitHub Actions CD setup (EB)
Set repository **secrets**:
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_REGION` (or set as variable)
- `EB_APPLICATION_NAME` (or set as variable)
- `EB_ENVIRONMENT_NAME` (or set as variable)

Set repository **variables** (preferred):
- `AWS_REGION`
- `EB_APPLICATION_NAME`
- `EB_ENVIRONMENT_NAME`

### Deployment behavior
- On push to `main`, workflow builds `app.jar`.
- Workflow packages `app.jar` + `Procfile` into `deploy.zip`.
- Workflow uploads bundle to Elastic Beanstalk storage S3 bucket.
- Workflow creates a version label from commit SHA and updates the EB environment.
- Workflow waits for environment update completion and prints environment health/status.

### Rollback
In AWS Console (Elastic Beanstalk), deploy a previous application version label to the same environment.

### Legacy EC2 blue/green deploy (manual)
The EC2 blue/green assets are still present for manual usage:
- `deploy/nginx/xparience.conf`
- `deploy/systemd/xparience@.service`
- `deploy/ec2/bootstrap-ec2.sh`
- `deploy/ec2/deploy-blue-green.sh`
- `.github/workflows/cd-ec2.yml` (manual dispatch only)

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