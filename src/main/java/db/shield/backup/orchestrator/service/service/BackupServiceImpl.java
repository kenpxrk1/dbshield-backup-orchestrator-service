package db.shield.backup.orchestrator.service.service;

import db.shield.backup.orchestrator.service.dto.event.BackupRequestedEvent;
import db.shield.backup.orchestrator.service.dto.integration.response.DatabaseConfigurationResponse;
import db.shield.backup.orchestrator.service.dto.request.CreateBackupRequest;
import db.shield.backup.orchestrator.service.dto.response.BackupJobResponse;
import db.shield.backup.orchestrator.service.event.BackupEventProducer;
import db.shield.backup.orchestrator.service.exception.BackupJobNotFoundException;
import db.shield.backup.orchestrator.service.exception.InvalidBackupJobStateException;
import db.shield.backup.orchestrator.service.integration.internal.ConfigurationServiceClient;
import db.shield.backup.orchestrator.service.mapper.BackupJobMapper;
import db.shield.backup.orchestrator.service.model.BackupJobEntity;
import db.shield.backup.orchestrator.service.model.BackupResultEntity;
import db.shield.backup.orchestrator.service.model.constant.BackupStatus;
import db.shield.backup.orchestrator.service.repository.BackupJobRepository;
import db.shield.backup.orchestrator.service.repository.BackupResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupServiceImpl implements BackupService {

    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final BackupJobRepository backupJobRepository;
    private final BackupResultRepository backupResultRepository;
    private final BackupEventProducer eventProducer;
    private final BackupJobMapper mapper;
    private final ConfigurationServiceClient configurationServiceClient;

    @Transactional
    @Override
    public BackupJobResponse createBackup(CreateBackupRequest request) {
        DatabaseConfigurationResponse configurationResponse = configurationServiceClient.getById(request.databaseId());

        BackupJobEntity job = mapper.toNewJob(request);
        job.setDbType(configurationResponse.dbType());
        backupJobRepository.save(job);

        BackupRequestedEvent event = buildRequestedEvent(job, job.getRequestedAt());
        eventProducer.sendBackupRequested(event);

        log.info("Backup job created jobId={} databaseId={} status={} retryCount={}", job.getId(), job.getDatabaseId(),
                job.getStatus(), job.getRetryCount());

        return mapper.toResponse(job);
    }

    @Transactional(readOnly = true)
    @Override
    public BackupJobResponse getBackup(UUID jobId) {
        BackupJobEntity job = getJobOrThrow(jobId);
        return mapper.toResponse(job);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BackupJobResponse> getAllBackups() {
        return backupJobRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    @Override
    public void cancelBackup(UUID jobId) {
        BackupJobEntity job = getJobOrThrow(jobId);

        BackupStatus currentStatus = job.getStatus();
        if (!currentStatus.canCancel()) {
            throw new InvalidBackupJobStateException(jobId, currentStatus, "cancel");
        }

        job.setStatus(BackupStatus.CANCELLED);
        backupJobRepository.save(job);

        log.info("Backup job cancelled jobId={} previousStatus={}", jobId, currentStatus);
    }

    @Transactional
    @Override
    public void markStarted(UUID jobId, String workerId) {
        BackupJobEntity job = getJobOrThrow(jobId);

        BackupStatus currentStatus = job.getStatus();
        if (currentStatus == BackupStatus.STARTED) {
            log.debug("Duplicate start event ignored jobId={} workerId={}", jobId, workerId);
            return;
        }
        if (currentStatus.isFinal()) {
            log.warn("Start event ignored for finalized job jobId={} workerId={} status={}", jobId, workerId, currentStatus);
            return;
        }
        if (!currentStatus.canStart()) {
            log.warn("Start event ignored due to invalid status jobId={} workerId={} status={}", jobId, workerId, currentStatus);
            return;
        }

        job.setStatus(BackupStatus.STARTED);
        job.setStartedAt(Instant.now());
        backupJobRepository.save(job);

        log.info("Backup job started jobId={} workerId={}", jobId, workerId);
    }

    @Transactional
    @Override
    public void markCompleted(UUID jobId, String filePath, long fileSize) {
        BackupJobEntity job = getJobOrThrow(jobId);

        BackupStatus currentStatus = job.getStatus();
        if (currentStatus == BackupStatus.COMPLETED) {
            log.debug("Duplicate completion event ignored jobId={} filePath={}", jobId, filePath);
            return;
        }
        if (currentStatus == BackupStatus.CANCELLED || currentStatus == BackupStatus.FAILED) {
            log.warn("Completion event ignored for finalized job jobId={} status={}", jobId, currentStatus);
            return;
        }
        if (!currentStatus.canComplete()) {
            log.warn("Completion event ignored due to invalid status jobId={} status={}", jobId, currentStatus);
            return;
        }

        job.setStatus(BackupStatus.COMPLETED);
        job.setCompletedAt(Instant.now());
        backupJobRepository.save(job);

        if (backupResultRepository.existsByJobId(jobId)) {
            log.warn("Backup result already exists for jobId={} - skipping result insert", jobId);
            return;
        }

        BackupResultEntity result = mapper.toNewResult(job, filePath, fileSize);
        backupResultRepository.save(result);

        log.info("Backup job completed jobId={} filePath={} fileSize={}", jobId, filePath, fileSize);
    }

    @Transactional
    @Override
    public void markFailed(UUID jobId, String error, boolean retryable) {
        BackupJobEntity job = getJobOrThrow(jobId);

        BackupStatus currentStatus = job.getStatus();
        if (currentStatus == BackupStatus.FAILED) {
            log.debug("Duplicate failure event ignored jobId={}", jobId);
            return;
        }
        if (currentStatus == BackupStatus.COMPLETED || currentStatus == BackupStatus.CANCELLED) {
            log.warn("Failure event ignored for finalized job jobId={} status={}", jobId, currentStatus);
            return;
        }
        if (!currentStatus.canFail()) {
            log.warn("Failure event ignored due to invalid status jobId={} status={}", jobId, currentStatus);
            return;
        }

        String safeError = error == null ? "unknown" : error;
        Integer retryCount = job.getRetryCount();
        int currentRetryCount = retryCount == null ? 0 : retryCount;

        job.setErrorMessage(safeError);

        if (retryable && currentRetryCount < MAX_RETRY_ATTEMPTS) {
            int nextRetryCount = currentRetryCount + 1;
            job.setRetryCount(nextRetryCount);
            job.setStatus(BackupStatus.REQUESTED);
            job.setRequestedAt(Instant.now());
            backupJobRepository.save(job);

            BackupRequestedEvent retryEvent = buildRequestedEvent(job, job.getRequestedAt());
            eventProducer.sendBackupRequested(retryEvent);

            log.info("Retry scheduled for backup job jobId={} retryCount={} maxRetries={}", jobId, nextRetryCount, MAX_RETRY_ATTEMPTS);
            return;
        }

        job.setStatus(BackupStatus.FAILED);
        backupJobRepository.save(job);

        log.error("Backup job failed jobId={} retryable={} retryCount={} error={}", jobId, retryable, currentRetryCount, safeError);
    }

    private BackupJobEntity getJobOrThrow(UUID jobId) {
        return backupJobRepository.findById(jobId).orElseThrow(() -> new BackupJobNotFoundException(jobId));
    }

    private BackupRequestedEvent buildRequestedEvent(BackupJobEntity job, Instant requestedAt) {
        return new BackupRequestedEvent(job.getId(), job.getDatabaseId(), job.getDbType(),
                OffsetDateTime.ofInstant(requestedAt, ZoneId.systemDefault()));
    }
}
