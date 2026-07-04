# Flash Sale Ticketing Service

A production-oriented Spring Boot backend for a flash-sale ticket booking platform inspired by applications such as **BookMyShow**. The application is designed to handle high-concurrency ticket bookings while ensuring correctness, scalability, and maintainability.

---

# Project Goal

The primary objective of this project is to demonstrate the design and implementation of a scalable, secure, and concurrency-safe ticket booking backend capable of handling flash-sale scenarios while following modern Spring Boot development practices.

---

# Features

## Authentication & Authorization

* JWT based authentication
* Role Based Access Control (RBAC)
* User and Admin roles
* Password encryption using BCrypt
* Secure REST APIs using Spring Security

---

## Event Management

* Create events
* Fetch event details
* Paginated event listing
* Search events by name
* Automatic event status management

Only **ACTIVE** and **INACTIVE** (upcoming) events are visible to end users. **CLOSED** and **CANCELLED** events are hidden from the public API.

---

## Order Management

* Place ticket orders
* Confirm orders (payment simulation)
* Cancel orders
* View paginated order history
* Idempotent order creation
* Automatic pending order expiry

---

## Background Scheduling

Two schedulers continuously maintain system consistency.

### Event Scheduler

Automatically updates event status based on event timings.

```
INACTIVE  →  ACTIVE  →  CLOSED
```

This ensures the application always relies on `EventStatus` instead of performing time-based checks during every request.

### Order Expiry Scheduler

Automatically expires pending orders after the configured timeout and restores reserved seats back to the associated event.

---

## Search & Pagination

### Event Search

Implemented using **Spring Data JPA Specifications**.

Current filters:

* Event Name
* Event Status (ACTIVE / INACTIVE)

Pagination supports:

* Page Number

### Order Pagination

Users can retrieve their booking history using pagination.

---

## Concurrency & Consistency

The application guarantees that seats are never oversold even under heavy concurrent traffic.

Concurrency is handled using **Optimistic Locking** (`@Version`) on both `Event` and `Order` entities.

When concurrent updates occur:

* Hibernate detects version conflicts.
* Conflicting transactions fail safely.
* Retry logic is applied where appropriate.
* Seat counts remain consistent.

This also ensures race conditions between:

* Confirm Order
* Cancel Order
* Expire Order

always produce a consistent final state.

---

# Architecture

The project follows a layered architecture.

```
Controller
      │
      ▼
Service
      │
      ▼
Repository
      │
      ▼
Database
```

Supporting layers include:

* DTOs
* Specifications
* Validators
* Schedulers
* Security
* Exception Handling
* Metrics
* Logging

---

# Technologies Used

* Java 17
* Spring Boot
* Spring Security
* JWT Authentication
* Spring Data JPA
* Hibernate
* MySQL
* Maven
* JUnit 5
* Mockito
* Actuator

---

# Core Design Decisions

## Optimistic Locking

Chosen over pessimistic locking to maximize throughput during flash-sale scenarios while preventing overselling.

---

## DTO-Based API

Entities are never exposed directly.

Dedicated request and response DTOs are used throughout the application to keep persistence and API layers independent.

---

## Specification Pattern

Dynamic event searching is implemented using Spring Data JPA Specifications.

This approach allows new filters to be added without creating numerous repository methods.

---

## Custom Pagination Response

Instead of exposing Spring's `Page` object directly, the application returns a custom `PageResponse<T>`.

Benefits:

* Cleaner API contract
* Framework-independent responses
* Easier customization
* Consistent response format across endpoints

---

# Entity Overview

## Event

* Event Id
* Event Name
* Event Description
* Total Seats
* Remaining Seats
* Start Time
* End Time
* Event Status
* Event Image URL
* Version
* Audit Fields

## Order

* Order Id
* Event
* User
* Quantity
* Status
* Idempotency Key
* Expiry Time
* Ticket Number
* Version
* Audit Fields

## Payment

* Payment Id
* Order
* Amount
* Payment Reference
* Payment Status
* Audit Fields

## User

* User Id
* Name
* Email
* Password
* Role
* Audit Fields

---

# API Highlights

## Event APIs

* Create Event
* Get Event By Id
* Get Events (Pagination + Search)

## Order APIs

* Create Order
* Confirm Order
* Cancel Order
* Get User Orders (Pagination)

## Authentication APIs

* Register User
* Login

---

# Observability

* Spring Boot Actuator
* Health Endpoint
* Custom Metrics

  * Orders Created
  * Optimistic Lock Retries
* Correlation ID Logging

---

# Validation

The application performs validation at multiple levels.

Examples include:

* Event timing validation
* Seat availability validation
* Order state validation
* Payment validation
* Request DTO validation

---

# Testing

The project contains:

* Unit Tests
* Integration Tests
* Concurrency Tests

Concurrency testing simulates hundreds of parallel booking requests to verify:

* No overselling
* Correct optimistic locking behavior
* Consistent seat counts

---

# Running the Application

## Clone the repository

```bash
git clone <repository-url>
```

## Configure the database

Update the datasource configuration in:

```
src/main/resources/application-dev.properties
```

## Build

```bash
mvn clean install
```

## Run

```bash
mvn spring-boot:run
```

The application starts on:

```
http://localhost:8080
```

---

# Future Enhancements

* Seat-level booking
* Event categories
* Venue management
* Event images
* Ticket QR codes
* Email notifications
* Payment gateway integration
* Redis caching
* Docker & Docker Compose
* CI/CD pipeline
* API documentation using OpenAPI/Swagger

---