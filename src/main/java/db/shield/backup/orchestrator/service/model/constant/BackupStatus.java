package db.shield.backup.orchestrator.service.model.constant;

public enum BackupStatus {

    PENDING,
    REQUESTED,
    STARTED,
    UPLOADING,
    COMPLETED,
    FAILED,
    CANCELLED

    ;

    public boolean canStart() {
        return this == REQUESTED;
    }

    public boolean canComplete() {
        return this == REQUESTED || this == STARTED || this == UPLOADING;
    }

    public boolean canFail() {
        return this == REQUESTED || this == STARTED || this == UPLOADING;
    }

    public boolean canCancel() {
        return this == PENDING || this == REQUESTED || this == STARTED || this == UPLOADING;
    }

    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}
