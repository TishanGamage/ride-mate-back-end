<p align="center">
  <img src="src/main/resources/assets/ride-mate-logo-dark.png" alt="RideMate Logo" width="200"/>
</p>

<h1 align="center">RideMate Backend</h1>

<p align="center">
  A production-grade ride-sharing backend application built with Spring Boot, providing RESTful APIs for passenger and driver management, ride operations, payments, and more.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot 3.2.0"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL"/>
  <img src="https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/Jenkins-CI%2FCD-D24939?style=for-the-badge&logo=jenkins&logoColor=white" alt="Jenkins"/>
</p>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [API Endpoints](#-api-endpoints)
- [Database Schema](#-database-schema)
- [Authentication & Security](#-authentication--security)
- [API Documentation (Swagger)](#-api-documentation-swagger)
- [Deployment](#-deployment)
- [Environment Variables](#-environment-variables)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🚀 Overview

**RideMate** is a comprehensive ride-sharing platform backend that supports multi-role users (Passengers, Drivers, and Admins). The application handles the complete lifecycle of ride-sharing operations including user registration, email verification, driver onboarding, ride management, payment processing, and earnings/withdrawal management.

### Key Features

- **User Management** — Registration, login, profile management with multi-role support (Passenger / Driver / Admin)
- **Email Verification** — 6-digit code verification via SMTP email service
- **JWT Authentication** — Secure access and refresh token-based authentication
- **Driver Onboarding** — Driver profile, vehicle details, and document management
- **Ride Management** — Create, track, and manage ride details with shared ride support
- **Payment Processing** — Integrated with PayHere payment gateway for card payments
- **File Management** — Document and image uploads via Supabase Storage
- **Earnings & Withdrawals** — Driver earnings tracking and withdrawal request processing
- **Vehicle Reference Data** — Vehicle types, makes, and models management
- **Swagger/OpenAPI** — Interactive API documentation with Swagger UI

---

## 🛠 Technology Stack

### Core

| Technology          | Version | Purpose                          |
|---------------------|---------|----------------------------------|
| **Java**            | 21      | Programming language             |
| **Spring Boot**     | 3.2.0   | Application framework            |
| **Spring Security** | 6.x     | Authentication & authorization   |
| **Spring Data JPA** | 3.x     | ORM & database access            |
| **Hibernate**       | 6.x     | JPA implementation               |
| **Maven**           | 3.x     | Build tool & dependency management |

### Database & Migrations

| Technology    | Purpose                              |
|---------------|--------------------------------------|
| **MySQL**     | Relational database                  |
| **Liquibase** | Database schema migration management |

### Security & Auth

| Technology | Version | Purpose              |
|------------|---------|----------------------|
| **JWT (jjwt)** | 0.12.5 | JSON Web Token authentication |
| **Spring Security** | 6.x | Security framework |
| **BCrypt** | — | Password hashing |

### API & Documentation

| Technology             | Version | Purpose                       |
|------------------------|---------|-------------------------------|
| **SpringDoc OpenAPI**  | 2.3.0   | Swagger UI & API documentation |
| **Swagger Annotations** | 2.2.15 | API endpoint annotations       |

### External Services

| Service          | Purpose                    |
|------------------|----------------------------|
| **Gmail SMTP**   | Email verification service |
| **Supabase Storage** | File/document storage  |
| **PayHere**      | Payment gateway integration |

### DevOps & Deployment

| Technology        | Purpose                        |
|-------------------|--------------------------------|
| **Docker**        | Containerization               |
| **Docker Compose** | Multi-container orchestration |
| **Jenkins**       | CI/CD pipeline automation      |

### Developer Tools

| Technology   | Purpose                          |
|--------------|----------------------------------|
| **Lombok**   | Boilerplate code reduction       |
| **Spring Boot DevTools** | Hot reload during development |
| **Jakarta Bean Validation** | Input validation          |

---

## 🏗 Architecture

The application follows a **layered architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────┐
│              Client (Mobile / Web)           │
└────────────────────┬────────────────────────┘
                     │ HTTP/REST
┌────────────────────▼────────────────────────┐
│            Controller Layer                  │
│   (REST endpoints, request/response DTOs)    │
├─────────────────────────────────────────────┤
│             Service Layer                    │
│   (Business logic, validation, transactions) │
├─────────────────────────────────────────────┤
│            Repository Layer                  │
│        (Spring Data JPA repositories)        │
├─────────────────────────────────────────────┤
│             Domain Layer                     │
│         (JPA entities / DB models)           │
├─────────────────────────────────────────────┤
│         MySQL Database (Liquibase)           │
└─────────────────────────────────────────────┘
```

### Cross-Cutting Concerns

- **Security** — JWT-based authentication filter chain
- **Exception Handling** — Global exception handler (`BaseResponseEntityExceptionHandler`)
- **Logging** — SLF4J with Lombok `@Slf4j`
- **Validation** — Jakarta Bean Validation with externalized messages

---

## 📁 Project Structure 

```
com.ride.mate/
├── config/              # Security, JWT, Web configuration
│   ├── SecurityConfig
│   ├── JwtAuthenticationFilter
│   ├── JwtAuthenticationEntryPoint
│   └── WebConfig
├── controller/          # REST API controllers
│   ├── AuthController
│   ├── UserController
│   ├── UserProfileController
│   ├── DriverProfileController
│   ├── RideDetailController
│   ├── PaymentController
│   ├── FileController
│   ├── WithdrawalController
│   └── ... (reference data controllers)
├── core/                # Base classes & utilities
│   ├── BaseEntity
│   ├── BaseResponseEntityExceptionHandler
│   ├── MessagePropertyBase
│   └── LoginAuthentication
├── domain/              # JPA entity classes
│   ├── User
│   ├── UserProfile
│   ├── DriverProfile
│   ├── DriverVehicleDetails
│   ├── RideDetail
│   ├── PaymentTransaction
│   └── ... (18 entities total)
├── enums/               # Application enumerations
│   ├── UserRole (PASSENGER, DRIVER, ADMIN)
│   ├── UserStatus (ACTIVE, SUSPENDED, INACTIVE)
│   ├── PaymentStatus
│   ├── WithdrawalStatus
│   └── YesNo
├── exception/           # Custom exceptions
│   └── ValidateRecordException
├── repository/          # Spring Data JPA repositories
├── resources/           # DTOs (Request/Response objects)
├── service/             # Service interfaces
│   └── impl/            # Service implementations
└── util/                # Utility classes
    ├── DateUtil
    ├── JwtUtil
    └── ConversionUtil
```

---

## 📦 Prerequisites

Before running the application, ensure you have:

- **Java 21** (JDK) — [Download](https://adoptium.net/)
- **Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** — [Download](https://dev.mysql.com/downloads/)
- **Docker** *(optional, for containerized deployment)* — [Download](https://www.docker.com/products/docker-desktop)
- **Git** — [Download](https://git-scm.com/)

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/TishanGamage/ride-mate-back-end.git
cd ride-mate-back-end
```

### 2. Set Up the Database

Create a MySQL database (it will be auto-created if using the local config):

```sql
CREATE DATABASE ride_mate_db;
```

### 3. Configure Local Properties

Update `src/main/resources/application-local.properties` with your local MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ride_mate_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run -Plocal
```

The application will start on **`http://localhost:8080/ride-mate`**

### 6. Access Swagger UI

Navigate to:

```
http://localhost:8080/ride-mate/swagger-ui.html
```

---

## ⚙ Configuration

The application supports two profiles:

| Profile   | File                             | Purpose               |
|-----------|----------------------------------|-----------------------|
| **local** | `application-local.properties`   | Local development     |
| **prod**  | `application-prod.properties`    | Production deployment |

### Key Configuration Properties

| Property                          | Description                        |
|-----------------------------------|------------------------------------|
| `server.servlet.context-path`     | API base path (`/ride-mate`)       |
| `spring.datasource.*`            | MySQL database connection          |
| `spring.mail.*`                  | SMTP email configuration           |
| `jwt.secret`                     | JWT signing secret key             |
| `jwt.access-token.expiration`    | Access token TTL (ms) — default: 15 min |
| `jwt.refresh-token.expiration`   | Refresh token TTL (ms) — default: 7 days |
| `supabase.url` / `supabase.key` | Supabase storage credentials       |
| `payhere.merchant-id`            | PayHere merchant credentials       |
| `spring.servlet.multipart.max-file-size` | Max upload size (20MB)    |

---

## 📡 API Endpoints

All endpoints are prefixed with **`/ride-mate`**.

### Authentication

| Method | Endpoint                | Description                |
|--------|-------------------------|----------------------------|
| POST   | `/auth/register`        | User registration          |
| POST   | `/auth/login`           | User login (JWT)           |
| POST   | `/auth/refresh-token`   | Refresh access token       |
| POST   | `/auth/send-verification` | Send email verification code |
| POST   | `/auth/verify-code`     | Verify email code          |
| POST   | `/auth/reset-password`  | Reset password             |

### User Management

| Method | Endpoint                | Description                |
|--------|-------------------------|----------------------------|
| GET    | `/user/{id}`            | Get user by ID             |
| PUT    | `/user/{id}`            | Update user                |
| PUT    | `/user/role/{id}`       | Update user role           |

### User Profile

| Method | Endpoint                | Description                |
|--------|-------------------------|----------------------------|
| POST   | `/user-profile`         | Create user profile        |
| PUT    | `/user-profile/{id}`    | Update user profile        |
| GET    | `/user-profile/{id}`    | Get user profile           |

### Driver Profile

| Method | Endpoint                | Description                |
|--------|-------------------------|----------------------------|
| POST   | `/driver-profile`       | Create driver profile      |
| PUT    | `/driver-profile/{id}`  | Update driver profile      |
| GET    | `/driver-profile/{id}`  | Get driver profile         |

### Ride Management

| Method | Endpoint                | Description                |
|--------|-------------------------|----------------------------|
| POST   | `/ride-detail`          | Create ride                |
| GET    | `/ride-detail/{id}`     | Get ride details           |
| PUT    | `/ride-detail/{id}`     | Update ride                |

### Payment

| Method | Endpoint                | Description                |
|--------|-------------------------|----------------------------|
| POST   | `/payment/initiate`     | Initiate payment           |
| POST   | `/payment/notify`       | PayHere webhook callback   |

### Withdrawals

| Method | Endpoint                | Description                |
|--------|-------------------------|----------------------------|
| POST   | `/withdrawal`           | Create withdrawal request  |
| PUT    | `/withdrawal/{id}`      | Update withdrawal status   |

### File Management

| Method | Endpoint                | Description                |
|--------|-------------------------|----------------------------|
| POST   | `/file/upload`          | Upload file(s)             |
| DELETE | `/file/{id}`            | Delete file                |

### Reference Data

| Method | Endpoint                      | Description              |
|--------|-------------------------------|--------------------------|
| GET    | `/identification-type`        | List identification types |
| GET    | `/vehicle-type`               | List vehicle types       |
| GET    | `/vehicle-make`               | List vehicle makes       |
| GET    | `/vehicle-model`              | List vehicle models      |

> 📝 *For the full list of endpoints with request/response schemas, refer to the [Swagger UI](#-api-documentation-swagger).*

---

## 🗃 Database Schema

The application uses **18 tables** managed by Liquibase migrations:

### Core Tables

| Table                         | Description                             |
|-------------------------------|-----------------------------------------|
| `user`                        | Authentication & account management     |
| `user_profile`                | Extended user profile information       |
| `emergency_contacts`          | User emergency contacts                 |
| `user_identification_details` | User identification documents           |
| `driver_profile`              | Driver-specific information             |
| `driver_vehicle_details`      | Driver vehicle information              |
| `document_details`            | Uploaded documents with URLs            |

### Ride & Payment Tables

| Table                  | Description                          |
|------------------------|--------------------------------------|
| `ride_detail`          | Ride information and status          |
| `shared_ride_detail`   | Shared ride participant details      |
| `payment_transaction`  | Payment records                      |
| `user_saved_card`      | Saved payment card details           |
| `driver_earning`       | Driver earnings tracking             |
| `withdrawal_request`   | Driver withdrawal requests           |

### Reference / Lookup Tables

| Table                | Description                       |
|----------------------|-----------------------------------|
| `identification_type`| ID type lookup (NIC, Passport...) |
| `vehicle_type`       | Vehicle types with pricing        |
| `vehicle_make`       | Vehicle manufacturers             |
| `vehicle_model`      | Vehicle models                    |
| `verification_codes` | Email verification codes          |

> 📄 For detailed column-level documentation, see [DATABASE_STRUCTURE.md](DATABASE_STRUCTURE.md).

---

## 🔐 Authentication & Security

### JWT Authentication Flow

```
1. User registers  →  POST /auth/register
2. Email verified  →  POST /auth/send-verification → POST /auth/verify-code
3. User logs in    →  POST /auth/login  →  Returns { accessToken, refreshToken }
4. Access APIs     →  Authorization: Bearer <accessToken>
5. Token expired   →  POST /auth/refresh-token  →  Returns new accessToken
```

### Security Features

- **Password Hashing** — BCrypt encoding via Spring Security's `PasswordEncoder`
- **JWT Tokens** — Access tokens (15 min) and refresh tokens (7 days)
- **Email Verification** — 6-digit code with expiration and max attempts
- **Optimistic Locking** — Version-based concurrency control on all entities
- **CORS** — Cross-Origin Resource Sharing enabled for all origins
- **Input Validation** — Jakarta Bean Validation on all DTOs

---

## 📖 API Documentation (Swagger)

When running locally, the interactive Swagger UI is available at:

```
http://localhost:8080/ride-mate/swagger-ui.html
```

OpenAPI JSON spec:

```
http://localhost:8080/ride-mate/api-docs
```

> ⚠️ Swagger UI is **disabled** in production profile for security.

---

## 🐳 Deployment

### Docker

#### Build and Run

```bash
# Build the JAR
mvn clean package -DskipTests

# Build Docker image
docker build -t ride-mate-backend .

# Run the container
docker run -d \
  --name ride-mate-backend \
  -p 8080:8080 \
  --env-file .env \
  -e SPRING_PROFILES_ACTIVE=prod \
  ride-mate-backend
```

#### Docker Compose

```bash
docker-compose up -d
```

This starts:
- **Jenkins** — CI/CD server on port `8082`
- **RideMate Backend** — Application on port `8080`

### CI/CD Pipeline (Jenkins)

The project includes a `Jenkinsfile` with the following stages:

1. **Clone Repository** — Pull latest code from GitHub
2. **Build JAR** — `mvn clean package -DskipTests`
3. **Build Docker Image** — Create container image
4. **Stop Old Container** — Gracefully stop existing deployment
5. **Run Container** — Deploy new version with environment variables

---

## 🔑 Environment Variables

For production deployment, set the following environment variables (or use a `.env` file):

| Variable                    | Description                          | Required |
|-----------------------------|--------------------------------------|----------|
| `DB_URL`                    | MySQL JDBC connection URL            | ✅        |
| `DB_USERNAME`               | Database username                    | ✅        |
| `DB_PASSWORD`               | Database password                    | ✅        |
| `MAIL_USERNAME`             | SMTP email address                   | ✅        |
| `MAIL_PASSWORD`             | SMTP email app password              | ✅        |
| `JWT_SECRET`                | JWT signing secret key               | ✅        |
| `SUPABASE_URL`              | Supabase project URL                 | ✅        |
| `SUPABASE_KEY`              | Supabase service key                 | ✅        |
| `PAYHERE_API_BASE_URL`      | PayHere API base URL                 | ✅        |
| `PAYHERE_API_CHARGE_PATH`   | PayHere charge endpoint path         | ✅        |
| `PAYHERE_MERCHANT_ID`       | PayHere merchant ID                  | ✅        |
| `PAYHERE_MERCHANT_SECRET`   | PayHere merchant secret              | ✅        |

### Example `.env` file

```env
DB_URL=jdbc:mysql://your-db-host:3306/ride_mate_db
DB_USERNAME=db_user
DB_PASSWORD=db_password
MAIL_USERNAME=info.ridemate@gmail.com
MAIL_PASSWORD=your_app_password
JWT_SECRET=YourProductionSecretKey
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_KEY=your_supabase_key
PAYHERE_API_BASE_URL=https://www.payhere.lk
PAYHERE_API_CHARGE_PATH=/merchant/v2/charge
PAYHERE_MERCHANT_ID=your_merchant_id
PAYHERE_MERCHANT_SECRET=your_merchant_secret
```

---

## 🤝 Contributing

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/your-feature`)
3. **Commit** your changes (`git commit -m 'Add your feature'`)
4. **Push** to the branch (`git push origin feature/your-feature`)
5. **Open** a Pull Request

### Development Guidelines

- Follow the coding conventions documented in [.github/copilot-instructions.md](.github/copilot-instructions.md)
- All entities must extend `BaseEntity` and include audit fields
- Use constructor injection (no `@Autowired` field injection)
- Use externalized messages from `notification.properties`
- Write meaningful log statements at entry, warning, and success points
- Add Liquibase migrations for any database schema changes

---

## 📄 License

This project is developed as part of the **SDGP (Software Development Group Project)** module at **IIT (Informatics Institute of Technology)**.

---

<p align="center">
  Made with ❤️ by the RideMate Team 
</p>
