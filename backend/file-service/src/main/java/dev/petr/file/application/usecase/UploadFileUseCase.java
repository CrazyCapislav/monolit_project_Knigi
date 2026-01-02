package dev.petr.file.application.usecase;

import dev.petr.file.application.dto.FileUploadResponse;
import dev.petr.file.domain.event.FileEvent;
import dev.petr.file.domain.model.FileMetadata;
import dev.petr.file.domain.repository.FileMetadataRepository;
import dev.petr.file.infrastructure.messaging.producer.FileEventProducer;
import dev.petr.file.infrastructure.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadFileUseCase {

    private final FileStorageService storageService;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileEventProducer fileEventProducer;

    @Transactional
    public FileUploadResponse execute(
            MultipartFile file,
            Long uploaderId,
            String entityType,
            Long entityId) {

        log.info("User {} uploading file: {}", uploaderId, file.getOriginalFilename());

        String storedFileName = storageService.store(file);

        FileMetadata metadata = FileMetadata.builder()
                .fileName(storedFileName)
                .originalName(file.getOriginalFilename())
                .filePath(storageService.getFilePath(storedFileName))
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploaderId(uploaderId)
                .entityType(entityType)
                .entityId(entityId)
                .createdAt(OffsetDateTime.now())
                .build();

        FileMetadata saved = fileMetadataRepository.save(metadata);
        log.info("File {} uploaded successfully with ID {}", storedFileName, saved.getId());

        FileEvent event = FileEvent.builder()
                .fileId(saved.getId())
                .fileName(saved.getFileName())
                .uploaderId(saved.getUploaderId())
                .fileType(saved.getFileType())
                .fileSize(saved.getFileSize())
                .entityType(saved.getEntityType())
                .entityId(saved.getEntityId())
                .eventType("FILE_UPLOADED")
                .build();
        fileEventProducer.sendFileUploaded(event);

        return new FileUploadResponse(
                saved.getId(),
                saved.getFileName(),
                saved.getOriginalName(),
                saved.getFileType(),
                saved.getFileSize(),
                "/api/v1/files/" + saved.getId() + "/download",
                saved.getCreatedAt()
        );
    }
}