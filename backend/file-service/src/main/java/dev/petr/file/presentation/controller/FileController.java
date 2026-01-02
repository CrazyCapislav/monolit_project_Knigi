package dev.petr.file.presentation.controller;

import dev.petr.file.application.dto.FileMetadataResponse;
import dev.petr.file.application.dto.FileUploadResponse;
import dev.petr.file.application.usecase.DeleteFileUseCase;
import dev.petr.file.application.usecase.DownloadFileUseCase;
import dev.petr.file.application.usecase.GetUserFilesUseCase;
import dev.petr.file.application.usecase.UploadFileUseCase;
import dev.petr.file.domain.model.FileMetadata;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Files", description = "File storage and management")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final UploadFileUseCase uploadFileUseCase;
    private final DownloadFileUseCase downloadFileUseCase;
    private final DeleteFileUseCase deleteFileUseCase;
    private final GetUserFilesUseCase getUserFilesUseCase;

    @Operation(summary = "Upload file")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "entityType", required = false) String entityType,
            @RequestParam(value = "entityId", required = false) Long entityId) {

        FileUploadResponse response = uploadFileUseCase.execute(file, userId, entityType, entityId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Download file")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Resource resource = downloadFileUseCase.execute(id);
        FileMetadata metadata = downloadFileUseCase.getMetadata(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getOriginalName() + "\"")
                .body(resource);
    }

    @Operation(summary = "Delete file")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        deleteFileUseCase.execute(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get user files")
    @GetMapping("/my")
    public ResponseEntity<Page<FileMetadataResponse>> getUserFiles(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<FileMetadataResponse> files = getUserFilesUseCase.execute(
                userId,
                PageRequest.of(page, size)
        );
        return ResponseEntity.ok(files);
    }
}