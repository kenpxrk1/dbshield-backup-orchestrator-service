package db.shield.backup.orchestrator.service.event;

import db.shield.backup.orchestrator.service.dto.event.BackupRequestedEvent;
import db.shield.backup.orchestrator.service.outbox.BackupEventOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackupEventProducer {

    private final BackupEventOutboxService outboxService;

    public void sendBackupRequested(BackupRequestedEvent event) {
        outboxService.enqueueBackupRequested(event);
        log.info("Backup request queued in outbox for databaseId={} and jobId={}", event.databaseId(), event.jobId());
    }
}
