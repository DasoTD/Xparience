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

## Production Deployment (AWS EC2 + RDS + NGINX, Zero Downtime)

This repo now includes blue/green deployment assets:
- `deploy/nginx/xparience.conf`
- `deploy/systemd/xparience@.service`
- `deploy/ec2/bootstrap-ec2.sh`
- `deploy/ec2/deploy-blue-green.sh`
- `.github/workflows/cd-ec2.yml`

### One-time EC2 bootstrap
1. Launch Ubuntu EC2 and install Java 21, NGINX, and Maven.
2. Clone this repo on EC2 (or copy the `deploy/` folder).
3. Run bootstrap:
	```bash
	sudo bash deploy/ec2/bootstrap-ec2.sh
	```
4. Create production env file:
	```bash
	sudo cp deploy/ec2/common.env.example /etc/xparience/common.env
	sudo nano /etc/xparience/common.env
	```
5. Ensure security groups:
	- EC2 inbound: `80` (and `22` for admin)
	- RDS inbound: `5432` from EC2 security group only

### GitHub Actions CD setup
Add these GitHub repository secrets:
- `EC2_HOST` (public DNS/IP)
- `EC2_USER` (SSH user)
- `EC2_SSH_PRIVATE_KEY` (private key content)

### Deployment behavior
- CI/CD builds `app.jar` from `main` push.
- Artifact is copied to EC2.
- Deploy script starts the inactive slot (`blue`:`9091` or `green`:`9092`).
- Health check runs on inactive slot (`/api-docs`).
- NGINX upstream switches to healthy slot and reloads.
- Old slot is stopped after switch.

This provides near-zero downtime rolling cutover behind NGINX.

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