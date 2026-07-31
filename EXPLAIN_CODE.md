# Code Architecture & Explanation - Feature Flag Manager

## Overview
This document explains the core codebase for the **Feature Flag Manager**, a reactive, non-blocking application built with Kotlin, Spring WebFlux, and Server-Sent Events (SSE).

---

## 1. Data Model (`FeatureFlag.kt`)

```kotlin
data class FeatureFlag(
    val name: String,
    val enabled: Boolean
)
```
* **Purpose:** Represents the state of a single feature flag.
* **Immutability:** Uses Kotlin's standard `data class` for auto-generated `equals()`, `hashCode()`, and `toString()` functions.

---

## 2. In-Memory Repository (`FeatureFlagRepository.kt`)

```kotlin
@Repository
class FeatureFlagRepository {
    private val flags = ConcurrentHashMap<String, FeatureFlag>().apply {
        put("dark-mode", FeatureFlag("dark-mode", true))
        put("compact-view", FeatureFlag("compact-view", false))
        put("beta-checkout", FeatureFlag("beta-checkout", false))
    }

    fun findAll(): Flux<FeatureFlag> = Flux.fromIterable(flags.values)

    fun findByName(name: String): Mono<FeatureFlag> = Mono.justOrNull(flags[name])

    fun save(flag: FeatureFlag): Mono<FeatureFlag> {
        flags[flag.name] = flag
        return Mono.just(flag)
    }

    fun deleteByName(name: String): Mono<Boolean> {
        return Mono.justOrNull(flags.remove(name) != null)
    }
}
```
* **Thread Safety:** Uses `ConcurrentHashMap` to ensure safe concurrent reads and writes across non-blocking reactive threads.
* **Reactive Wrappers:** Wraps operations in `Mono` (0..1 items) and `Flux` (0..N items) to maintain non-blocking semantics.

---

## 3. Reactive Routing & SSE Stream (`RouterConfig.kt`)

```kotlin
@Configuration
class RouterConfig(private val repository: FeatureFlagRepository) {

    // Multicast sink broadcasts new updates to all connected subscribers
    private val flagSink = Sinks.many().multicast().onBackpressureBuffer<FeatureFlag>()

    @Bean
    fun routes(): RouterFunction<ServerResponse> = router {
        GET("/api/flags") {
            ServerResponse.ok().body(repository.findAll(), FeatureFlag::class.java)
        }

        POST("/api/flags") { request ->
            request.bodyToMono(FeatureFlag::class.java)
                .flatMap { flag -> repository.save(flag) }
                .doOnNext { flag -> flagSink.tryEmitNext(flag) }
                .flatMap { flag -> ServerResponse.ok().bodyValue(flag) }
        }

        DELETE("/api/flags/{name}") { request ->
            val name = request.pathVariable("name")
            repository.deleteByName(name).flatMap { deleted ->
                if (deleted) {
                    flagSink.tryEmitNext(FeatureFlag(name, false))
                    ServerResponse.noContent().build()
                } else {
                    ServerResponse.notFound().build()
                }
            }
        }

        GET("/api/flags/stream") {
            val stream = flagSink.asFlux().map { flag ->
                ServerSentEvent.builder(flag).build()
            }
            ServerResponse.ok().sse().body(stream)
        }
    }
}
```
* **Functional Routing:** Replaces standard `@RestController` with WebFlux `RouterFunction` for explicit route definitions.
* **`Sinks.many().multicast()`:** Acts as a reactive event broker. When a flag is modified or deleted via `POST` or `DELETE`, `tryEmitNext()` broadcasts the update.
* **SSE Streaming (`/api/flags/stream`):** Converts the sink output into `ServerSentEvent` objects, allowing browsers to receive live push notifications.

---

## 4. Frontend Event Subscriber (`index.html`)

```javascript
// Connect to the SSE stream endpoint
const eventSource = new EventSource('/api/flags/stream');

eventSource.onmessage = (event) => {
    const flag = JSON.parse(event.data);
    applyFlagEffect(flag.name, flag.enabled);
};

function applyFlagEffect(name, enabled) {
    if (name === 'dark-mode') {
        document.body.classList.toggle('dark-theme', enabled);
    }
    if (name === 'compact-view') {
        document.body.classList.toggle('compact-mode', enabled);
    }
    if (name === 'beta-checkout') {
        document.getElementById('betaFeature').style.display = enabled ? 'block' : 'none';
    }
}
```
* **Native EventSource:** Uses the browser's HTML5 `EventSource` API to maintain an open HTTP connection.
* **DOM Updates:** Listens for streaming events and applies immediate CSS changes without reloading the page.