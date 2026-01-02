package dev.petr.file.infrastructure.persistence.repository;

import dev.petr.file.infrastructure.persistence.entity.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaFileMetadataRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findByFileName(String fileName);

    Page<FileEntity> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId, Pageable pageable);

    List<FileEntity> findByEntityTypeAndEntityId(String entityType, Long entityId);
}