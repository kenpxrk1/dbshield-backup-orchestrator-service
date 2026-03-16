package db.shield.backup.orchestrator.service.dto.response;

import java.util.List;

public record BackupListResponse(

        List<BackupJobResponse> backups

) {
}
