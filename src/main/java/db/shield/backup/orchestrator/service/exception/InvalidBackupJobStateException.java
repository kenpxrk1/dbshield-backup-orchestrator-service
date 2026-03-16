package db.shield.backup.orchestrator.service.exception;

import db.shield.backup.orchestrator.service.model.constant.BackupStatus;

import java.util.UUID;

public class InvalidBackupJobStateException extends RuntimeException {

    public InvalidBackupJobStateException(UUID jobId, BackupStatus currentStatus, BackupStatus targetStatus) {
        super("Invalid backup job state transition. jobId=" + jobId
                + ", currentStatus=" + currentStatus
                + ", targetStatus=" + targetStatus);
    }

    public InvalidBackupJobStateException(UUID jobId, BackupStatus currentStatus, String action) {
        super("Invalid backup job state for action. jobId=" + jobId
                + ", currentStatus=" + currentStatus
                + ", action=" + action);
    }
}
