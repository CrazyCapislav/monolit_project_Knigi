package dev.petr.file.application.usecase;

import dev.petr.file.domain.event.FileEvent;
import dev.petr.file.domain.model.FileMetadata;
import dev.petr.file.domain.repository.FileMetadataRepository;
import dev.petr.file.infrastructure.messaging.producer.FileEventProducer;
import dev.petr.file.infrastructure.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteFileUseCase {

    private final FileStorageService storageService;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileEventProducer fileEventProducer;

    @Transactional
    public void execute(Long fileId, Long userId) {
        log.info("User {} deleting file {}", userId, fileId);

        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        if (!metadata.getUploaderId().equals(userId)) {
            throw new IllegalArgumentException("Only file uploader can delete the file");
        }

        storageService.delete(metadata.getFileName());
        fileMetadataRepository.deleteById(fileId);

        log.info("File {} deleted successfully", fileId);

        FileEvent event = FileEvent.builder()
                .fileId(metadata.getId())
                .fileName(metadata.getFileName())
                .uploaderId(metadata.getUploaderId())
                .entityType(metadata.getEntityType())
                .entityId(metadata.getEntityId())
                .eventType("FILE_DELETED")
                .build();
        fileEventProducer.sendFileDeleted(event);
    }
}