package db.shield.backup.orchestrator.service.event;

import db.shield.backup.orchestrator.service.dto.event.BackupRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BackupEventProducer {

    private static final String TOPIC_BACKUP_REQUESTED = "backup.job.requested";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendBackupRequested(BackupRequestedEvent event) {
        kafkaTemplate.send(
                TOPIC_BACKUP_REQUESTED,
                event.jobId().toString(),
                event
        );
    }
}
