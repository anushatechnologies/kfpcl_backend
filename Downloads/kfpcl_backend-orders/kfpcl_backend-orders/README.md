# KFPCL_BACKEND

Spring Boot backend application for KFPCL.

## Tech Stack
- Java 17
- Spring Boot 3.3.2
- Spring Data JPA
- MySQL
- Lombok
- Maven

## Project Structure
```
src/main/java/com/kfpcl/
├── KfpclBackendApplication.java    → Main Application
├── entity/                         → JPA Entity classes
├── repository/                     → Spring Data JPA Repositories
├── service/                        → Service Interfaces
│   └── impl/                       → Service Implementations
├── dto/                            → Data Transfer Objects
├── controller/                     → REST Controllers
└── exception/                      → Custom Exception Handlers
```

## Setup & Run

1. **Configure Database** – Update `src/main/resources/application.properties` with your MySQL credentials.

2. **Build**
   ```bash
   mvn clean install
   ```

3. **Run**
   ```bash
   mvn spring-boot:run
   ```

4. The application starts at `http://localhost:8080`
