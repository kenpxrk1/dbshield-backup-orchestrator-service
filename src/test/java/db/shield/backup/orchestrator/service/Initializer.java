package db.shield.backup.orchestrator.service;

import db.shield.backup.orchestrator.service.dto.request.CreateBackupRequest;
import db.shield.backup.orchestrator.service.dto.response.BackupJobResponse;
import db.shield.backup.orchestrator.service.model.BackupJobEntity;
import db.shield.backup.orchestrator.service.model.BackupResultEntity;
import db.shield.backup.orchestrator.service.model.constant.BackupStatus;
import db.shield.backup.orchestrator.service.model.constant.DatabaseType;
import org.junit.jupiter.api.BeforeAll;

import java.time.Instant;
import java.util.UUID;

public abstract class Initializer {

    protected static UUID jobId;
    protected static UUID databaseId;
    protected static UUID resultId;
    protected static String workerId;
    protected static String filePath;
    protected static long fileSize;
    protected static Instant requestedAt;
    protected static Instant startedAt;
    protected static Instant completedAt;
    protected static String errorMessage;

    protected static CreateBackupRequest createBackupRequest;
    protected static BackupJobEntity backupJobEntity;
    protected static BackupJobResponse backupJobResponse;
    protected static BackupResultEntity backupResultEntity;

    @BeforeAll
    static void init() {
        jobId = UUID.randomUUID();
        databaseId = UUID.randomUUID();
        resultId = UUID.randomUUID();
        workerId = "worker-1";
        filePath = "/tmp/backups/dbshield/backup.sql";
        fileSize = 2048L;
        requestedAt = Instant.now();
        startedAt = Instant.now();
        completedAt = Instant.now();
        errorMessage = "network timeout";

        createBackupRequest = new CreateBackupRequest(databaseId);

        backupJobEntity = BackupJobEntity.builder()
                .databaseId(databaseId)
                .dbType(DatabaseType.POSTGRES)
                .status(BackupStatus.REQUESTED)
                .requestedAt(requestedAt)
                .retryCount(0)
                .build();
        backupJobEntity.setId(jobId);

        backupJobResponse = new BackupJobResponse(
                jobId,
                databaseId,
                DatabaseType.POSTGRES,
                BackupStatus.REQUESTED,
                requestedAt,
                startedAt,
                completedAt
        );

        backupResultEntity = BackupResultEntity.builder()
                .jobId(jobId)
                .filePath(filePath)
                .fileSize(fileSize)
                .checksum("checksum-123")
                .build();
        backupResultEntity.setId(resultId);
    }
}
