package db.shield.backup.orchestrator.service.service.scheduler;

import db.shield.backup.orchestrator.service.dto.request.CreateBackupRequest;
import db.shield.backup.orchestrator.service.model.BackupScheduleEntity;
import db.shield.backup.orchestrator.service.repository.BackupScheduleRepository;
import db.shield.backup.orchestrator.service.service.BackupService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackupScheduler {

    private final BackupScheduleRepository scheduleRepository;
    private final BackupService backupService;

    @Scheduled(fixedDelay = 30_000)
    @SchedulerLock(name = "backupScheduler", lockAtMostFor = "PT1M")
    @Transactional
    public void triggerBackups() {
        Instant now = Instant.now();

        List<BackupScheduleEntity> schedules =
                scheduleRepository.findTop100ByEnabledTrueAndNextRunAtBefore(now);

        for (BackupScheduleEntity schedule : schedules) {
            try {
                trigger(schedule, now);
            } catch (Exception e) {
                log.error("Failed to trigger backup for scheduleId={}", schedule.getId(), e);
            }
        }
    }

    private void trigger(BackupScheduleEntity schedule, Instant now) {
        CreateBackupRequest request = new CreateBackupRequest(schedule.getDatabaseId());

        backupService.createBackup(request);

        CronExpression cron = CronExpression.parse(schedule.getCronExpression());
        Instant nextRun = cron.next(now);

        schedule.setLastRunAt(now);
        schedule.setNextRunAt(nextRun);

        log.info("Scheduled backup triggered databaseId={} nextRunAt={}",
                schedule.getDatabaseId(), nextRun);
    }
}