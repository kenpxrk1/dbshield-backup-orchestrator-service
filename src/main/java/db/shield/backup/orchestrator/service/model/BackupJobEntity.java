package db.shield.backup.orchestrator.service.model;


import db.shield.backup.orchestrator.service.model.constant.BackupStatus;
import db.shield.backup.orchestrator.service.model.constant.DatabaseType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "backup_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupJobEntity extends BaseEntity {

    @Column(name = "database_id", nullable = false)
    private UUID databaseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "db_type", nullable = false)
    private DatabaseType dbType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackupStatus status;

    @Column(name = "requested_at")
    private Instant requestedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "error_message")
    private String errorMessage;

}
