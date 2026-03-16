package db.shield.backup.orchestrator.service.repository;

import db.shield.backup.orchestrator.service.model.BackupScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BackupScheduleRepository extends JpaRepository<BackupScheduleEntity, UUID> {

}
