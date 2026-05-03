package db.shield.backup.orchestrator.service.dto.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BackupStartedEvent(

        UUID jobId,
        String workerId,
        OffsetDateTime startedAt

) {
}
