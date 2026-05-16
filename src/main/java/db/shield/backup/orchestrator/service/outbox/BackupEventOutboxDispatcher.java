package db.shield.backup.orchestrator.service.outbox;

import db.shield.backup.orchestrator.service.dto.event.BackupRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackupEventOutboxDispatcher {

    private static final Set<BackupEventOutboxStatus> READY_STATUSES = Set.of(
            BackupEventOutboxStatus.PENDING,
            BackupEventOutboxStatus.RETRY
    );

    private final BackupEventOutboxRepository repository;
    private final OutboxProperties outboxProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelayString = "${outbox.dispatcher.fixed-delay-ms:2000}")
    public void dispatch() {
        if (!outboxProperties.isEnabled()) {
            return;
        }

        int batchSize = Math.max(1, outboxProperties.getDispatcher().getBatchSize());
        List<BackupEventOutboxEntity> events = repository.findReadyToDispatch(
                READY_STATUSES,
                Instant.now(),
                PageRequest.of(0, batchSize)
        );

        for (BackupEventOutboxEntity event : events) {
            dispatchSingle(event);
        }
    }

    protected void dispatchSingle(BackupEventOutboxEntity event) {
        int claimUpdated = repository.markSending(
                event.getId(),
                BackupEventOutboxStatus.SENDING,
                READY_STATUSES,
                Instant.now()
        );
        if (claimUpdated == 0) {
            return;
        }

        int nextAttemptCount = event.getAttemptCount() + 1;

        try {
            BackupRequestedEvent payload = restoreEvent(event);
            kafkaTemplate.send(event.getTopic(), event.getEventKey(), payload).join();

            repository.markSent(
                    event.getId(),
                    BackupEventOutboxStatus.SENT,
                    Instant.now(),
                    Instant.now()
            );

            log.info("Outbox event sent to Kafka. outboxId={}, topic={}, key={}, jobId={}",
                    event.getId(), event.getTopic(), event.getEventKey(), event.getJobId());
        } catch (Exception ex) {
            String error = truncate(ex);

            if (nextAttemptCount >= outboxProperties.getDispatcher().getMaxAttempts()) {
                repository.markFailed(
                        event.getId(),
                        BackupEventOutboxStatus.FAILED,
                        nextAttemptCount,
                        error,
                        Instant.now()
                );

                log.error("Outbox event marked as failed after max attempts. outboxId={}, attempts={}, jobId={}",
                        event.getId(), nextAttemptCount, event.getJobId(), ex);
                return;
            }

            Instant nextAttemptAt = Instant.now().plusMillis(computeBackoffMs(nextAttemptCount));
            repository.markRetry(
                    event.getId(),
                    BackupEventOutboxStatus.RETRY,
                    nextAttemptCount,
                    nextAttemptAt,
                    error,
                    Instant.now()
            );

            log.warn("Outbox dispatch failed, retry scheduled. outboxId={}, jobId={}, attempt={}, nextAttemptAt={}",
                    event.getId(), event.getJobId(), nextAttemptCount, nextAttemptAt, ex);
        }
    }

    private BackupRequestedEvent restoreEvent(BackupEventOutboxEntity event) {
        return new BackupRequestedEvent(
                event.getJobId(),
                event.getDatabaseId(),
                event.getDbType(),
                OffsetDateTime.ofInstant(event.getRequestedAt(), ZoneOffset.UTC)
        );
    }

    private long computeBackoffMs(int attempt) {
        long initialBackoffMs = Math.max(1, outboxProperties.getDispatcher().getInitialBackoffMs());
        long maxBackoffMs = Math.max(initialBackoffMs, outboxProperties.getDispatcher().getMaxBackoffMs());

        long delay = initialBackoffMs;
        for (int i = 1; i < attempt; i++) {
            delay = Math.min(delay * 2, maxBackoffMs);
        }

        return delay;
    }

    private String truncate(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }

        return message.length() > 3000 ? message.substring(0, 3000) : message;
    }
}
