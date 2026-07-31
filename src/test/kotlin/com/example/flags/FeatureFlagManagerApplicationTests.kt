package com.example.flags

import com.example.flags.model.FeatureFlag
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FeatureFlagManagerApplicationTests {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun resetState() {
        webTestClient.post().uri("/api/flags").bodyValue(FeatureFlag("beta-checkout", false)).exchange()
        webTestClient.post().uri("/api/flags").bodyValue(FeatureFlag("dark-mode", false)).exchange()
        webTestClient.post().uri("/api/flags").bodyValue(FeatureFlag("compact-view", false)).exchange()
    }

    @Test
    fun `should fetch all initial feature flags`() {
        webTestClient.get().uri("/api/flags")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[?(@.name == 'dark-mode')].enabled").isEqualTo(false)
            .jsonPath("$[?(@.name == 'compact-view')].enabled").isEqualTo(false)
    }

    @Test
    fun `should update existing feature flag`() {
        val updatedFlag = FeatureFlag("dark-mode", true)

        webTestClient.post().uri("/api/flags")
            .bodyValue(updatedFlag)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("dark-mode")
            .jsonPath("$.enabled").isEqualTo(true)
    }

    @Test
    fun `should delete feature flag successfully`() {
        webTestClient.delete().uri("/api/flags/dark-mode")
            .exchange()
            .expectStatus().isNoContent

        webTestClient.get().uri("/api/flags")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[?(@.name == 'dark-mode')]").doesNotExist()
    }

    @Test
    fun `should return 404 when deleting non-existent flag`() {
        webTestClient.delete().uri("/api/flags/non-existent-flag")
            .exchange()
            .expectStatus().isNotFound
    }
}