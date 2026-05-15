package com.techchallenge;

import com.techchallenge.messaging.publisher.OsEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TechChallengeApplicationTests {

	@MockBean
	OsEventPublisher osEventPublisher;

	@Test
	void contextLoads() {
		// Test that the application context loads successfully
	}

}
