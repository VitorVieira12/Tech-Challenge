package com.techchallenge.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests.
 * Uses H2 in-memory database for CI/CD compatibility.
 * For local testing with PostgreSQL via Testcontainers, use a different profile.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
    // Using H2 database configured in application-test.yml
    // This ensures tests run smoothly in CI/CD environments without Docker
}



