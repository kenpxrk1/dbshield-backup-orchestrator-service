package db.shield.backup.orchestrator.service.model.constant;

public enum BackupStatus {

    PENDING,
    REQUESTED,
    STARTED,
    UPLOADING,
    COMPLETED,
    FAILED,
    CANCELLED
}
