package com.techchallenge.bdd;

import com.techchallenge.messaging.publisher.OsEventPublisher;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.database-platform=com.techchallenge.config.H2TestDialect"
})
public class CucumberSpringConfiguration {

    @MockBean
    OsEventPublisher osEventPublisher;
}
