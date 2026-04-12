package db.shield.backup.orchestrator.service.repository;

import db.shield.backup.orchestrator.service.model.BackupScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BackupScheduleRepository extends JpaRepository<BackupScheduleEntity, UUID> {
    List<BackupScheduleEntity> findTop100ByEnabledTrueAndNextRunAtBefore(Instant now);
}
