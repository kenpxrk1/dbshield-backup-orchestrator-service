package db.shield.backup.orchestrator.service.dto.request;

import java.util.UUID;

public record CreateScheduleRequest(

        UUID databaseId,
        String cronExpression,
        boolean enabled

) {
}
