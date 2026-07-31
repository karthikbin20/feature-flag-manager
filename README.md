# Feature Flag Manager

A real-time reactive Feature Flag management service built with Kotlin, Spring WebFlux, and Server-Sent Events (SSE).

---

## 🚀 Features
* **Reactive Non-Blocking API:** Full CRUD operations built on Spring WebFlux (`Mono`/`Flux`).
* **Real-Time Streaming:** Instant client updates via Server-Sent Events (`/api/flags/stream`).
* **Interactive UI:** Single-page application with dynamic theme toggles and live feature previews.
* **Thread-Safe Storage:** Low-latency in-memory persistence using `ConcurrentHashMap`.

---

## 🛠 Tech Stack
* **Language:** Kotlin
* **Framework:** Spring Boot 3 / Spring WebFlux
* **Streaming:** Server-Sent Events (SSE) via Project Reactor `Sinks`
* **Testing:** JUnit 5 + `WebTestClient`

---

## 📋 Prerequisites
* Java 17+
* Maven wrapper (`./mvnw` included)

---

## 🚀 Getting Started

### 1. Run the Application
Execute using Maven:
```bash
./mvnw spring-boot:run
```
Or run via Windows batch script:
```cmd
run.bat
```

### 2. Access the Dashboard
Open your browser and navigate to:
```
http://localhost:8080
```

---

## 🧪 Running Tests

Execute the non-blocking integration test suite:
```bash
./mvnw clean test
```

---

## 📡 API Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/flags` | Fetch all feature flags |
| `POST` | `/api/flags` | Create or update a feature flag |
| `DELETE` | `/api/flags/{name}` | Delete a feature flag by name |
| `GET` | `/api/flags/stream` | SSE endpoint for real-time live updates |