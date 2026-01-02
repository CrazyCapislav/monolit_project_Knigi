package dev.petr.file.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.petr.file.application.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    public void registerSession(Long userId, WebSocketSession session) {
        log.info("Registering WebSocket session for user {}", userId);
        userSessions.put(userId, session);
    }

    public void removeSession(Long userId) {
        log.info("Removing WebSocket session for user {}", userId);
        userSessions.remove(userId);
    }

    public void notifyUploadProgress(Long userId, String fileName, int progress) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> message = Map.of(
                        "type", "UPLOAD_PROGRESS",
                        "fileName", fileName,
                        "progress", progress
                );
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
                log.debug("Sent upload progress to user {}: {}%", userId, progress);
            } catch (IOException e) {
                log.error("Error sending WebSocket message to user {}", userId, e);
                userSessions.remove(userId);
            }
        }
    }

    public void notifyUploadComplete(Long userId, FileUploadResponse response) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> message = Map.of(
                        "type", "UPLOAD_COMPLETE",
                        "file", response
                );
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
                log.info("Sent upload complete notification to user {}", userId);
            } catch (IOException e) {
                log.error("Error sending WebSocket message to user {}", userId, e);
                userSessions.remove(userId);
            }
        }
    }
}