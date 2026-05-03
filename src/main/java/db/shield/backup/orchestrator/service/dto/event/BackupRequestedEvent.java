package db.shield.backup.orchestrator.service.dto.event;

import db.shield.backup.orchestrator.service.model.constant.DatabaseType;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BackupRequestedEvent(

        UUID jobId,
        UUID databaseId,
        DatabaseType dbType,
        OffsetDateTime requestedAt

) {
}
