package db.shield.backup.orchestrator.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "backup_result")
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
