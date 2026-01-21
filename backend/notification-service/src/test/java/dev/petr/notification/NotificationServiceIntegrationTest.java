package dev.petr.notification;

import dev.petr.notification.application.dto.NotificationCreateRequest;
import dev.petr.notification.application.dto.NotificationResponse;
import dev.petr.notification.application.usecase.CreateNotificationUseCase;
import dev.petr.notification.application.usecase.GetUserNotificationsUseCase;
import dev.petr.notification.application.usecase.MarkNotificationAsReadUseCase;
import dev.petr.notification.domain.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SuppressWarnings("resource")
@Testcontainers(disabledWithoutDocker = true)
public class NotificationServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:29092");
    }

    @Autowired
    private CreateNotificationUseCase createNotificationUseCase;

    @Autowired
    private GetUserNotificationsUseCase getUserNotificationsUseCase;

    @Autowired
    private MarkNotificationAsReadUseCase markNotificationAsReadUseCase;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void shouldCreateAndRetrieveNotification() {
        NotificationCreateRequest request = new NotificationCreateRequest(
                1L,
                "EXCHANGE_REQUESTED",
                "Test Notification",
                "Test message",
                Map.of("key", "value")
        );

        NotificationResponse created = createNotificationUseCase.execute(request);

        assertThat(created).isNotNull();
        assertThat(created.userId()).isEqualTo(1L);
        assertThat(created.title()).isEqualTo("Test Notification");
        assertThat(created.read()).isFalse();
    }

    @Test
    void shouldGetUserNotifications() {
        createNotificationUseCase.execute(new NotificationCreateRequest(
                2L, "EXCHANGE_ACCEPTED", "Title 1", "Message 1", Map.of()
        ));
        createNotificationUseCase.execute(new NotificationCreateRequest(
                2L, "EXCHANGE_REJECTED", "Title 2", "Message 2", Map.of()
        ));

        Page<NotificationResponse> notifications = getUserNotificationsUseCase.execute(
                2L, PageRequest.of(0, 10)
        );

        assertThat(notifications.getContent()).hasSize(2);
        assertThat(notifications.getContent().getFirst().userId()).isEqualTo(2L);
    }

    @Test
    void shouldMarkNotificationAsRead() {
        NotificationResponse created = createNotificationUseCase.execute(new NotificationCreateRequest(
                3L, "PUBLICATION_APPROVED", "Test", "Test", Map.of()
        ));

        markNotificationAsReadUseCase.execute(created.id());

        Page<NotificationResponse> notifications = getUserNotificationsUseCase.execute(
                3L, PageRequest.of(0, 10)
        );
        assertThat(notifications.getContent().getFirst().read()).isTrue();
    }

    @Test
    void shouldCountUnreadNotifications() {
        createNotificationUseCase.execute(new NotificationCreateRequest(
                4L, "FILE_UPLOADED", "File 1", "Uploaded", Map.of()
        ));
        createNotificationUseCase.execute(new NotificationCreateRequest(
                4L, "FILE_UPLOADED", "File 2", "Uploaded", Map.of()
        ));

        long count = notificationRepository.countUnreadByUserId(4L);

        assertThat(count).isEqualTo(2);
    }
}