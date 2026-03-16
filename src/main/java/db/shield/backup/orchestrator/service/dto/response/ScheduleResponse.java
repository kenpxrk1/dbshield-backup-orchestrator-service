package db.shield.backup.orchestrator.service.dto.response;

import java.util.UUID;

public record ScheduleResponse(

        UUID id,
        UUID databaseId,
        String cronExpression,
        boolean enabled

) {
}
