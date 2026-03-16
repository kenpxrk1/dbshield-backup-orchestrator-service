package db.shield.backup.orchestrator.service.repository;

import db.shield.backup.orchestrator.service.model.BackupResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BackupResultRepository extends JpaRepository<BackupResultEntity, UUID> {

    boolean existsByJobId(UUID jobId);
}
