package dev.petr.notification.presentation.controller;

import dev.petr.notification.application.dto.NotificationResponse;
import dev.petr.notification.application.dto.UnreadCountResponse;
import dev.petr.notification.application.usecase.GetUserNotificationsUseCase;
import dev.petr.notification.application.usecase.MarkNotificationAsReadUseCase;
import dev.petr.notification.domain.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notifications", description = "Notification management")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final GetUserNotificationsUseCase getUserNotificationsUseCase;
    private final MarkNotificationAsReadUseCase markNotificationAsReadUseCase;
    private final NotificationRepository notificationRepository;

    @Operation(summary = "Get user notifications")
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<NotificationResponse> notifications = getUserNotificationsUseCase.execute(
                userId, 
                PageRequest.of(page, size)
        );
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread count")
    @GetMapping("/unread/count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId) {
        
        long count = notificationRepository.countUnreadByUserId(userId);
        return ResponseEntity.ok(new UnreadCountResponse(userId, count));
    }

    @Operation(summary = "Mark notification as read")
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        markNotificationAsReadUseCase.execute(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Mark all notifications as read")
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestHeader("X-User-Id") Long userId) {
        markNotificationAsReadUseCase.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}