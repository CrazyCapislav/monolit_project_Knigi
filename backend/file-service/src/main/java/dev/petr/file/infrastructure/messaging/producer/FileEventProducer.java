package dev.petr.file.infrastructure.messaging.producer;

import dev.petr.file.domain.event.FileEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileEventProducer {

    private final KafkaTemplate<String, FileEvent> kafkaTemplate;

    public void sendFileUploaded(FileEvent event) {
        event.setEventType("FILE_UPLOADED");
        log.info("Sending FILE_UPLOADED event: {}", event);
        kafkaTemplate.send("file-events", event.getFileId().toString(), event);
    }

    public void sendFileDeleted(FileEvent event) {
        event.setEventType("FILE_DELETED");
        log.info("Sending FILE_DELETED event: {}", event);
        kafkaTemplate.send("file-events", event.getFileId().toString(), event);
    }
}