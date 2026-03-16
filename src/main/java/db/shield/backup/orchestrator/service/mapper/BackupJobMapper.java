package db.shield.backup.orchestrator.service.mapper;

import db.shield.backup.orchestrator.service.dto.request.CreateBackupRequest;
import db.shield.backup.orchestrator.service.dto.response.BackupJobResponse;
import db.shield.backup.orchestrator.service.model.BackupJobEntity;
import db.shield.backup.orchestrator.service.model.BackupResultEntity;
import db.shield.backup.orchestrator.service.model.constant.BackupStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(
        componentModel = "spring",
        imports = {Instant.class, BackupStatus.class},
        builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface BackupJobMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dbType", ignore = true)
    @Mapping(target = "status", expression = "java(BackupStatus.REQUESTED)")
    @Mapping(target = "requestedAt", expression = "java(Instant.now())")
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "retryCount", constant = "0")
    @Mapping(target = "errorMessage", ignore = true)
    BackupJobEntity toEntity(CreateBackupRequest request);

    BackupJobResponse toResponse(BackupJobEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "filePath", source = "filePath")
    @Mapping(target = "fileSize", source = "fileSize")
    @Mapping(target = "checksum", ignore = true)
    BackupResultEntity toResult(BackupJobEntity job, String filePath, long fileSize);

    default BackupJobEntity toNewJob(CreateBackupRequest request) {
        BackupJobEntity entity = toEntity(request);
        entity.initId();
        return entity;
    }

    default BackupResultEntity toNewResult(BackupJobEntity job, String filePath, long fileSize) {
        BackupResultEntity entity = toResult(job, filePath, fileSize);
        entity.initId();
        return entity;
    }
}
