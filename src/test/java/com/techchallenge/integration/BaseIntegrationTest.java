package com.techchallenge.integration;

import com.techchallenge.config.TestSecurityConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests.
 * Uses H2 in-memory database for CI/CD compatibility.
 * Security is disabled for testing purposes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public abstract class BaseIntegrationTest {
    // Using H2 database configured in application-test.yml
    // This ensures tests run smoothly in CI/CD environments without Docker
    // Security is disabled via TestSecurityConfig
}



