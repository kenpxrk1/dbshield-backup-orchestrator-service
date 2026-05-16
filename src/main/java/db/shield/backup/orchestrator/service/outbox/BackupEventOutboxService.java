package db.shield.backup.orchestrator.service.outbox;

import db.shield.backup.orchestrator.service.dto.event.BackupRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupEventOutboxService {

    public static final String TOPIC_BACKUP_REQUESTED = "backup.job.requested";

    private final BackupEventOutboxRepository repository;
    private final OutboxProperties outboxProperties;

    @Transactional
    public void enqueueBackupRequested(BackupRequestedEvent event) {
        if (!outboxProperties.isEnabled()) {
            throw new IllegalStateException("Outbox is disabled. Direct Kafka publishing is not configured");
        }

        BackupEventOutboxEntity outboxEvent = BackupEventOutboxEntity.builder()
                .topic(TOPIC_BACKUP_REQUESTED)
                .eventKey(event.jobId().toString())
                .jobId(event.jobId())
                .databaseId(event.databaseId())
                .dbType(event.dbType())
                .requestedAt(event.requestedAt().toInstant())
                .status(BackupEventOutboxStatus.PENDING)
                .attemptCount(0)
                .nextAttemptAt(Instant.now())
                .build();

        repository.save(outboxEvent);

        log.info("Backup request event persisted to outbox. outboxId={}, jobId={}, databaseId={}",
                outboxEvent.getId(), outboxEvent.getJobId(), outboxEvent.getDatabaseId());
    }
}
