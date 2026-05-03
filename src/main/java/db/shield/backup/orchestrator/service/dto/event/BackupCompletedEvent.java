package db.shield.backup.orchestrator.service.dto.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BackupCompletedEvent(

        UUID jobId,
        String filePath,
        long fileSize,
        long durationMs,
        OffsetDateTime completedAt

) {
}
