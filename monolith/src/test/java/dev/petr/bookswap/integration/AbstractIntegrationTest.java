package dev.petr.bookswap.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import dev.petr.bookswap.TestcontainersConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Base class for integration tests.
 * 
 * Runs against a disposable PostgreSQL container (see TestcontainersConfiguration).
 * 
 * No manually started database is required.
 * 
 * Database is automatically cleaned before and after each test.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    
    @BeforeEach
    void cleanDatabaseBefore() {
        cleanDatabase();
    }
    
    @AfterEach
    void cleanDatabaseAfter() {
        cleanDatabase();
    }
    
    /**
     * Cleans database by truncating all tables.
     * Uses TransactionTemplate for explicit transaction management.
     */
    private void cleanDatabase() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(@SuppressWarnings("null") TransactionStatus status) {
                entityManager.createNativeQuery("TRUNCATE TABLE book_rating, exchange_request, publication_request, book_genre, book, genre, users RESTART IDENTITY CASCADE").executeUpdate();
                entityManager.clear();
            }
        });
    }
}

