package db.shield.backup.orchestrator.service.service;

import db.shield.backup.orchestrator.service.Initializer;
import db.shield.backup.orchestrator.service.dto.event.BackupRequestedEvent;
import db.shield.backup.orchestrator.service.dto.integration.response.DatabaseConfigurationResponse;
import db.shield.backup.orchestrator.service.dto.response.BackupJobResponse;
import db.shield.backup.orchestrator.service.event.BackupEventProducer;
import db.shield.backup.orchestrator.service.exception.BackupJobNotFoundException;
import db.shield.backup.orchestrator.service.exception.InvalidBackupJobStateException;
import db.shield.backup.orchestrator.service.integration.internal.ConfigurationServiceClient;
import db.shield.backup.orchestrator.service.mapper.BackupJobMapper;
import db.shield.backup.orchestrator.service.model.BackupJobEntity;
import db.shield.backup.orchestrator.service.model.BackupResultEntity;
import db.shield.backup.orchestrator.service.model.constant.BackupStatus;
import db.shield.backup.orchestrator.service.model.constant.DatabaseType;
import db.shield.backup.orchestrator.service.repository.BackupJobRepository;
import db.shield.backup.orchestrator.service.repository.BackupResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackupServiceImplTest extends Initializer {

    @Mock
    private BackupJobRepository backupJobRepository;

    @Mock
    private BackupResultRepository backupResultRepository;

    @Mock
    private BackupEventProducer eventProducer;

    @Mock
    private BackupJobMapper mapper;

    @Mock
    private ConfigurationServiceClient configurationServiceClient;

    @InjectMocks
    private BackupServiceImpl service;

    @Test
    void shouldCreateBackupJob() {
        BackupJobEntity job = BackupJobEntity.builder()
                .databaseId(databaseId)
                .dbType(DatabaseType.POSTGRES)
                .status(BackupStatus.REQUESTED)
                .requestedAt(requestedAt)
                .retryCount(0)
                .build();
        job.setId(jobId);

        BackupJobResponse response = backupJobResponse;

        when(configurationServiceClient.getById(databaseId)).thenReturn(new DatabaseConfigurationResponse(DatabaseType.POSTGRES));
        when(mapper.toNewJob(createBackupRequest)).thenReturn(job);
        when(backupJobRepository.save(job)).thenReturn(job);
        when(mapper.toResponse(job)).thenReturn(response);

        BackupJobResponse result = service.createBackup(createBackupRequest);

        assertThat(result).isEqualTo(response);

        ArgumentCaptor<BackupRequestedEvent> eventCaptor = ArgumentCaptor.forClass(BackupRequestedEvent.class);
        verify(eventProducer).sendBackupRequested(eventCaptor.capture());
        assertThat(eventCaptor.getValue().jobId()).isEqualTo(jobId);
    }

    @Test
    void shouldReturnBackupJob() {
        when(backupJobRepository.findById(jobId)).thenReturn(Optional.of(backupJobEntity));
        when(mapper.toResponse(backupJobEntity)).thenReturn(backupJobResponse);

        BackupJobResponse response = service.getBackup(jobId);

        assertThat(response).isEqualTo(backupJobResponse);
    }

    @Test
    void shouldThrowWhenBackupNotFound() {
        when(backupJobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBackup(jobId))
                .isInstanceOf(BackupJobNotFoundException.class);
    }

    @Test
    void shouldReturnAllBackups() {
        when(backupJobRepository.findAll()).thenReturn(List.of(backupJobEntity));
        when(mapper.toResponse(backupJobEntity)).thenReturn(backupJobResponse);

        List<BackupJobResponse> responses = service.getAllBackups();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0)).isEqualTo(backupJobResponse);
    }

    @Test
    void shouldCancelBackupJob() {
        BackupJobEntity job = BackupJobEntity.builder()
                .databaseId(databaseId)
                .dbType(DatabaseType.POSTGRES)
                .status(BackupStatus.REQUESTED)
                .requestedAt(requestedAt)
                .retryCount(0)
                .build();
        job.setId(jobId);

        when(backupJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        service.cancelBackup(jobId);

        assertThat(job.getStatus()).isEqualTo(BackupStatus.CANCELLED);
        verify(backupJobRepository).save(job);
    }

    @Test
    void shouldThrowWhenCancelNotAllowed() {
        BackupJobEntity job = BackupJobEntity.builder()
                .databaseId(databaseId)
                .dbType(DatabaseType.POSTGRES)
                .status(BackupStatus.COMPLETED)
                .requestedAt(requestedAt)
                .retryCount(0)
                .build();
        job.setId(jobId);

        when(backupJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.cancelBackup(jobId))
                .isInstanceOf(InvalidBackupJobStateException.class);
    }

    @Test
    void shouldMarkStarted() {
        BackupJobEntity job = BackupJobEntity.builder()
                .databaseId(databaseId)
                .dbType(DatabaseType.POSTGRES)
                .status(BackupStatus.REQUESTED)
                .requestedAt(requestedAt)
                .retryCount(0)
                .build();
        job.setId(jobId);

        when(backupJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        service.markStarted(jobId, workerId);

        assertThat(job.getStatus()).isEqualTo(BackupStatus.STARTED);
        assertThat(job.getStartedAt()).isNotNull();
        verify(backupJobRepository).save(job);
    }

    @Test
    void shouldIgnoreStartWhenFinal() {
        BackupJobEntity job = BackupJobEntity.builder()
                .databaseId(databaseId)
                .dbType(DatabaseType.POSTGRES)
                .status(BackupStatus.COMPLETED)
                .requestedAt(requestedAt)
                .retryCount(0)
                .build();
        job.setId(jobId);

        when(backupJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        service.markStarted(jobId, workerId);

        assertThat(job.getStatus()).isEqualTo(BackupStatus.COMPLETED);
        verify(backupJobRepository, never()).save(job);
    }

    @Test
    void shouldMarkCompletedAndSaveResult() {
        BackupJobEntity job = BackupJobEntity.builder()
                .databaseId(databaseId)
                .dbType(DatabaseType.POSTGRES)
                .status(BackupStatus.STARTED)
                .requestedAt(requestedAt)
                .retryCount(0)
                .build();
        job.setId(jobId);

        BackupResultEntity result = BackupResultEntity.builder()
                .jobId(jobId)
                .filePath(filePath)
                .fileSize(fileSize)
                .build();

        when(backupJobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(backupResultRepository.existsByJobId(jobId)).thenReturn(false);
        when(mapper.toNewResult(job, filePath, fileSize)).thenReturn(result);

        service.markCompleted(jobId, filePath, fileSize);

        assertThat(job.getStatus()).isEqualTo(BackupStatus.COMPLETED);
        assertThat(job.getCompletedAt()).isNotNull();
        verify(backupJobRepository).save(job);
        verify(backupResultRepository).save(result);
    }

    @Test
    void shouldIgnoreCompletionWhenAlreadyCompleted() {
        BackupJobEntity job = BackupJobEntity.builder()
                .databaseId(databaseId)
                .dbType(DatabaseType.POSTGRES)
                .status(BackupStatus.COMPLETED)
                .requestedAt(requestedAt)
                .retryCount(0)
                .build();
        job.setId(jobId);

        when(backupJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        service.markCompleted(jobId, filePath, fileSize);

        verify(backupJobRepository, never()).save(job);
        verify(backupResultRepository, never()).save(any());
    }

    @Test
    void shouldRetryOnFailureWhenRetryable() {
        BackupJobEntity job = BackupJobEntity.builder()
                .databaseId(databaseId)
                .dbType(DatabaseType.POSTGRES)
                .status(BackupStatus.STARTED)
                .requestedAt(requestedAt)
                .retryCount(null)
                .build();
        job.setId(jobId);

        when(backupJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        service.markFailed(jobId, errorMessage, true);

        assertThat(job.getStatus()).isEqualTo(BackupStatus.REQUESTED);
        assertThat(job.getRetryCount()).isEqualTo(1);
        assertThat(job.getRequestedAt()).isAfterOrEqualTo(requestedAt);
        verify(backupJobRepository).save(job);
        verify(eventProducer).sendBackupRequested(any(BackupRequestedEvent.class));
    }

    @Test
    void shouldMarkFailedWhenRetryLimitExceeded() {
        BackupJobEntity job = BackupJobEntity.builder()
                .databaseId(databaseId)
                .dbType(DatabaseType.POSTGRES)
                .status(BackupStatus.STARTED)
                .requestedAt(requestedAt)
                .retryCount(3)
                .build();
        job.setId(jobId);

        when(backupJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        service.markFailed(jobId, errorMessage, true);

        assertThat(job.getStatus()).isEqualTo(BackupStatus.FAILED);
        verify(backupJobRepository).save(job);
        verify(eventProducer, never()).sendBackupRequested(any(BackupRequestedEvent.class));
    }
}
