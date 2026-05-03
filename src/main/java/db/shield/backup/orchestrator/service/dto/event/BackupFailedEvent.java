package db.shield.backup.orchestrator.service.dto.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BackupFailedEvent(

        UUID jobId,
        String error,
        boolean retryable,
        OffsetDateTime failedAt

) {
}
