package db.shield.backup.orchestrator.service.dto.response;

import db.shield.backup.orchestrator.service.model.constant.BackupStatus;
import db.shield.backup.orchestrator.service.model.constant.DatabaseType;

import java.time.Instant;
import java.util.UUID;

public record BackupJobResponse(

        UUID id,
        UUID databaseId,
        DatabaseType dbType,
        BackupStatus status,
        Instant requestedAt,
        Instant startedAt,
        Instant completedAt

) {
}
