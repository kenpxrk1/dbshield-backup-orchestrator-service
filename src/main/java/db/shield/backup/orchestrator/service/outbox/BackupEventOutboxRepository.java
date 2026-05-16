package db.shield.backup.orchestrator.service.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface BackupEventOutboxRepository extends JpaRepository<BackupEventOutboxEntity, UUID> {

    @Query("""
            SELECT event FROM BackupEventOutboxEntity event
            WHERE event.status IN :statuses
              AND event.nextAttemptAt <= :now
            ORDER BY event.createdAt ASC
            """)
    List<BackupEventOutboxEntity> findReadyToDispatch(
            @Param("statuses") Collection<BackupEventOutboxStatus> statuses,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE BackupEventOutboxEntity event
               SET event.status = :sendingStatus,
                   event.updatedAt = :updatedAt,
                   event.lastError = NULL
             WHERE event.id = :eventId
               AND event.status IN :claimableStatuses
            """)
    int markSending(
            @Param("eventId") UUID eventId,
            @Param("sendingStatus") BackupEventOutboxStatus sendingStatus,
            @Param("claimableStatuses") Collection<BackupEventOutboxStatus> claimableStatuses,
            @Param("updatedAt") Instant updatedAt
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE BackupEventOutboxEntity event
               SET event.status = :sentStatus,
                   event.sentAt = :sentAt,
                   event.updatedAt = :updatedAt,
                   event.lastError = NULL
             WHERE event.id = :eventId
            """)
    int markSent(
            @Param("eventId") UUID eventId,
            @Param("sentStatus") BackupEventOutboxStatus sentStatus,
            @Param("sentAt") Instant sentAt,
            @Param("updatedAt") Instant updatedAt
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE BackupEventOutboxEntity event
               SET event.status = :retryStatus,
                   event.attemptCount = :attemptCount,
                   event.nextAttemptAt = :nextAttemptAt,
                   event.lastError = :lastError,
                   event.updatedAt = :updatedAt
             WHERE event.id = :eventId
            """)
    int markRetry(
            @Param("eventId") UUID eventId,
            @Param("retryStatus") BackupEventOutboxStatus retryStatus,
            @Param("attemptCount") int attemptCount,
            @Param("nextAttemptAt") Instant nextAttemptAt,
            @Param("lastError") String lastError,
            @Param("updatedAt") Instant updatedAt
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE BackupEventOutboxEntity event
               SET event.status = :failedStatus,
                   event.attemptCount = :attemptCount,
                   event.lastError = :lastError,
                   event.updatedAt = :updatedAt
             WHERE event.id = :eventId
            """)
    int markFailed(
            @Param("eventId") UUID eventId,
            @Param("failedStatus") BackupEventOutboxStatus failedStatus,
            @Param("attemptCount") int attemptCount,
            @Param("lastError") String lastError,
            @Param("updatedAt") Instant updatedAt
    );
}
