package db.shield.backup.orchestrator.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "backup_schedule")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupScheduleEntity extends BaseEntity {

    @Column(name = "database_id", nullable = false)
    private UUID databaseId;

    @Column(name = "cron_expression", nullable = false)
    private String cronExpression;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "last_run_at")
    private Instant lastRunAt;

    @Column(name = "next_run_at")
    private Instant nextRunAt;

}
