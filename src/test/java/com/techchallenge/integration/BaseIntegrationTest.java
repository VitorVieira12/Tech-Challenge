package com.techchallenge.integration;

import com.techchallenge.config.TestSecurityConfig;
import com.techchallenge.messaging.publisher.OsEventPublisher;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public abstract class BaseIntegrationTest {

    @MockBean
    protected OsEventPublisher osEventPublisher;
}
