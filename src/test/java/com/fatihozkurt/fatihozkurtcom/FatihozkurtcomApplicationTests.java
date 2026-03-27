package com.fatihozkurt.fatihozkurtcom;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies that application context can start.
 */
@SpringBootTest
@ActiveProfiles("test")
class FatihozkurtcomApplicationTests {

	/**
	 * Ensures Spring context bootstrap succeeds.
	 */
	@Test
	void contextLoads() {
	}

}
