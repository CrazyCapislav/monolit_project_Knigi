package dev.petr.file.application.usecase;

import dev.petr.file.application.dto.FileMetadataResponse;
import dev.petr.file.domain.model.FileMetadata;
import dev.petr.file.domain.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserFilesUseCase {

    private final FileMetadataRepository fileMetadataRepository;

    @Transactional(readOnly = true)
    public Page<FileMetadataResponse> execute(Long userId, Pageable pageable) {
        return fileMetadataRepository.findByUploaderId(userId, pageable)
                .map(this::toResponse);
    }

    private FileMetadataResponse toResponse(FileMetadata metadata) {
        return new FileMetadataResponse(
                metadata.getId(),
                metadata.getFileName(),
                metadata.getOriginalName(),
                metadata.getFileType(),
                metadata.getFileSize(),
                metadata.getUploaderId(),
                metadata.getEntityType(),
                metadata.getEntityId(),
                "/api/v1/files/" + metadata.getId() + "/download",
                metadata.getCreatedAt(),
                metadata.getUpdatedAt()
        );
    }
}