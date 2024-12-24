# Streaming Service API

## Description

This is a Spring Boot-based API for a video streaming service that includes user registration and payment functionality. It allows users to register, validate their credit card, and process payments for their subscriptions.

The project follows a layered architecture with a controller and service layer. The Controller layer manages incoming HTTP requests and handles the interaction with the web. The Service layer contains the core business logic, such as validating user information or processing payments. 

## Requirements

- Java 11 or higher
- Maven
- Git

## Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Shozab-N18/StreamingServiceAPI.git
   cd StreamingServiceAPI
2. **Build the application:**
   ```bash
   mvn clean install
3. **Run the application:**
   ```bash
   mvn spring-boot:run

## API Endpoints

- **POST /users/register:** Register a new user (with username, password, email, etc.)
- **GET /users:** Retrieve users, optionally filtered by whether they have a credit card (hasCreditCard=yes/no).
- **POST /payments:** Process a payment for a user (requires a valid credit card and amount).

## Testing

Unit tests can be run using:
  ```bash
  mvn test
  ```
Generate a Jacoco report using:
  ```bash
  mvn jacoco:report
  ```
Navigate into Jacoco directory using: 
```bash
cd target/site/jacoco
```
Open up report on browser using: 
```bash
start index.html
```

