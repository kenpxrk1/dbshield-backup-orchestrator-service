package db.shield.backup.orchestrator.service.event;

import db.shield.backup.orchestrator.service.dto.event.BackupRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackupEventProducer {

    private static final String TOPIC_BACKUP_REQUESTED = "backup.job.requested";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendBackupRequested(BackupRequestedEvent event) {
        log.info("Send backup request to {} for database with id: {}", TOPIC_BACKUP_REQUESTED, event.databaseId());
        kafkaTemplate.send(
                TOPIC_BACKUP_REQUESTED,
                event.jobId().toString(),
                event
        );
    }
}
