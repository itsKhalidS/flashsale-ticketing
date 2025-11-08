# Flash Sale Ticketing Service

A Spring Boot application designed to handle **flash-sale ticket bookings** with high concurrency, ensuring **no overselling**, **idempotent order creation**, and **consistent state under race conditions**.

---

## Overview

This service simulates a high-load flash sale environment where multiple users try to book limited event tickets at the same time.  
It guarantees correctness even under **retry storms**, **parallel requests**, and **simultaneous payment/cancel operations**.

The project demonstrates:
- **Optimistic locking** using `@Version` fields.
- **Transactional safety** with Spring Data JPA.
- **Idempotent order creation** using an `idempotencyKey`.
- **Auto-expiry of pending orders**.
- **Basic observability** via Actuator endpoints and custom metrics.

---

## Tech Stack

- **Java 17**
- **Spring Boot 3.5.7**
- **Spring Data JPA / Hibernate**
- **Actuator**
- **JUnit 5 / Mockito**
- **Maven**

---

## Architecture

**Layers:**
- **Controller:** Handles REST endpoints for events and orders.
- **Service:** Core business logic (seat updates, idempotency checks, order expiry).
- **Repository:** JPA repositories for persistence.
- **Scheduler:** Periodic task to auto-expire pending orders.
- **Config:** Logging, metrics, and Audit setup.

**Key Entities:**
- `Event` -> `(eventId, eventName, totalSeats, remainingSeats, version, startTime, endTime, status, createdBy, createdAt, updatedBy, updatedAt)`
- `Order` -> `(orderId, event, qty, idempotencyKey, expiresAt, status, createdBy, createdAt, updatedBy, updatedAt, version)`
- `Payment` -> `(paymentId, order, paymentReference, amount, status, createdBy, createdAt, updatedBy, updatedAt)`

---

## Concurrency Design

Concurrency is managed using **Optimistic Locking** (`@Version`) in both `Event` and `Order` entities.

When multiple threads attempt to modify the same event (e.g., decrease `remainingSeats`), Hibernate compares version numbers during updates:
- If two updates conflict, a `OptimisticLockException` is thrown.
- The service catches this and **retries** the transaction safely.
- This ensures **no overselling** and consistent seat counts even with 1000+ parallel requests.

When multiple threads attempt to modify the same order (e.g., change order status), Hibernate compares version numbers during updates:
- If two updates conflict, a `OptimisticLockException` is thrown.
- The service catches this and a suitable message is shown.
- This ensures Race conditions (orderStatus : `CONFIRMED` vs `CANCELLED` vs `EXPIRED`) result in consistent final state. 

---

## Features

| Feature | Description |
|----------|-------------|
| **Event Management** | Create and fetch events with seat availability |
| **Order Placement** | POST `/order/create` with `{ eventId, quantity, idempotencyKey }` |
| **Idempotency** | Re-sending same idempotencyKey returns same order |
| **Order Lifecycle** | `PENDING -> EXPIRED` , `PENDING -> CANCELLED` , `PENDING -> CONFIRMED` , `PENDING -> CONFIRMED -> CANCELLED` |
| **Auto Expiry** | Background scheduler updated expired orders **restores event seats** |
| **Observability** | `/actuator/health`, `/actuator/metrics/orders.created`, `/actuator/metrics/optimistic.lock.retries` |
| **Logging** | Each request tagged with `correlationId` |

---

## Testing

- **Unit Tests:**  
  Test for service methods (seat updates, idempotency, expiry).
- **Integration Tests:**  
  Test for complete order flow via REST endpoints.
- **Concurrency Test:**  
  Test which simulates 500+ parallel order requests to verify no overselling and consistent seat counts.
- **Tools Used:**  
  JUnit 5, Mockito, CountDownLatch, ExecutorService.

---

## Running the Project

### 1. Setup Database and update Configurations
Setup Database according to the entities provided above and update DB and other configurations present in /flashsale-ticketing/src/main/resources/application-dev.properties

### 2. Build
```bash
mvn clean install
```

### 3. Run
```bash
mvn spring-boot:run
```

App runs on:  
`http://localhost:8080`

### 4. API Examples

**Create Event**
```bash
POST /event/create
{
  "eventName": "Coldplay: Music of Spheres",
  "totalSeats": "50000",
  "startTime": "2025-11-08T12:00:00",
  "endTime": "2025-11-08T13:00:00"
}
```

**Place Order**
```bash
POST /order/create
{
  "eventId": "5",
  "quantity": "10",
  "idempotencyKey": "11dcfe35-c1f1-4671-afd2-e782ea7309d1"
}
```

**Confirm Order**
```bash
PUT /order/{ORDER_ID}/confirm
{ 
  "amount" : "5000",
  "paymentReference": "23dcfe35-c1f1-4671-afd2-e782ea7309d1"
}
```

---

## Design Trade-offs

- **Optimistic vs Pessimistic Locking:**  
  Chose optimistic to avoid blocking and maximize concurrency.
- **No explicit synchronization or database-level locks are used - this design remains scalable and lightweight**.