package dev.petr.file.infrastructure.persistence.repository;

import dev.petr.file.domain.model.FileMetadata;
import dev.petr.file.domain.repository.FileMetadataRepository;
import dev.petr.file.infrastructure.persistence.entity.FileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FileMetadataRepositoryImpl implements FileMetadataRepository {

    private final JpaFileMetadataRepository jpaRepository;

    @Override
    public FileMetadata save(FileMetadata file) {
        FileEntity entity = toEntity(file);
        FileEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<FileMetadata> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<FileMetadata> findByFileName(String fileName) {
        return jpaRepository.findByFileName(fileName).map(this::toDomain);
    }

    @Override
    public Page<FileMetadata> findByUploaderId(Long uploaderId, Pageable pageable) {
        return jpaRepository.findByUploaderIdOrderByCreatedAtDesc(uploaderId, pageable)
                .map(this::toDomain);
    }

    @Override
    public List<FileMetadata> findByEntity(String entityType, Long entityId) {
        return jpaRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private FileEntity toEntity(FileMetadata domain) {
        return FileEntity.builder()
                .id(domain.getId())
                .fileName(domain.getFileName())
                .originalName(domain.getOriginalName())
                .filePath(domain.getFilePath())
                .fileType(domain.getFileType())
                .fileSize(domain.getFileSize())
                .uploaderId(domain.getUploaderId())
                .entityType(domain.getEntityType())
                .entityId(domain.getEntityId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private FileMetadata toDomain(FileEntity entity) {
        return FileMetadata.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .originalName(entity.getOriginalName())
                .filePath(entity.getFilePath())
                .fileType(entity.getFileType())
                .fileSize(entity.getFileSize())
                .uploaderId(entity.getUploaderId())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}