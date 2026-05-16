package db.shield.backup.orchestrator.service.outbox;

public enum BackupEventOutboxStatus {
    PENDING,
    SENDING,
    RETRY,
    SENT,
    FAILED
}
