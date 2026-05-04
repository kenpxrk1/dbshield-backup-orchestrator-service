package db.shield.backup.orchestrator.service.service.scheduler;

import db.shield.backup.orchestrator.service.dto.request.CreateBackupRequest;
import db.shield.backup.orchestrator.service.dto.request.CreateScheduleRequest;
import db.shield.backup.orchestrator.service.model.BackupScheduleEntity;
import db.shield.backup.orchestrator.service.repository.BackupScheduleRepository;
import db.shield.backup.orchestrator.service.service.BackupService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class BackupScheduleService {

    private final BackupScheduleRepository repository;

    @Transactional
    public void createSchedule(CreateScheduleRequest request) {

        validateCron(request.cronExpression());

        CronExpression cron = CronExpression.parse(request.cronExpression());

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime next = cron.next(now);

        if (next == null) {
            throw new IllegalArgumentException("Cron expression does not produce future executions");
        }

        BackupScheduleEntity entity = BackupScheduleEntity.builder()
                .databaseId(request.databaseId())
                .cronExpression(request.cronExpression())
                .enabled(request.enabled())
                .build();

        entity.initId();
        entity.setLastRunAt(null);
        entity.setNextRunAt(next.toInstant());

        repository.save(entity);

        log.info("Backup schedule created databaseId={} cron={} nextRunAt={}",
                entity.getDatabaseId(),
                entity.getCronExpression(),
                entity.getNextRunAt()
        );
    }

    private void validateCron(String cronExpression) {
        try {
            CronExpression.parse(cronExpression);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cron expression: " + cronExpression, e);
        }
    }
}
