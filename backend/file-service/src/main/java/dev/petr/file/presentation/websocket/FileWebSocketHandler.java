package dev.petr.file.presentation.websocket;

import dev.petr.file.infrastructure.websocket.FileUploadWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileWebSocketHandler extends TextWebSocketHandler {

    private final FileUploadWebSocketHandler uploadHandler;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = extractUserId(session);
        if (userId != null) {
            uploadHandler.registerSession(userId, session);
            log.info("WebSocket connection established for user {}", userId);
        } else {
            log.warn("WebSocket connection without userId");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.debug("Received WebSocket message: {}", message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = extractUserId(session);
        if (userId != null) {
            uploadHandler.removeSession(userId);
            log.info("WebSocket connection closed for user {}", userId);
        }
    }

    private Long extractUserId(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri != null) {
                String query = uri.getQuery();
                if (query != null && query.contains("userId=")) {
                    String userIdStr = query.split("userId=")[1].split("&")[0];
                    return Long.parseLong(userIdStr);
                }
            }
        } catch (Exception e) {
            log.error("Error extracting userId from WebSocket session", e);
        }
        return null;
    }
}