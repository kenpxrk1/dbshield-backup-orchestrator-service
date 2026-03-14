package db.shield.backup.orchestrator.service.service;

import db.shield.backup.orchestrator.service.dto.request.CreateBackupRequest;
import db.shield.backup.orchestrator.service.dto.response.BackupJobResponse;

import java.util.List;
import java.util.UUID;

public interface BackupService {

    BackupJobResponse createBackup(CreateBackupRequest request);

    BackupJobResponse getBackup(UUID jobId);

    List<BackupJobResponse> getAllBackups();

    void cancelBackup(UUID jobId);

    void markStarted(UUID jobId, String workerId);

    void markCompleted(UUID jobId, String filePath, long fileSize);

    void markFailed(UUID jobId, String error, boolean retryable);

}
