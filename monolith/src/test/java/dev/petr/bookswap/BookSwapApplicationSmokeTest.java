package dev.petr.bookswap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import dev.petr.bookswap.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class BookSwapApplicationSmokeTest {

    @Test 
    void contextLoads() {
    }
}
