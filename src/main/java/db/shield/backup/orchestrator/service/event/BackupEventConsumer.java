package db.shield.backup.orchestrator.service.event;

import db.shield.backup.orchestrator.service.service.BackupService;
import db.shield.backup.orchestrator.service.dto.event.BackupCompletedEvent;
import db.shield.backup.orchestrator.service.dto.event.BackupFailedEvent;
import db.shield.backup.orchestrator.service.dto.event.BackupStartedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BackupEventConsumer {

    private static final String GROUP_ID = "backup-orchestrator";
    private final BackupService backupService;

    @KafkaListener(
            topics = "backup.job.started",
            groupId = GROUP_ID,
            containerFactory = "errorHandlingKafkaListenerContainerFactory"
    )
    public void onBackupStarted(BackupStartedEvent event) {
        backupService.markStarted(event.jobId(), event.workerId());
    }

    @KafkaListener(
            topics = "backup.job.completed",
            groupId = GROUP_ID,
            containerFactory = "errorHandlingKafkaListenerContainerFactory"
    )
    public void onBackupCompleted(BackupCompletedEvent event) {
        backupService.markCompleted(
                event.jobId(),
                event.filePath(),
                event.fileSize()
        );
    }

    @KafkaListener(
            topics = "backup.job.failed",
            groupId = GROUP_ID,
            containerFactory = "errorHandlingKafkaListenerContainerFactory"
    )
    public void onBackupFailed(BackupFailedEvent event) {
        backupService.markFailed(
                event.jobId(),
                event.error(),
                event.retryable()
        );
    }
}