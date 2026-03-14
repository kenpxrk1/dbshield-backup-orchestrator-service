package db.shield.backup.orchestrator.service.service;

import db.shield.backup.orchestrator.service.dto.request.CreateBackupRequest;
import db.shield.backup.orchestrator.service.dto.response.BackupJobResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class BackupServiceImpl implements BackupService {


    @Transactional
    @Override
    public BackupJobResponse createBackup(CreateBackupRequest request) {
        return null;
    }

    @Override
    public BackupJobResponse getBackup(UUID jobId) {
        return null;
    }

    @Override
    public List<BackupJobResponse> getAllBackups() {
        return List.of();
    }

    @Override
    public void cancelBackup(UUID jobId) {

    }

    @Override
    public void markStarted(UUID jobId, String workerId) {

    }

    @Override
    public void markCompleted(UUID jobId, String filePath, long fileSize) {

    }

    @Override
    public void markFailed(UUID jobId, String error, boolean retryable) {

    }
}
