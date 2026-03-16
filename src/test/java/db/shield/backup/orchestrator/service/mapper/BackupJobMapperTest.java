package db.shield.backup.orchestrator.service.mapper;

import db.shield.backup.orchestrator.service.Initializer;
import db.shield.backup.orchestrator.service.dto.response.BackupJobResponse;
import db.shield.backup.orchestrator.service.model.BackupJobEntity;
import db.shield.backup.orchestrator.service.model.BackupResultEntity;
import db.shield.backup.orchestrator.service.model.constant.BackupStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BackupJobMapperTest extends Initializer {

    private final BackupJobMapper mapper = new BackupJobMapperImpl();

    @Test
    void shouldMapRequestToEntity() {
        BackupJobEntity entity = mapper.toEntity(createBackupRequest);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getDatabaseId()).isEqualTo(databaseId);
        assertThat(entity.getStatus()).isEqualTo(BackupStatus.REQUESTED);
        assertThat(entity.getRequestedAt()).isNotNull();
        assertThat(entity.getRetryCount()).isEqualTo(0);
        assertThat(entity.getStartedAt()).isNull();
        assertThat(entity.getCompletedAt()).isNull();
    }

    @Test
    void shouldMapEntityToResponse() {
        BackupJobResponse response = mapper.toResponse(backupJobEntity);

        assertThat(response.id()).isEqualTo(jobId);
        assertThat(response.databaseId()).isEqualTo(databaseId);
        assertThat(response.dbType()).isEqualTo(backupJobEntity.getDbType());
        assertThat(response.status()).isEqualTo(backupJobEntity.getStatus());
        assertThat(response.requestedAt()).isEqualTo(requestedAt);
    }

    @Test
    void shouldMapEntityListToResponses() {
        List<BackupJobResponse> responses = List.of(backupJobEntity)
                .stream()
                .map(mapper::toResponse)
                .toList();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(jobId);
    }

    @Test
    void shouldMapJobToResult() {
        BackupResultEntity result = mapper.toResult(backupJobEntity, filePath, fileSize);

        assertThat(result.getId()).isNull();
        assertThat(result.getJobId()).isEqualTo(jobId);
        assertThat(result.getFilePath()).isEqualTo(filePath);
        assertThat(result.getFileSize()).isEqualTo(fileSize);
    }
}
