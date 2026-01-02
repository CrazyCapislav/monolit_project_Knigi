package dev.petr.file.application.usecase;

import dev.petr.file.domain.model.FileMetadata;
import dev.petr.file.domain.repository.FileMetadataRepository;
import dev.petr.file.infrastructure.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadFileUseCase {

    private final FileStorageService storageService;
    private final FileMetadataRepository fileMetadataRepository;

    @Transactional(readOnly = true)
    public Resource execute(Long fileId) {
        log.info("Downloading file with ID {}", fileId);

        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        return storageService.load(metadata.getFileName());
    }

    @Transactional(readOnly = true)
    public FileMetadata getMetadata(Long fileId) {
        return fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
    }
}