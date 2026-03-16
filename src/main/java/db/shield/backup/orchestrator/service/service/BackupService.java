package db.shield.backup.orchestrator.service.service;

import db.shield.backup.orchestrator.service.dto.request.CreateBackupRequest;
import db.shield.backup.orchestrator.service.dto.response.BackupJobResponse;

import java.util.List;
import java.util.UUID;

public interface BackupService {

    /**
     * Creates a new backup job and publishes a request event to workers.
     *
     * @param request request payload containing the database identifier
     * @return created backup job response
     */
    BackupJobResponse createBackup(CreateBackupRequest request);

    /**
     * Retrieves a single backup job by its identifier.
     *
     * @param jobId backup job identifier
     * @return backup job response
     */
    BackupJobResponse getBackup(UUID jobId);

    /**
     * Retrieves all backup jobs.
     *
     * @return list of backup job responses
     */
    List<BackupJobResponse> getAllBackups();

    /**
     * Cancels a backup job if it is still in a cancellable state.
     *
     * @param jobId backup job identifier
     */
    void cancelBackup(UUID jobId);

    /**
     * Marks a backup job as started by a worker. Duplicate events are ignored.
     *
     * @param jobId    backup job identifier
     * @param workerId worker identifier
     */
    void markStarted(UUID jobId, String workerId);

    /**
     * Marks a backup job as completed and stores the produced backup result.
     *
     * @param jobId    backup job identifier
     * @param filePath output path
     * @param fileSize size in bytes
     */
    void markCompleted(UUID jobId, String filePath, long fileSize);

    /**
     * Marks a backup job as failed or schedules a retry if allowed.
     *
     * @param jobId     backup job identifier
     * @param error     error message
     * @param retryable whether retry is allowed
     */
    void markFailed(UUID jobId, String error, boolean retryable);

}
