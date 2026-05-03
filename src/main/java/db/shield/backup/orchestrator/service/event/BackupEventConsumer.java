package db.shield.backup.orchestrator.service.event;

import db.shield.backup.orchestrator.service.service.BackupService;
import db.shield.backup.orchestrator.service.dto.event.BackupCompletedEvent;
import db.shield.backup.orchestrator.service.dto.event.BackupFailedEvent;
import db.shield.backup.orchestrator.service.dto.event.BackupStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackupEventConsumer {

    private static final String GROUP_ID = "backup-orchestrator";
    private final BackupService backupService;

    @KafkaListener(
            topics = "backup.job.started",
            groupId = GROUP_ID,
            containerFactory = "errorHandlingKafkaListenerContainerFactory",
            properties = {
                    "spring.json.value.default.type=db.shield.backup.orchestrator.service.dto.event.BackupStartedEvent"
            }
    )
    public void onBackupStarted(BackupStartedEvent event) {
        log.info("Consumed backup started event for job: {}, worker: {}", event.jobId(), event.workerId());
        backupService.markStarted(event.jobId(), event.workerId());
    }

    @KafkaListener(
            topics = "backup.job.completed",
            groupId = GROUP_ID,
            containerFactory = "errorHandlingKafkaListenerContainerFactory",
            properties = {
                    "spring.json.value.default.type=db.shield.backup.orchestrator.service.dto.event.BackupCompletedEvent"
            }
    )
    public void onBackupCompleted(BackupCompletedEvent event) {
        log.info("Consumed backup completed event for job: {}", event.jobId());
        backupService.markCompleted(
                event.jobId(),
                event.filePath(),
                event.fileSize()
        );
    }

    @KafkaListener(
            topics = "backup.job.failed",
            groupId = GROUP_ID,
            containerFactory = "errorHandlingKafkaListenerContainerFactory",
            properties = {
                    "spring.json.value.default.type=db.shield.backup.orchestrator.service.dto.event.BackupFailedEvent"
            }
    )
    public void onBackupFailed(BackupFailedEvent event) {
        log.info("Consumed backup failed event for job: {}, failed at: {}", event.jobId(), event.failedAt());
        backupService.markFailed(
                event.jobId(),
                event.error(),
                event.retryable()
        );
    }
}
