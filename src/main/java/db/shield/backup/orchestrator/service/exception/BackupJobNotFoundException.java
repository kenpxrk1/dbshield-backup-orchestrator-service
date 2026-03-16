package db.shield.backup.orchestrator.service.exception;

import java.util.UUID;

public class BackupJobNotFoundException extends RuntimeException {

    public BackupJobNotFoundException(UUID jobId) {
        super("Backup job not found. jobId=" + jobId);
    }
}
