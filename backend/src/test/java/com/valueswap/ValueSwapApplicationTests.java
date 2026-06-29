package com.valueswap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ValueSwapApplicationTests {

	@Autowired
	private Environment environment;

	@Test
	void contextLoadsWithMatchingDefaults() {
		assertThat(environment.getProperty("valueswap.matching.threshold", Integer.class)).isEqualTo(60);
		assertThat(environment.getProperty("valueswap.matching.interval-ms", Long.class)).isEqualTo(300000L);
		assertThat(environment.getProperty("valueswap.matching.scheduler-enabled", Boolean.class)).isFalse();
	}

}
