package db.shield.backup.orchestrator.service.dto.request;

import java.util.UUID;

public record CreateBackupRequest(

        UUID databaseId

) {}
