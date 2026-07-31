package com.example.flags.repository

import com.example.flags.model.FeatureFlag
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

@Repository
class FeatureFlagRepository {
    private val flags = ConcurrentHashMap<String, FeatureFlag>().apply {
        put("dark-mode", FeatureFlag("dark-mode", true))
        put("beta-checkout", FeatureFlag("beta-checkout", false))
    }

    fun findAll(): Flux<FeatureFlag> = Flux.fromIterable(flags.values)

    fun findByName(name: String): Mono<FeatureFlag> = Mono.justOrEmpty(flags[name])

    fun save(flag: FeatureFlag): Mono<FeatureFlag> {
        flags[flag.name] = flag
        return Mono.just(flag)
    }
}