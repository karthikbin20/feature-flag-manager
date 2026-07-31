package com.example.flags.service

import com.example.flags.model.FeatureFlag
import com.example.flags.repository.FeatureFlagRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class FeatureFlagService(private val repository: FeatureFlagRepository) {

    fun getAllFlags(): Flux<FeatureFlag> = repository.findAll()

    fun getFlagByName(name: String): Mono<FeatureFlag> = repository.findByName(name)

    fun createOrUpdateFlag(flag: FeatureFlag): Mono<FeatureFlag> = repository.save(flag)
}