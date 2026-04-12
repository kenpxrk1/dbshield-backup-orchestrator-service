package db.shield.backup.orchestrator.service.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateScheduleRequest(

        @Schema(
                description = "ID of the database to be backed up",
                example = "550e8400-e29b-41d4-a716-446655440000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID databaseId,

        @Schema(
                description = """
                        Cron expression in Spring format: second minute hour day month day-of-week.
                        
                        Examples:
                        - "0 */5 * * * *"  -> every 5 minutes
                        - "0 0 * * * *"    -> every hour
                        - "0 0 2 * * *"    -> every day at 02:00
                        """,
                example = "0 */5 * * * *",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String cronExpression,

        @Schema(
                description = "Whether the schedule is enabled",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        boolean enabled

) {}
