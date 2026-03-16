package db.shield.backup.orchestrator.service.repository;

import db.shield.backup.orchestrator.service.model.BackupJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BackupJobRepository extends JpaRepository<BackupJobEntity, UUID> {
}
