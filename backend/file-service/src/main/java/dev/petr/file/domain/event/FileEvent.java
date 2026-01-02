package dev.petr.file.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEvent {
    private Long fileId;
    private String fileName;
    private Long uploaderId;
    private String fileType;
    private Long fileSize;
    private String entityType;
    private Long entityId;
    private String eventType;
}