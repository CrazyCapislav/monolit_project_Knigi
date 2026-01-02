package dev.petr.file.domain.repository;

import dev.petr.file.domain.model.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository {
    FileMetadata save(FileMetadata file);
    Optional<FileMetadata> findById(Long id);
    Optional<FileMetadata> findByFileName(String fileName);
    Page<FileMetadata> findByUploaderId(Long uploaderId, Pageable pageable);
    List<FileMetadata> findByEntity(String entityType, Long entityId);
    void deleteById(Long id);
}