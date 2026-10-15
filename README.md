# Kumar Electrical Contractor — Spring Boot + Maven Backend

Java 17, Spring Boot 3.4, Maven, Spring Security, JPA/Hibernate and MySQL. The frontend is connected through a JSON REST API and sends the current company state to MySQL after admin changes. The employee API response is filtered to that employee's own profile, attendance, relevant sites and relevant projects.

## Requirements
- JDK 17 or newer
- Maven 3.9+
- MySQL 8+
- Frontend served from `http://localhost:5500` (not opened as `file://`)

## 1. Create database and user

Run this in MySQL (change the password):

```sql
CREATE DATABASE IF NOT EXISTS kumar_electrical CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'kec_app'@'localhost' IDENTIFIED BY 'ChangeThis_DB_Password';
GRANT ALL PRIVILEGES ON kumar_electrical.* TO 'kec_app'@'localhost';
FLUSH PRIVILEGES;
```

## 2. Configure environment variables

Linux/macOS:

```bash
export DB_URL='jdbc:mysql://localhost:3306/kumar_electrical?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata'
export DB_USERNAME='kec_app'
export DB_PASSWORD='ChangeThis_DB_Password'
export ADMIN_USERNAME='ADMIN'
export ADMIN_PASSWORD='Set-A-Long-Unique-Admin-Password'
export EMPLOYEE_DEFAULT_PASSWORD='ChangeThis-Employee-Password'
export CORS_ALLOWED_ORIGINS='http://localhost:5500,http://127.0.0.1:5500'
```

Windows PowerShell:

```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/kumar_electrical?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata'
$env:DB_USERNAME='kec_app'
$env:DB_PASSWORD='ChangeThis_DB_Password'
$env:ADMIN_USERNAME='ADMIN'
$env:ADMIN_PASSWORD='Set-A-Long-Unique-Admin-Password'
$env:EMPLOYEE_DEFAULT_PASSWORD='ChangeThis-Employee-Password'
$env:CORS_ALLOWED_ORIGINS='http://localhost:5500,http://127.0.0.1:5500'
```

Defaults `admin123` and `emp123` are for local development only. Never deploy with these defaults. This starter uses one configurable development password for employee accounts; production should replace this with per-employee password setup/reset and a password hash table. Do not commit production secrets to source control.

## 3. Run backend

From this folder:

```bash
mvn spring-boot:run
```

Health check: `http://localhost:8080/api/health` should return `{"status":"UP",...}`.

## 4. Run frontend

Open a terminal in the separate frontend folder and run:

```bash
python -m http.server 5500
```

Open `http://localhost:5500`. `api.js` is configured to use `http://localhost:8080/api` and backend mode by default.

## Demo credentials (development only)
- Admin: `ADMIN` / `admin123` (unless `ADMIN_PASSWORD` is set)
- Employees: `EMP001`, `EMP002`, or `EMP003` / `emp123` (unless `EMPLOYEE_DEFAULT_PASSWORD` is set)

The first admin save stores the initial state in MySQL. The backend's first empty-state response includes starter employee/site records so you can log in immediately.

## REST endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/health` | Health check |
| POST | `/api/auth/login` | Validate credentials and issue bearer token |
| POST | `/api/auth/logout` | Revoke token |
| GET | `/api/auth/me` | Current authenticated user |
| GET | `/api/state` | Load full state for admin or employee-filtered state for an employee |
| PUT | `/api/state` | Admin-only persist employees, sites, attendance, projects and wage adjustments |
| GET | `/api/employees` | Employee list (employee role receives only their own record) |
| GET | `/api/sites` | Worksites visible to signed-in user |
| GET | `/api/attendance` | Attendance visible to signed-in user |
| GET | `/api/assignments` | Assignment records (represented by attendance/work entries) |
| GET | `/api/projects` | Projects visible to signed-in user |
| GET | `/api/payroll?week=YYYY-MM-DD` | Admin-only attendance records used by the frontend payroll calculator |
| POST | `/api/attendance/verify-location` | Server-side distance calculation for configured site coordinates |

## How persistence works

The existing frontend was originally built around one shared browser data object. To preserve all existing pages and their UI, this integration stores that object as a JSON document in a MySQL `LONGTEXT` row (`application_state`) and synchronizes admin changes using `PUT /api/state`. It works across devices once each device signs into the same backend. It is a pragmatic integration bridge, not a normalized business schema. For a larger production system, migrate employees, worksites, attendance, projects, advances, deductions, payroll and audit events into separate relational tables and transactional CRUD endpoints.

## Security and production checklist
- HTTPS only in production; use a reverse proxy or managed hosting TLS.
- Replace all development passwords and add individual employee account provisioning/password reset.
- Current tokens are random, in-memory bearer tokens; server restart invalidates sessions. For production use persistent/rotating secure sessions or a vetted token/session strategy, rate limiting and account lockout.
- Add audit records for admin attendance edits, wage changes and deletions.
- Validate all attendance/payroll data on the server; never trust the browser's wage calculations.
- Current GPS distance check is a location signal only, not anti-spoofing or identity verification.
- No fingerprint is collected or faked. Add WebAuthn/passkeys for browser biometric-capable authenticators and native Android/iOS biometric APIs separately if required.
- Worksite photos are currently included in the JSON state as data URLs. For production, move them to controlled file/object storage and save only an asset URL in MySQL.
- Back up the MySQL database and configure retention, monitoring and error handling before live employee use.
