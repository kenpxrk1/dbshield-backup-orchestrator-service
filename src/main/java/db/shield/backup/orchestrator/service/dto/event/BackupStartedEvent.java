package db.shield.backup.orchestrator.service.dto.event;

import java.time.Instant;
import java.util.UUID;

public record BackupStartedEvent(

        UUID jobId,
        String workerId,
        Instant startedAt

) {
}
