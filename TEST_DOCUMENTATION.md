# Test Documentation - Feature Flag Manager

## Overview
This document outlines the testing approach, structure, and execution for the Feature Flag Manager application.

---

## Test Architecture

### Tech Stack
* **Framework:** JUnit 5
* **Spring Support:** `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`
* **Client:** `WebTestClient` (Non-blocking WebFlux test client)

### Test Isolation
State is reset before each test execution via `@BeforeEach`:

```kotlin
@BeforeEach
fun resetState() {
    webTestClient.post().uri("/api/flags").bodyValue(FeatureFlag("beta-checkout", false)).exchange()
    webTestClient.post().uri("/api/flags").bodyValue(FeatureFlag("dark-mode", false)).exchange()
    webTestClient.post().uri("/api/flags").bodyValue(FeatureFlag("compact-view", false)).exchange()
}