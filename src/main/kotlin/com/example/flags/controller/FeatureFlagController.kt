package com.example.flags.controller

import com.example.flags.model.FeatureFlag
import com.example.flags.service.FeatureFlagService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/flags")
class FeatureFlagController(private val service: FeatureFlagService) {

    @GetMapping
    fun getAllFlags(): Flux<FeatureFlag> = service.getAllFlags()

    @GetMapping("/{name}")
    fun getFlagByName(@PathVariable name: String): Mono<ResponseEntity<FeatureFlag>> =
        service.getFlagByName(name)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createFlag(@RequestBody flag: FeatureFlag): Mono<FeatureFlag> =
        service.createOrUpdateFlag(flag)
}