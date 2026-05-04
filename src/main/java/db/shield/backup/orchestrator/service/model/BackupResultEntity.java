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

import java.util.UUID;

@Entity
@Table(name = "backup_result")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupResultEntity extends BaseEntity {

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column
    private String checksum;

}
