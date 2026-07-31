package com.example.flags

import com.example.flags.model.FeatureFlag
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap

@SpringBootApplication
class FeatureFlagManagerApplication

fun main(args: Array<String>) {
    runApplication<FeatureFlagManagerApplication>(*args)
}

@Configuration
class RouterConfig {

    private val flags = ConcurrentHashMap<String, Boolean>().apply {
        put("beta-checkout", false)
        put("dark-mode", false)
        put("compact-view", false)
    }

    private val flagSink = Sinks.many().multicast().onBackpressureBuffer<FeatureFlag>()

    @Bean
    fun routes(): RouterFunction<ServerResponse> = router {
        GET("/api/flags") {
            val list = flags.map { FeatureFlag(it.key, it.value) }
            ServerResponse.ok().body(Flux.fromIterable(list))
        }
        POST("/api/flags") { request ->
            request.bodyToMono(FeatureFlag::class.java).flatMap { flag ->
                flags[flag.name] = flag.enabled
                flagSink.tryEmitNext(flag)
                ServerResponse.ok().body(Mono.just(flag))
            }
        }
        DELETE("/api/flags/{name}") { request ->
            val name = request.pathVariable("name")
            if (flags.containsKey(name)) {
                flags.remove(name)
                // We send a dummy false state via SSE to trigger UI updates for live effects
                flagSink.tryEmitNext(FeatureFlag(name, false))
                ServerResponse.noContent().build()
            } else {
                ServerResponse.notFound().build()
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